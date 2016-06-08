package com.example.gafur.coveragequalitydetector;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        AdapterView.OnItemSelectedListener {

    private GoogleMap mMap;
    UserLocalStore userLocalStore;//user information
    //Google ApiClient
    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;
    private LatLng myLocation = null;//location info
    private Marker myMarker;
    private int str_end=0,signalStr;//signal strength
    TelephonyManager Tel;
    MyPhoneStateListener    MyListener;
    private String operatorName;//operator name

    Spinner spinner;
    int threshold=-1;
    int onLocationChangedCounter=0;

    List<String> records;
    DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        userLocalStore = new UserLocalStore(this);
        //Initializing googleapi client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API)
                .build();

        /* Update the listener, and start it */
        MyListener   = new MyPhoneStateListener();
        Tel       = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        spinner=(Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        List<Integer> thresholds = new ArrayList<Integer>();
        thresholds.add(10);
        thresholds.add(11);
        thresholds.add(12);
        thresholds.add(13);
        thresholds.add(14);
        thresholds.add(15);
        thresholds.add(16);
        thresholds.add(17);
        thresholds.add(18);
        thresholds.add(19);
        thresholds.add(20);

        // Creating adapter for spinner
        ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, thresholds);
        spinner.setAdapter(dataAdapter);

        records=new ArrayList<String>();
        mydb = new DBHelper(this);
    }

    public void startSignal(View view){
        str_end=1;
    }

    public void stopSignal(View view){
        str_end=0;
        signalStr=0;
    }

    public void findOperator(){
        operatorName = Tel.getNetworkOperatorName();
    }

    /* Called when the application is minimized */
    @Override
    protected void onPause()
    {
        super.onPause();
        Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
    }

    /* Called when the application resumes */
    @Override
    protected void onResume()
    {
        super.onResume();
        Tel.listen(MyListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        threshold = (int) parent.getItemAtPosition(position);
        getAllEskiRecords();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /* —————————– */
    /* Start the PhoneState listener */
   /* —————————– */
    private class MyPhoneStateListener extends PhoneStateListener
    {
        /* Get the Signal strength from the provider, each tiome there is an update */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
            super.onSignalStrengthsChanged(signalStrength);
            if(str_end==1){
                signalStr=signalStrength.getGsmSignalStrength();//0-31 arası deger donduryor
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        getAllEskiRecords();
    }

    public void getAllEskiRecords(){
        int number=mydb.numberOfRows();

        Toast.makeText(getApplicationContext(), "kayit sayisi: "+number, Toast.LENGTH_SHORT).show();

        if(number!=0){
            records = mydb.getAllRecords();//eski bilgileri almak icin(varsa)
            Toast.makeText(getApplicationContext(), "Eski kayıtlar alındı!", Toast.LENGTH_SHORT).show();
            int i=0;
            for(String record:records){
                String[] str=record.split("\n");

                Double lat=Double.valueOf(str[2]);
                Double lon=Double.valueOf(str[3]);
                LatLng latLng=new LatLng(lat,lon);

                if((Integer.valueOf(str[4]) < 10)&&(Integer.valueOf(str[4])>threshold)) {
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(i+"")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));//0-10 arasi
                }
                else if((Integer.valueOf(str[4])<15)&&(Integer.valueOf(str[4])>threshold)){
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(i+"")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));//10-15 arasi
                }
                else if((Integer.valueOf(str[4])<20)&&(Integer.valueOf(str[4])>threshold)){
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(i+"")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));//15-20
                }
                else if((Integer.valueOf(str[4])>=20)&&(Integer.valueOf(str[4])>threshold)){
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(i+"")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
                i++;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.gafur.coveragequalitydetector/http/host/path")
        );
        AppIndex.AppIndexApi.start(googleApiClient, viewAction);
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.example.gafur.coveragequalitydetector/http/host/path")
        );
        AppIndex.AppIndexApi.end(googleApiClient, viewAction);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);

        Location location = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        //myLocation=new LatLng(location.getLatitude(),location.getLongitude());
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation=new LatLng(location.getLatitude(), location.getLongitude());

        if(myMarker!=null){
            myMarker.remove();
        }
        myMarker=mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title("I am here how !")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        if(onLocationChangedCounter%10==0){
            if((threshold<signalStr)&&(str_end==1)){
                DateFormat df = new SimpleDateFormat("d-MMM-yyyy HH:MM");
                String date = df.format(Calendar.getInstance().getTime());
                findOperator();

                if(mydb.updateRecord(operatorName,myLocation.latitude,myLocation.longitude,signalStr,date)){
                    Toast.makeText(getApplicationContext(), "Record is updated !", Toast.LENGTH_SHORT).show();
                }else {
                    boolean result = mydb.insertRecord(
                            operatorName,
                            String.valueOf(myLocation.latitude),
                            String.valueOf(myLocation.longitude),
                            signalStr,
                            date);
                    if (result) {
                        Toast.makeText(getApplicationContext(), "Record is inserted !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            mMap.clear();

            if(myMarker!=null){
                myMarker.remove();
            }
            myMarker=mMap.addMarker(new MarkerOptions()
                    .position(myLocation)
                    .title("I am here how !")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            getAllEskiRecords();//markerleri yeniliyorum
        }
        onLocationChangedCounter++;
    }

    public void getBestStrength(View view){
        str_end=0;
        int j=0,max_strength=0,index=0;
        int[] ints=new int[records.size()];
        for(String i:records){
            String[] str=i.split("\n");
            //                array_list.add(res.getColumnIndex(COLUMN_ID)+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_OPERATOR_NAME))+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_LAT))+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_LON))+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_SIGNAL_STRENGTH))+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_DATE_TIME)));
            ints[j]=Integer.valueOf(str[4]);
            if(ints[j]>max_strength){
                max_strength=ints[j];
                index=j;
            }
            j++;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("The best Stength");
        alertDialogBuilder.setMessage(records.get(index));

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void getWorstStrength(View view){
        str_end=0;
        int j=0,min_strength=32,index=0;
        int[] ints=new int[records.size()];
        for(String i:records){
            String[] str=i.split("\n");
            //                array_list.add(res.getColumnIndex(COLUMN_ID)+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_OPERATOR_NAME))+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_LAT))+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_LON))+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_SIGNAL_STRENGTH))+"\n"
//                        +res.getString(res.getColumnIndex(COLUMN_DATE_TIME)));
            ints[j]=Integer.valueOf(str[4]);
            if(ints[j]<min_strength){
                min_strength=ints[j];
                index=j;
            }
            j++;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("The worst Stength");
        alertDialogBuilder.setMessage(records.get(index));

        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void zoomMyMap(View view){
        if(myLocation!=null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 20));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

}

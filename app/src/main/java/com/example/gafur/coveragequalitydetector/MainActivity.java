package com.example.gafur.coveragequalitydetector;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /* This variables need to be global, so we can used them onResume and onPause method to
      stop the listener */
    TelephonyManager Tel;
    MyPhoneStateListener    MyListener;
    int str_end=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Update the listener, and start it */
        MyListener   = new MyPhoneStateListener();
        Tel       = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void startSignal(View view){
        str_end=1;
    }

    public void stopSignal(View view){
        str_end=0;
    }

    public void findOperator(View view){
        String carrierName = Tel.getNetworkOperatorName();
        Toast.makeText(getApplicationContext(), "GSM Operator Name is "
                + carrierName, Toast.LENGTH_SHORT).show();
        str_end=0;
    }

    public void recordingPhase(View view){
        startActivity(new Intent(this,SignIn.class));
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
                Toast.makeText(getApplicationContext(), "GSM Signal Strength = "
                        + signalStrength.getGsmSignalStrength(), Toast.LENGTH_SHORT).show();//0-31 arası deger donduryor
            }
        }
    }
}

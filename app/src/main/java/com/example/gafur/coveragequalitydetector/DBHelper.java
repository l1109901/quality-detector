package com.example.gafur.coveragequalitydetector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gafur on 6/4/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDB.db";
    public static final String TABLE_NAME = "records";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_OPERATOR_NAME = "op_name";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_LON = "longtitude";
    public static final String COLUMN_SIGNAL_STRENGTH = "strength";
    public static final String COLUMN_DATE_TIME = "date_time";
    private HashMap hp;

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table records " +
                        "(id integer primary key, op_name text,latitude text,longtitude text, strength integer,date_time text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS records");
        onCreate(db);
    }

    public boolean insertRecord  (String op_name, String latitude, String longtitude, int strength,String date_time)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("op_name", op_name);
        contentValues.put("latitude", latitude);
        contentValues.put("longtitude", longtitude);
        contentValues.put("strength", strength);
        contentValues.put("date_time", date_time);
        db.insert("records", null, contentValues);
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("select * from records where id =" + id + "", null);
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        return numRows;
    }

    public int selectSorgusu(double lat,double lon,int strength){
        int myid=-1;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery(
                "select id from records where latitude="
                        + String.valueOf(lat)
                        + " and longtitude="
                        + String.valueOf(lon)
                        + " and strength!="
                        + strength
                        + "", null);
        if(res.getCount()!=0){
            myid=res.getColumnIndex(COLUMN_ID);
        }
        return myid;
    }
    //buraya yeni bir select methodu yazmak lazım
    //gelecek degiskenler latitude,longtitude,strength, eger aynı sterngth var ise -1 dondursun yoksada id
    //0 donmus ise update yapmicak
    //1donmus ise bir update methodu yaz, gelecek degiskenler id,strength,date_time,donus boolean

    public boolean updateRecord (String op_name, double latitude, double longtitude, int strength,String date_time)
    {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("name", name);
//        contentValues.put("phone", phone);
//        contentValues.put("email", email);
//        contentValues.put("street", street);
//        contentValues.put("place", place);
//        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );

        int id=-1;
        id=selectSorgusu(latitude,longtitude,strength);
        if(id!=-1){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("strength", strength);
            values.put("date_time", date_time);
            db.update("records", values, "id = ? ", new String[] { Integer.toString(id) } );
            return true;
//            Cursor res =  db.rawQuery(
//                    "update records set strength="+strength
//                            +",date_time="+date_time+
//                            " where id="
//                            +id+"",null);
//            if(res!=null){
//                return true;
//            }
        }
        return false;
    }

    public Integer deleteRecord (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("records",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllRecords()
    {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from records", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getColumnIndex(COLUMN_ID)+"\n"
                            +res.getString(res.getColumnIndex(COLUMN_OPERATOR_NAME))+"\n"
                            +res.getString(res.getColumnIndex(COLUMN_LAT))+"\n"
                            +res.getString(res.getColumnIndex(COLUMN_LON))+"\n"
                            +res.getString(res.getColumnIndex(COLUMN_SIGNAL_STRENGTH))+"\n"
                            +res.getString(res.getColumnIndex(COLUMN_DATE_TIME)));
            res.moveToNext();
        }
        return array_list;
    }
}

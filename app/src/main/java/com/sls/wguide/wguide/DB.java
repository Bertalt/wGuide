package com.sls.wguide.wguide;


/**
 * Created by Sls on 21.05.2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class DB {

    private static final String DB_NAME = "dbAccessPoints";
    private static final int DB_VERSION = 1;
    public static final String ID = "_id";
    public static final String SSID = "ssid";
    public static final String BSSID = "bssid";
    public static final String LEVEL = "level";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lon";
    public static final String ENCRYPT = "encrypt";
    public static final String WHO_ADD= "who_add";
    public static final String TIME = "time";

    private final String TAG = "database";

    public ArrayList<AccessPoint> getmApList() {
        return mApList;
    }

    private ArrayList  <AccessPoint> mApList;
    public static final String TABLE_NAME = "apDnepr";

    private static final String DB_CREATE =
            "create table " + TABLE_NAME + "("
                    + ID + "  INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + BSSID + " text,"
                    + SSID + " text,"
                    + LEVEL + " int,"
                    + LATITUDE + " real,"
                    + LONGITUDE + " real,"
                    + ENCRYPT + " text,"
                    + WHO_ADD + " text,"
                    + TIME + " int);";

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // ������� �����������
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // ������� �����������
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // �������� ��� ������ �� ������� DB_TABLE
    public Cursor getAllData() {

            mApList = new ArrayList<AccessPoint>();
            Cursor c  = mDB.query(TABLE_NAME, null, null, null, null, null, null);

            if (c.moveToFirst()) {

                // ���������� ������ �������� �� ����� � �������
                int _idColIndex = c.getColumnIndex(ID);
                int bssidColIndex = c.getColumnIndex(BSSID);
                int ssidColIndex = c.getColumnIndex(SSID);
                int levelColIndex = c.getColumnIndex(LEVEL);
                int latColIndex = c.getColumnIndex(LATITUDE);
                int lonColIndex = c.getColumnIndex(LONGITUDE);
                int encryptColIndex = c.getColumnIndex(ENCRYPT);
                int whoAddColIndex = c.getColumnIndex(WHO_ADD);
                int timeColIndex = c.getColumnIndex(TIME);

                do {
                    // �������� �������� �� ������� �������� � ��������� ������
                    AccessPoint mAp = new AccessPoint();

                    mAp.setID(_idColIndex);
                    mAp.setSSID(c.getString(ssidColIndex));    //string
                    mAp.setBSSID(c.getString(bssidColIndex));  //string
                    mAp.setLevel(c.getInt(levelColIndex)); //int
                    mAp.setLat(c.getDouble(latColIndex));//double
                    mAp.setLon(c.getDouble(lonColIndex));//double
                    mAp.setEncrypt(c.getString(encryptColIndex));//string
                    mAp.setWho_add(c.getString(whoAddColIndex));
                    mAp.setTime(c.getLong(timeColIndex));

                    //��������� ������ � ������ ��������

                    mApList.add(mAp);

                    // � ���� ��������� ��� (������� - ���������), �� false - ������� �� �����
                } while (c.moveToNext());
            } else
                Log.d(TAG, "0 rows");

            Log.d(TAG, "red " + mApList.size() + "AP object(s)");

        return c;
    }

    public AccessPoint findApById(long id)
{
    for (int i=0; i<mApList.size(); i++)
        if (mApList.get(i).getID() == id)
            return mApList.get(i);

    return null;
}


    // �������� ������ � DB_TABLE
    /*
    public void addRec(String bssid, String ssid, int level, double lat, double lon, String encrypt, String who_add, long time) {

        ContentValues cv = new ContentValues();
        cv.put(BSSID, bssid);
        cv.put(SSID, ssid);
        cv.put(LEVEL, level);
        cv.put(LATITUDE, lat);
        cv.put(LONGITUDE, lon);
        cv.put(WHO_ADD, who_add);
        cv.put(ENCRYPT, encrypt);
        cv.put(TIME, time);
       long _id = mDB.insert(TABLE_NAME, null, cv);


        AccessPoint mAp = new AccessPoint();

        mAp.setID((int)_id);
        mAp.setSSID(ssid);    //string
        mAp.setBSSID(bssid);  //string
        mAp.setLevel(level);    //int
        mAp.setLat(lat);    //double
        mAp.setLon(lon);    //double
        mAp.setEncrypt(encrypt);   //string
        mAp.setWho_add(who_add);
        mAp.setTime(time);

        //��������� ������ � ������ ��������

        mApList.add(mAp);
    }
    */
    public AccessPoint getByBssid(String bssid)
    {
        for (int i =0; i < mApList.size(); i++ )
        {
            Log.d(TAG, mApList.get(i).getBSSID());
            if (mApList.get(i).getBSSID().equalsIgnoreCase(bssid))
                return mApList.get(i);
        }
        return null;
    }

    public boolean insertRec (String bssid, String ssid, int level, double lat, double lon, String encrypt, String who_add, long time) {

 try {
     ContentValues cv = new ContentValues();
     cv.put(BSSID, bssid);
     cv.put(SSID, ssid);
     cv.put(LEVEL, level);
     cv.put(LATITUDE, lat);
     cv.put(LONGITUDE, lon);
     cv.put(WHO_ADD, who_add);
     cv.put(ENCRYPT, encrypt);
     cv.put(TIME, time);

     if (apFindBssid(bssid)) {
         Toast.makeText(mCtx, ssid + "'s data was updated", Toast.LENGTH_SHORT).show();
         return mDB.update(TABLE_NAME, cv, BSSID + "=?", new String[]{bssid + ""}) > 0;
     }

     AccessPoint mAp = new AccessPoint();

     mAp.setSSID(ssid);    //string
     mAp.setBSSID(bssid);  //string
     mAp.setLevel(level);    //int
     mAp.setLat(lat);    //double
     mAp.setLon(lon);    //double
     mAp.setEncrypt(encrypt);   //string
     mAp.setWho_add(who_add);
     mAp.setTime(time);

     //��������� ������ � ������ ��������

     mApList.add(mAp);
     Toast.makeText(mCtx, ssid + " was added", Toast.LENGTH_SHORT).show();
     mDB.insert(TABLE_NAME, null, cv);

     return true;
 }
 catch (NullPointerException NPE)
 {
     NPE.printStackTrace();
     Log.e(TAG,"Caught null. (database)");
     return false;
 }
    }

    public boolean apFindBssid (String bssid)
    {
        try
        {
            for (int i =0; i < mApList.size(); i++ )
            {
                if (mApList.get(i).getBSSID().equalsIgnoreCase(bssid))
                return true;
            }

        }catch (NullPointerException ex)
        {
            ex.printStackTrace();
            Log.d(TAG, "Wait... I try fix it");
            getAllData();
            return apFindBssid(bssid);
        }
        return false;
    }


    // ������� ������ �� DB_TABLE
    public void delRec(long id) {
        mDB.delete(TABLE_NAME, ID + " = " + id, null);
    }

    // ����� �� �������� � ���������� ��
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // ������� � ��������� ��
        @Override
        public void onCreate(SQLiteDatabase db) {
           db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
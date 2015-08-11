package com.sls.wguide.wguide;

/**
 * Created by Sls on 21.05.2015.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DB {

    private static final String DB_NAME = "dbAccessPoints";
    private static final int DB_VERSION = 2;
    public static final String ID = "_id";
    public static final String SSID = "ssid";
    public static final String BSSID = "bssid";
    public static final String LEVEL = "level";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lon";
    public static final String ENCRYPT = "encrypt";
    public static final String WHO_ADD= "who_add";
    public static final String TIME = "time";
    public static final String AMOUNT_SAT = "amount_sat";
    public static final String BROADCAST_UPDATE_DB = "com.sls.wguide.updated_db";

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
                    + AMOUNT_SAT + " int,"
                    + TIME + " int);";

    private static final String ALTER_TABLE =
            "alter table "+TABLE_NAME+" add column "+ AMOUNT_SAT + " int;";

    private static final String DROP_TABLE = "drop table "+DB_NAME+"."+TABLE_NAME+";";

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {        mCtx = ctx;             }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);

        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
    open();
            mApList = new ArrayList<AccessPoint>();
            Cursor c  = mDB.query(TABLE_NAME, null, null, null, null, null, null);

            if (c.moveToFirst()) {

                // определяем номера столбцов по имени в выборке
                int _idColIndex = c.getColumnIndex(ID);
                int bssidColIndex = c.getColumnIndex(BSSID);
                int ssidColIndex = c.getColumnIndex(SSID);
                int levelColIndex = c.getColumnIndex(LEVEL);
                int latColIndex = c.getColumnIndex(LATITUDE);
                int lonColIndex = c.getColumnIndex(LONGITUDE);
                int encryptColIndex = c.getColumnIndex(ENCRYPT);
                int whoAddColIndex = c.getColumnIndex(WHO_ADD);
                int amountOfSatColIndex = c.getColumnIndex(AMOUNT_SAT);
                int timeColIndex = c.getColumnIndex(TIME);


                do {
                    // получаем значения по номерам столбцов в отдельный объект
                    AccessPoint mAp = new AccessPoint();

                    mAp.setID(_idColIndex);
                    mAp.setSSID(c.getString(ssidColIndex));    //string
                    mAp.setBSSID(c.getString(bssidColIndex));  //string
                    mAp.setLevel(c.getInt(levelColIndex)); //int
                    mAp.setLat(c.getDouble(latColIndex));//double
                    mAp.setLon(c.getDouble(lonColIndex));//double
                    mAp.setEncrypt(c.getString(encryptColIndex));//string
                    mAp.setAmountSat(c.getShort(amountOfSatColIndex));
                    mAp.setWho_add(c.getString(whoAddColIndex));
                    mAp.setTime(c.getLong(timeColIndex));

                    //добавляем объект в список объектов

                    mApList.add(mAp);

                    // а если следующей нет (текущая - последняя), то false - выходим из цикла
                } while (c.moveToNext());
            } else
                Log.d(TAG, "0 rows");

            Log.d(TAG, "red " + mApList.size() + "AP object(s)");
    close();
        return c;
    }

    public AccessPoint findApById(long id)
{
    for (int i=0; i<mApList.size(); i++)
        if (mApList.get(i).getID() == id)
            return mApList.get(i);

    return null;
}


    // добавить запись в DB_TABLE
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

        //добавляем объект в список объектов

        mApList.add(mAp);
    }
    */
    public AccessPoint getByBssid(String bssid)
    {
        try
        {

        for (int i =0; i < mApList.size(); i++ )
        {
            if (mApList.get(i).getBSSID().equalsIgnoreCase(bssid))
            {
                Log.d(TAG, mApList.get(i).getBSSID());
                return mApList.get(i);
            }

        }
        }catch (NullPointerException ex)
        {
            ex.printStackTrace();
            Log.e(TAG, "Wait... I try fix it");
            getAllData();
            return getByBssid(bssid);
        }

        return null;
    }

    public boolean insertRec (String bssid, String ssid, int level, double lat, double lon, String encrypt, String who_add,
                              int amountSat, long time) {
    open();
 try {
     ContentValues cv = new ContentValues();
     cv.put(BSSID, bssid);
     cv.put(SSID, ssid);
     cv.put(LEVEL, level);
     cv.put(LATITUDE, lat);
     cv.put(LONGITUDE, lon);
     cv.put(WHO_ADD, who_add);
     cv.put(ENCRYPT, encrypt);
     cv.put(AMOUNT_SAT, amountSat);
     cv.put(TIME, time);

     if (getByBssid(bssid) != null) {
         Log.d(TAG, ssid + "'s data was updated");
         boolean isUpdate = mDB.update(TABLE_NAME, cv, BSSID + "=?", new String[]{bssid + ""}) > 0;
         close();
      //   sendBroadcaset_update();
         return isUpdate;
     }

     AccessPoint mAp = new AccessPoint();

     mAp.setSSID(ssid);    //string
     mAp.setBSSID(bssid);  //string
     mAp.setLevel(level);    //int
     mAp.setLat(lat);    //double
     mAp.setLon(lon);    //double
     mAp.setEncrypt(encrypt);   //string
     mAp.setWho_add(who_add);
     mAp.setAmountSat(amountSat);
     mAp.setTime(time);

     //добавляем объект в список объектов

     mApList.add(mAp);
     Log.d(TAG, ssid + " was added");
     mDB.insert(TABLE_NAME, null, cv);
close();
   //  sendBroadcaset_update();
     return true;
 }
 catch (NullPointerException NPE)
 {
     NPE.printStackTrace();
     Log.e(TAG, "Caught null. (database)");
     return false;
 }
    }

    // удалить запись из DB_TABLE
        public void delRec(long id) {
        open();
        mDB.delete(TABLE_NAME, ID + " = " + id, null);
        close();
    }

    private void sendBroadcaset_update()
    {
        Intent intent = new Intent(BROADCAST_UPDATE_DB);
        mCtx.sendBroadcast(intent);
    }
    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
           db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(ALTER_TABLE);
        }
    }

}
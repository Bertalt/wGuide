package com.sls.wguide.wguide;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by Sls on 08.06.2015.
 */
public class WiFiviser extends Service {

    private String TAG = "NewScan";
    private ExecutorService es;
    private ExecutorService es2;
    private BroadcastReceiver br;


    public static final String PARAM_LON = "Longitude";
    public static final String PARAM_LAT = "Latitude";
    public final static String BROADCAST_ACTION = "com.nullxweight.servicebackbroadcast";
    public final static String BROADCAST_ACTION_SAT = "com.nullxweight.servicebackbroadcast_sat";
    private int mSatCount = 0;
    private IntentFilter intFilt;
    private IntentFilter intFilt2;
    private static LatLng mCurLoc;
    private  DB db;
    private SharedPreferences sharedPref;
    @Override
    public void onCreate()
    {

        super.onCreate();
     //   es = Executors.newFixedThreadPool(1);
        db = new DB(getApplicationContext());

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

       // scanService mScanService = new scanService();
        mCurLoc = ServiceForLocation.mCurLoc;
        mSatCount = MapsActivity.mSatCount;

            myUtil util = new myUtil();
            db.getAllData();
            mCurLoc = MapsActivity.mCurLoc;
            mSatCount = MapsActivity.mSatCount;
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {

                List<ScanResult> scanResults = wifiManager.getScanResults();
                if (scanResults != null) {
                    for (ScanResult scan : scanResults) {

                        if (scan.level < -Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_WIFI_MIN_LEVEL, "90")))
                            continue;
                        // ���������� ����� �������, ���� ������� ������� ����, ��� ������ � ���������� (��� ����)

                        //���������� �������� ����� �������, ���� � ���������� �������� "Scan all"

                        Intent mIntent = new Intent(WifiListActivity.BROADCAST_ACTION_WF);
                        mIntent.putExtra(WifiListActivity.PARAM_BSSID, scan.BSSID);
                        mIntent.putExtra(WifiListActivity.PARAM_SSID, scan.SSID);
                        mIntent.putExtra(WifiListActivity.PARAM_LEVEL, scan.level);
                        mIntent.putExtra(WifiListActivity.PARAM_ECRYPT, new myUtil().SecurTypeWiFi(scan.capabilities));
                        mIntent.putExtra(WifiListActivity.PARAM_COUNT, scanResults.size());
                        sendBroadcast(mIntent);

                        if (!sharedPref.getBoolean(SettingsActivity.KEY_PREF_SCAN_CLOSE, false))
                            if (!util.SecurTypeWiFi(scan.capabilities).equalsIgnoreCase("open"))
                                continue;
                        if (scan.level < -Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_WIFI_AUTO_MIN_LEVEL, "80")))
                            continue;

                        long mTime1 = new Date().getTime();

                        while (MapsActivity.mCurLoc == null )
                        {
                            Log.i(TAG, "I'm waiting of satellites...");
                            if (new Date().getTime() - mTime1 > 4000)
                                stopSelf();
                        }

                        if (MapsActivity.mSatCount < Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_GPS_COUNT_SAT, "6")))
                            continue;

                        //���. ����� ���������� ����� �������, ���� �������� ������������ ���������

                        {
                            AccessPoint tmp = db.getByBssid(scan.BSSID);
                            if (tmp == null) {
                                db.insertRec(scan.BSSID,
                                        scan.SSID,
                                        scan.level,
                                        mCurLoc.latitude,
                                        mCurLoc.longitude,
                                        util.SecurTypeWiFi(scan.capabilities),
                                        "S",
                                        mSatCount,
                                        new Date().getTime());
                            } else {
                                if (tmp.getLevel() < scan.level)
                                    db.insertRec(scan.BSSID,
                                            scan.SSID,
                                            scan.level,
                                            mCurLoc.latitude,
                                            mCurLoc.longitude,
                                            util.SecurTypeWiFi(scan.capabilities),
                                            "S",
                                            mSatCount,
                                            new Date().getTime());
                            }
                        }
                    }
                }
            } else
                Log.d(TAG, "WiFi Disable");


       // es.execute(mScanService);
        return super.onStartCommand(intent,flags,startId);
    }

    public void onDestroy() {


        super.onDestroy();
        Log.d(TAG, "scanService onDestroy");
        super.onDestroy();
    }
    /*
    class scanService implements Runnable
    {
        @Override
            public void run() {

            try {
                myUtil util = new myUtil();
                db.getAllData();
                mCurLoc = MapsActivity.mCurLoc;
                mSatCount = MapsActivity.mSatCount;
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {

                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    if (scanResults != null) {
                        for (ScanResult scan : scanResults) {

                            if (scan.level < -Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_WIFI_MIN_LEVEL, "90")))
                                continue;
                            // ���������� ����� �������, ���� ������� ������� ����, ��� ������ � ���������� (��� ����)

                            //���������� �������� ����� �������, ���� � ���������� �������� "Scan all"

                            Intent intent = new Intent(WifiListActivity.BROADCAST_ACTION_WF);
                            intent.putExtra(WifiListActivity.PARAM_BSSID, scan.BSSID);
                            intent.putExtra(WifiListActivity.PARAM_SSID, scan.SSID);
                            intent.putExtra(WifiListActivity.PARAM_LEVEL, scan.level);
                            intent.putExtra(WifiListActivity.PARAM_ECRYPT, new myUtil().SecurTypeWiFi(scan.capabilities));
                            intent.putExtra(WifiListActivity.PARAM_COUNT, scanResults.size());
                            sendBroadcast(intent);

                            if (!sharedPref.getBoolean(SettingsActivity.KEY_PREF_SCAN_CLOSE, false))
                                if (!util.SecurTypeWiFi(scan.capabilities).equalsIgnoreCase("open"))
                                    continue;
                            if (scan.level < -Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_WIFI_AUTO_MIN_LEVEL, "80")))
                                continue;

                            long mTime1 = new Date().getTime();

                            while (MapsActivity.mCurLoc == null )
                            {
                                Log.i(TAG, "I'm waiting of satellites...");
                                if (new Date().getTime() - mTime1 > 4000)
                                    stopSelf();
                            }

                            if (MapsActivity.mSatCount < Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_GPS_COUNT_SAT, "6")))
                                continue;

                            //���. ����� ���������� ����� �������, ���� �������� ������������ ���������

                            {
                                AccessPoint tmp = db.getByBssid(scan.BSSID);
                                if (tmp == null) {
                                    db.insertRec(scan.BSSID,
                                            scan.SSID,
                                            scan.level,
                                            mCurLoc.latitude,
                                            mCurLoc.longitude,
                                            util.SecurTypeWiFi(scan.capabilities),
                                            "S",
                                            mSatCount,
                                            new Date().getTime());
                                } else {
                                    if (tmp.getLevel() < scan.level)
                                        db.insertRec(scan.BSSID,
                                                scan.SSID,
                                                scan.level,
                                                mCurLoc.latitude,
                                                mCurLoc.longitude,
                                                util.SecurTypeWiFi(scan.capabilities),
                                                "S",
                                                mSatCount,
                                                new Date().getTime());
                                }
                            }
                        }
                    }
                } else
                    Log.d(TAG, "WiFi Disable");
            }catch (NullPointerException NPE)
            {
                NPE.printStackTrace();
                stopSelf();
            }
        stopSelf();
        }

    }
*/
}

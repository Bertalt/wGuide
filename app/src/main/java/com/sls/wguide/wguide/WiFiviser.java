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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sls on 08.06.2015.
 */
public class WiFiviser extends Service {

    private String TAG = "NewScan";
    private ExecutorService es;
    private ExecutorService es2;
    private BroadcastReceiver br;
    private BroadcastReceiver br_sat;
    public static final String PARAM_SAT  = "satellites";
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
        es = Executors.newFixedThreadPool(1);
        es2 = Executors.newFixedThreadPool(1);
        db = new DB(getApplicationContext());

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        scanService mScanService = new scanService();
        broadcastListener mBroadcastListener = new broadcastListener();

        es.execute(mScanService);
        es2.execute(mBroadcastListener);
        return super.onStartCommand(intent,flags,startId);
    }

    public void onDestroy() {
        try{
            unregisterReceiver(br_sat);
            unregisterReceiver(br);

        }catch (NullPointerException NPE)
        {
            NPE.printStackTrace();
        }
        catch(RuntimeException RE)
        {
            super.onDestroy();
        }
        Log.d(TAG, "scanService onDestroy");
        super.onDestroy();
    }
    class scanService implements Runnable
    {
        @Override
        public void run() {

            myUtil util = new myUtil();
            db.getAllData();
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {

                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    if (scanResults != null) {
                        for (ScanResult scan : scanResults) {

                            if (scan.level< -Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_WIFI_MIN_LEVEL, "90")))
                                continue;
                            // игнорирует точку доступа, если уровень сигнала ниже, чем указан в настройках (дл€ всех)

                            //игнорирует закрытые точки доступа, если в настройках отключен "Scan all"

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
                     if(scan.level < -Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_WIFI_AUTO_MIN_LEVEL, "80")))
                                continue;

                            while(mCurLoc == null)
                                Log.i(TAG, "I'm waiting of satellites...");
                            if (mSatCount < Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_GPS_COUNT_SAT, "6")))
                                continue;

                            //авт. часть игнорирует точку доступа, если доступно недостаточно спутников

                            {
                              AccessPoint tmp =  db.getByBssid(scan.BSSID);
                                if (tmp == null)
                                {
                                    db.insertRec( scan.BSSID,
                                            scan.SSID,
                                            scan.level,
                                            mCurLoc.latitude,
                                            mCurLoc.longitude,
                                            util.SecurTypeWiFi(scan.capabilities),
                                            "S",
                                            new Date().getTime());
                                }
                                else
                                {
                                    if (tmp.getLevel() < scan.level)
                                        db.insertRec( scan.BSSID,
                                                scan.SSID,
                                                scan.level,
                                                mCurLoc.latitude,
                                                mCurLoc.longitude,
                                                util.SecurTypeWiFi(scan.capabilities),
                                                "S",
                                                new Date().getTime());
                                }
                            }
                        }
                    }
                }
                else
                Log.d(TAG,"WiFi Disable") ;

        stopSelf();
        }

    }

    class broadcastListener implements Runnable {
        @Override
        public void run() {

            br = new BroadcastReceiver() {
                // действи€ при получении сообщений
                public void onReceive(Context context, Intent intent) {
                    double mLongitude = intent.getDoubleExtra(PARAM_LON, 0);
                    double mLatitude = intent.getDoubleExtra(PARAM_LAT, 0);
                    Log.d(TAG, "onReceive: Lat = " + mLatitude + ", Lon = " + mLongitude);
                    mCurLoc = new LatLng(mLatitude, mLongitude);
                }
            };
            // создаем объект дл€ создани€ и управлени€ верси€ми Ѕƒ
            // создаем фильтр дл€ BroadcastReceiver
            intFilt = new IntentFilter(BROADCAST_ACTION);

            br_sat = new BroadcastReceiver() {
                // действи€ при получении сообщений
                public void onReceive(Context context, Intent intent) {

                    mSatCount = intent.getIntExtra(PARAM_SAT, 0);

                    Log.d(TAG, "onReceive: satellites =  " + mSatCount);
                }
            };
            // создаем фильтр дл€ BroadcastReceiver
            intFilt2 = new IntentFilter(BROADCAST_ACTION_SAT);
try {
     if (br_sat.isInitialStickyBroadcast())
         unregisterReceiver(br_sat);
    registerReceiver(br_sat, intFilt2);
    if (br.isInitialStickyBroadcast())
        unregisterReceiver(br);
    registerReceiver(br, intFilt);
}catch (Exception ex)
{
    ex.printStackTrace();
    stopSelf();
}
        }
    }
}

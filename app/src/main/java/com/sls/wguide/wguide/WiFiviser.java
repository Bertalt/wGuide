package com.sls.wguide.wguide;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

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
    @Override
    public void onCreate()
    {

        super.onCreate();
        es = Executors.newFixedThreadPool(2);

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
    es.execute(mBroadcastListener);

        return super.onStartCommand(intent,flags,startId);
    }

    public void onDestroy() {
        super.onDestroy();

        try{
            unregisterReceiver(br_sat);
            unregisterReceiver(br);

        }catch (NullPointerException NPE)
        {
            NPE.printStackTrace();
        }
        Log.d(TAG, "scanService onDestroy");
    }
    class scanService implements Runnable
    {
        @Override
        public void run() {

                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {

                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    if (scanResults != null) {
                        for (ScanResult scan : scanResults) {
                            Log.i(TAG,
                                    "\n SSID: " + scan.SSID     //string
                                            + "\n BSSID: " + scan.BSSID   //string
                                            + "\n level: " + scan.level  //int
                                            //  + "\n Lat: " + mCurLoc.latitude//double
                                            //  + "\n Lon: " + mCurLoc.longitude//double
                                            + "\n Security: " + new myUtil().SecurTypeWiFi(scan.capabilities)//string
                                            + "\n time: " + (new Date().getTime()));//long
                            Intent intent = new Intent(WifiListActivity.BROADCAST_ACTION_WF);
                            intent.putExtra(WifiListActivity.PARAM_BSSID, scan.BSSID);
                            intent.putExtra(WifiListActivity.PARAM_SSID, scan.SSID);
                            intent.putExtra(WifiListActivity.PARAM_LEVEL, scan.level);
                            intent.putExtra(WifiListActivity.PARAM_ECRYPT, new myUtil().SecurTypeWiFi(scan.capabilities));
                            intent.putExtra(WifiListActivity.PARAM_COUNT, scanResults.size());
                            sendBroadcast(intent);


                        }
                    }
                }
                else
                Log.d(TAG,"WiFi Disable") ;
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            //}// end of while

        stopSelf();
        }

    }

    class broadcastListener implements Runnable {
        @Override
        public void run()
        {
            br = new BroadcastReceiver() {
                // действия при получении сообщений
                public void onReceive(Context context, Intent intent) {

                    double mLongitude = intent.getDoubleExtra(PARAM_LON, 0);
                    double mLatitude = intent.getDoubleExtra(PARAM_LAT, 0);
                    Log.d(TAG, "onReceive: Lat = " + mLatitude + ", Lon = " + mLongitude);
                    mCurLoc = new LatLng(mLatitude, mLongitude);
                }
            };
            // создаем объект для создания и управления версиями БД
            // создаем фильтр для BroadcastReceiver
            intFilt = new IntentFilter(BROADCAST_ACTION);

            br_sat = new BroadcastReceiver() {
                // действия при получении сообщений
                public void onReceive(Context context, Intent intent) {

                    mSatCount = intent.getIntExtra(PARAM_SAT, 0);

                    Log.d(TAG, "onReceive: satellites =  " + mSatCount);
                }
            };
            // создаем фильтр для BroadcastReceiver
            intFilt2 = new IntentFilter(BROADCAST_ACTION_SAT);

            registerReceiver(br_sat, intFilt2);
            registerReceiver(br, intFilt);

        }
    }
}

package com.sls.wguide.wguide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class MapsActivity extends ActionBarActivity
         {
             private GoogleMap mMap; // Might be null if Google Play services APK (GPsA) is not available
    private Marker mMarkerCurrentPos;
    private ScheduledExecutorService serviceWiFi;
    private boolean mLocationAccuracy = false;
    public final static String BROADCAST_ACTION = "com.nullxweight.servicebackbroadcast";
    private BroadcastReceiver br;
    private BroadcastReceiver br_update_map;
    private static final String TAG = "Map";

             public static LatLng mCurLoc;
             public static int mSatCount;
             private LocationManager lm;
             private  GpsStatusListener GSL;
             private Handler handler;
             private Intent mIntent;
             private Double mLat;
             private Double mLon;
             private boolean mServiceRunState = false;
             private Thread mServiceWithTimer;
             private Circle mCircleActivity;
             private float mAvaRadius;
             Timer myTimer;
    private Button bCurrentPos;
    private TextView tvStatCount;
         //    private boolean mScannerActiveted = false;
             private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        serviceWiFi = Executors.newSingleThreadScheduledExecutor();
        myTimer = new Timer();
        mServiceWithTimer = new Thread(new RunServiceWithTimer());
        if(savedInstanceState == null)
        Log.d("Test", "FIRST START OF ACTIVITY");
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.d("LifeCycle", "onCreate()");
        bCurrentPos = (Button) findViewById(R.id.bCurrentPos);

        tvStatCount = (TextView) findViewById(R.id.tvStatCount);

        GSL =  new GpsStatusListener(tvStatCount);


        startService(new Intent(this, ServiceForLocation.class).putExtra("Accuracy", true));

        // создаем BroadcastReceiver
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {

                mCurLoc = ServiceForLocation.mCurLoc;
                Log.d(TAG, "onReceive new location: Lat = " + mCurLoc.latitude + ", Lon = " + mCurLoc.longitude);
                if (mMarkerCurrentPos != null)
                    mMarkerCurrentPos.remove();

                mMarkerCurrentPos =mMap.addMarker(newMarkerMyPosition(mCurLoc));
                if (mCircleActivity != null && mCircleActivity.isVisible())
                    mCircleActivity.remove();

                if (mMap!= null)
                    mCircleActivity = mMap.addCircle(new CircleOptions()
                            .center(mCurLoc)
                            .radius(mAvaRadius)
                            .strokeColor(Color.LTGRAY)
                            .strokeWidth(3)     // in pixels
                    );

            }
        };
        br_update_map = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                mMap.clear();
                new FillInMap(getApplicationContext(), mMap).start();
            }
        };
        IntentFilter intFilt_update_map = new IntentFilter(DB.BROADCAST_UPDATE_DB);
        registerReceiver(br_update_map, intFilt_update_map);
/*
            ////// Регистрация слушателя доступных спутников
 */
        handler = new Handler(Looper.getMainLooper());
        lm.addGpsStatusListener(GSL);

            ////// Переодическое выполнение сервиса сканирования wireless

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);
    }
             @Override
             protected void onNewIntent(Intent intent) {
                 super.onNewIntent(intent);
                 setIntent(intent);

                 mLat = intent.getDoubleExtra("lat", 48.35);
                 mLon = intent.getDoubleExtra("lon", 31.16);
                 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLon), 15.5f));

                 // something you want
             }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LifeCycle", "onResume()");
       setUpMapIfNeeded();


        mLocationAccuracy = sharedPref.getBoolean(SettingsActivity.KEY_PREF_MODE, false);
        mAvaRadius = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_MAP_AVA_RADIUS, "1000"));
        if (mMap != null)
        mMap.clear();

        new FillInMap(this, mMap).start();//наполнение карты маркерами wifi точек


        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS)
            Toast.makeText(this, "Google Play service failed", Toast.LENGTH_SHORT).show();

        //showDialog();
    }


             @Override
             protected void onStart() {
        super.onStart();
        Log.d("LifeCycle", "onStart()");

            if ( !mServiceWithTimer.isAlive() && sharedPref.getBoolean(SettingsActivity.KEY_PREF_MODE, false) ) //
            {

                myTimer.cancel();
                myTimer = new Timer();
                mServiceWithTimer = new Thread(new RunServiceWithTimer());
                mServiceWithTimer.start();

            }
        if (!sharedPref.getBoolean(SettingsActivity.KEY_PREF_MODE, false))// остановка сервиса, если настройки изменились
        {
            myTimer.cancel();
        }

    }

             @Override
             protected void onStop() {
        super.onStop();
        Log.d("LifeCycle", "onStop()");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LifeCycle", "onDestroy()");
        // дерегистрируем (выключаем) BroadcastReceiver
        unregisterReceiver(br);
        unregisterReceiver(br_update_map);
        lm.removeGpsStatusListener(GSL);
        stopService(new Intent(this, ServiceForLocation.class));
        stopService(new Intent(this, WiFiviser.class));
      //  mScannerActiveted = false;
    }
    private boolean setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            if (mMap == null) {
                Log.e(TAG, "Can not find map");
                return false;
            }

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }

        if  (bCurrentPos!= null)
            bCurrentPos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    goCurrentLocation(mMap);
                }
            });

        return true;

    }
    private void setUpMap() {
        mIntent = getIntent();
        double mLat = mIntent.getDoubleExtra("lat", 48.35);
        double mLon = mIntent.getDoubleExtra("lon",31.16);
        float mZoom = 5f;
        if  (mLat!= 48.35 && mLon != 31.16)
            mZoom =15.5f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLon), mZoom));
        UiSettings UImap = mMap.getUiSettings();    // настроиваем ПИ(польз. интерефейс)
        UImap.setCompassEnabled(false);             // включаем компас
        UImap.setMyLocationButtonEnabled(false);     //кнопка текущего местоположения
        UImap.setZoomControlsEnabled(true);         // кнопки зума



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_wifiList)
        {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_recommend_on_wifi), Toast.LENGTH_SHORT).show();
                return true;
            }
            else if (!mLocationAccuracy)
            {
                Toast.makeText(getApplicationContext(), getString(R.string.toast_recommend_on_search), Toast.LENGTH_SHORT).show();
                return true;
            }
            startActivity(new Intent(this, WifiListActivity.class));
            return true;
        }
        if (id == R.id.action_list_from_db)
        {
            startActivity(new Intent(this, ListFromDb.class));
            return true;
        }
        if (id == R.id.action_update_map) {
            mMap.clear();
            new FillInMap(this, mMap).start();//наполнение карты маркерами wifi точек


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mMap != null)
            ;
        else
            Toast.makeText(this, "map = null", Toast.LENGTH_SHORT).show();
    }




    private void goCurrentLocation (GoogleMap _mMap)  //move camera on current user's coordinates
    {
        if (_mMap != null && mCurLoc != null)
        {
            _mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurLoc, 15.5f));
            if (mMarkerCurrentPos!= null)
            {
                mMarkerCurrentPos.remove();
                mMarkerCurrentPos =_mMap.addMarker(newMarkerMyPosition(mCurLoc));

            }
        }
    }


    private MarkerOptions newMarkerMyPosition (LatLng mCurrentPosition)     //set up marker to current position
    {
        return new MarkerOptions()
                .position(mCurrentPosition)
                .title(getString(R.string.title_user_here))
                .icon(BitmapDescriptorFactory
                        .fromBitmap(BitmapFactory
                                .decodeResource(this.getResources(), R.mipmap.ic_loc)))
                .draggable(true);
    }






                     boolean doubleBackToExitPressedOnce = false;
             @Override
             public void onBackPressed() {
                 if (doubleBackToExitPressedOnce) {
                     super.onBackPressed();

                     android.os.Process.killProcess(android.os.Process.myPid());
                     stopService(new Intent(this, WiFiviser.class));
                     return;
                 }
                 doubleBackToExitPressedOnce = true;

                 Toast.makeText(this, getString(R.string.toast_back_exit), Toast.LENGTH_SHORT).show();

                 new Handler().postDelayed(new Runnable() {

                     @Override
                     public void run() {
                         doubleBackToExitPressedOnce=false;
                     }
                 }, 3000);
             }

             public class RunServiceWithTimer implements Runnable{

                 @Override
                 public void run() {
                     {
                         mServiceRunState = true;
                         myTimer.schedule(new TimerTask() {
                             @Override
                             public void run() {
                                 startService(new Intent(getApplicationContext(), WiFiviser.class));
                             }
                         }, 0, 5 * 1000);
                     }
                     }
                 }

             public class GpsStatusListener implements  android.location.GpsStatus.Listener
             {

                 TextView mTvStatCount;
                 public GpsStatusListener (TextView mTvStatCount)
                 {
                     this.mTvStatCount = mTvStatCount;
                 }

                 @Override
                 public void onGpsStatusChanged(int event) {
                     handler.post(new Runnable() {
                         public void run() {

                             int satellites = 0;
                             int satellitesInFix = 0;
                             int timetofix = lm.getGpsStatus(null).getTimeToFirstFix();
                            // Log.i(TAG, "Time to first fix = " + String.valueOf(timetofix)); //время на подключение к достат. кол-ву спутников

                             for (GpsSatellite sat : lm.getGpsStatus(null).getSatellites()) {
                                 if (sat.usedInFix()) {
                                     satellitesInFix++;     //подсчет кол-ва спутников, которые учавствуют в
                                 }
                                 satellites++;
                             }
                           //  Log.i(TAG, String.valueOf(satellites) + " Used In Last Fix (" + satellitesInFix + ")");

                             if (satellitesInFix < Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_GPS_COUNT_SAT, "6")))
                                 mTvStatCount.setTextColor(Color.RED);
                             else
                                 mTvStatCount.setTextColor(Color.GREEN);

                             mTvStatCount.setText("" + satellitesInFix);
                             mSatCount = satellitesInFix;
                         }
                     });

                 }

             }
             }


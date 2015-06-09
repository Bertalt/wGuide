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
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.ExecutorService;


public class MapsActivity extends ActionBarActivity
         {
             private GoogleMap mMap; // Might be null if Google Play services APK (GPsA) is not available
    private Marker mMarkerCurrentPos;
    private Switch mSwitchMod  =null;
    private boolean mLocationAccuracy = false;
    public final static String BROADCAST_ACTION = "com.nullxweight.servicebackbroadcast";
    private BroadcastReceiver br;
    private static final String TAG = "Map";
             private ExecutorService mExecutorService;
             public static final String PARAM_LON = "Longitude";
             public static final String PARAM_LAT = "Latitude";
             private LatLng mCurLoc;
             private LocationManager lm;
             private  GpsStatusListener GSL;
             private Handler mHandler;
             private Intent mIntent;
             private Double mLat;
             private Double mLon;
    private Button bCurrentPos;
    private TextView tvStatCount;
         //    private boolean mScannerActiveted = false;
             private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        setUpMapIfNeeded();


        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.d("LifeCycle", "onCreate()");
        bCurrentPos = (Button) findViewById(R.id.bCurrentPos);
        mSwitchMod = (Switch) findViewById(R.id.switch_search_mod);
        tvStatCount = (TextView) findViewById(R.id.tvStatCount);

        GSL =  new GpsStatusListener(tvStatCount);

        mLocationAccuracy = sharedPref.getBoolean(SettingsActivity.KEY_PREF_MODE, false);
        startService(new Intent(this, ServiceForLocation.class).putExtra("Accuracy", true));
        startService(new Intent(this, WiFiviser.class));


        mSwitchMod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mLocationAccuracy = !mLocationAccuracy;
                mSwitchMod.setChecked(mLocationAccuracy);
                //createLocationRequest();
/*
                if (mLocationAccuracy)
                {
                    startService(new Intent(getApplicationContext(), WiFiScanner00.class));
                }

                else
                {
                    stopService(new Intent(getApplicationContext(), WiFiScanner00.class));
                   lm.removeGpsStatusListener(GSL);
                }
*/
                Log.d("Switch", "Search mod " + mLocationAccuracy);

            }
        });


        // ������� BroadcastReceiver
        br = new BroadcastReceiver() {
            // �������� ��� ��������� ���������
            public void onReceive(Context context, Intent intent) {
                double mLongitude = intent.getDoubleExtra(PARAM_LON, 0);
                double mLatitude = intent.getDoubleExtra(PARAM_LAT, 0);
                mCurLoc = new LatLng(mLatitude, mLongitude);
                Log.d(TAG, "onReceive: Lat = " + mLatitude + ", Lon = " + mLongitude);
                if (mMarkerCurrentPos != null)
                    mMarkerCurrentPos.remove();

                mMarkerCurrentPos =mMap.addMarker(newMarkerMyPosition(mCurLoc));

            }
        };

        // ������� ������ ��� BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // ������������ (��������) BroadcastReceiver
        registerReceiver(br, intFilt);
    }
             @Override
             protected void onNewIntent(Intent intent) {
                 super.onNewIntent(intent);
                 setIntent(intent);


                 // something you want
             }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LifeCycle", "onResume()");
        setUpMapIfNeeded();

        mMap.clear();
        new FillInMap(this, mMap).start();//���������� ����� ��������� wifi �����


        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS)
            Toast.makeText(this, "Google Play service failed", Toast.LENGTH_SHORT).show();
        mIntent = getIntent();
        mLat = mIntent.getDoubleExtra("lat", 48.35);
        mLon = mIntent.getDoubleExtra("lon", 31.16);
        if  (mLat== 48.35 && mLon == 31.16)
        goCurrentLocation(mMap);
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLon), 15.5f));
        mSwitchMod.setChecked(mLocationAccuracy);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("LifeCycle", "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("LifeCycle", "onStop()");
        if (mExecutorService != null)
            mExecutorService.shutdownNow();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("LifeCycle", "onDestroy()");
        // �������������� (���������) BroadcastReceiver
        unregisterReceiver(br);
        lm.removeGpsStatusListener(GSL);
        stopService(new Intent(this, ServiceForLocation.class));
        stopService(new Intent(this, WiFiviser.class));
      //  mScannerActiveted = false;
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();


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


    }


    private void setUpMap() {
        mIntent = getIntent();
        double mLat = mIntent.getDoubleExtra("lat", 48.35);
        double mLon = mIntent.getDoubleExtra("lon",31.16);
        float mZoom = 5f;
        if  (mLat!= 48.35 && mLon != 31.16)
            mZoom =15.5f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat, mLon), mZoom));
        UiSettings UImap = mMap.getUiSettings();    // ����������� ��(�����. ����������)
        UImap.setCompassEnabled(false);             // �������� ������
        UImap.setMyLocationButtonEnabled(false);     //������ �������� ��������������
        UImap.setZoomControlsEnabled(true);         // ������ ����


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
                Toast.makeText(getApplicationContext(), "You should enable WiFi", Toast.LENGTH_SHORT).show();
                return true;
            }
            else if (!mLocationAccuracy)
            {
                Toast.makeText(getApplicationContext(), "You should change mod on Search", Toast.LENGTH_SHORT).show();
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
                .title("I'm here!")
                .icon(BitmapDescriptorFactory
                        .fromBitmap(BitmapFactory
                                .decodeResource(this.getResources(), R.mipmap.ic_launcher)))
                .draggable(true);
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

                             int satellites = 0;
                             int satellitesInFix = 0;
                             int timetofix = lm.getGpsStatus(null).getTimeToFirstFix();
                             Log.i(TAG, "Time to first fix = "+String.valueOf(timetofix)); //����� �� ����������� � ������. ���-�� ���������

                             for (GpsSatellite sat : lm.getGpsStatus(null).getSatellites()) {
                                 if(sat.usedInFix()) {
                                     satellitesInFix++;     //������� ���-�� ���������, ������� ���������� �
                                 }
                                 satellites++;
                             }
                     Log.i(TAG, String.valueOf(satellites) + " Used In Last Fix (" + satellitesInFix + ")");

                             if (satellitesInFix < Integer.parseInt(sharedPref.getString(SettingsActivity.KEY_PREF_GPS_COUNT_SAT, "6")))
                                 mTvStatCount.setTextColor(Color.RED);
                             else
                                 mTvStatCount.setTextColor(Color.GREEN);

                             mTvStatCount.setText(""+ satellitesInFix);

                             Intent intent = new Intent(WifiListActivity.BROADCAST_ACTION);
                             intent.putExtra(WifiListActivity.PARAM_SAT, satellitesInFix );

                             sendBroadcast(intent);
                 }

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

                 Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                 new Handler().postDelayed(new Runnable() {

                     @Override
                     public void run() {
                         doubleBackToExitPressedOnce=false;
                     }
                 }, 3000);
             }

         }
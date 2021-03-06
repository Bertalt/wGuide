package com.sls.wguide.wguide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import static com.google.android.gms.location.LocationServices.API;
import static com.google.android.gms.location.LocationServices.FusedLocationApi;

/**
 * Created by  on 12.05.2015.
 */
public class ServiceForLocation extends Service
        implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
       // android.location.GpsStatus.Listener
         {
    String LOG_TAG = "myServiceGPS";
    iLocationListener LocLis;
    LocationManager lm;

    private GoogleApiClient mGoogleApiClient;   //obj for GPsA
    private LocationRequest mLocationRequest;
    private PendingResult<Status> mUpdateLocStatus;
    private boolean mLocationAccuracy =false;
    public static LatLng mCurLoc;

    public void onCreate() {

        super.onCreate();
        Log.d(LOG_TAG, "MyService onCreate");
    //   es = Executors.newFixedThreadPool(1);

        mGoogleApiClient = new GoogleApiClient.Builder(this)    // setup API
                .addApi(API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        lm = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

    }



    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        Log.d(LOG_TAG, "MyService onDestroy");
        if (LocLis!=null && mGoogleApiClient.isConnected())
            FusedLocationApi.removeLocationUpdates(mGoogleApiClient, LocLis);

        //lm.removeGpsStatusListener(this);
      // es.shutdownNow();
    }

    // interfaces for Google API
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            Log.d(LOG_TAG, "MyService onStartCommand");
            mLocationAccuracy = intent.getBooleanExtra("Accuracy", false);// get boolean to settings PRIORITY
            mGoogleApiClient.connect();

            return super.onStartCommand(intent, flags, startId);
        }catch(NullPointerException ex) {
            Log.e(LOG_TAG, "Probably main activity was destroyed.");
            super.onDestroy();
            return -1;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("GPsA", "Google Play Service connected2");
        createLocationRequest();
        Location mLastLocation = FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null)
        {
            Log.d("GPsA", "Lat: "+String.valueOf(
                            mLastLocation.getLatitude())
            );
            Log.d("GPsA", "Lon: " + String.valueOf(
                            mLastLocation.getLongitude())
            );

        }
        else {
            Log.d("GPsA", "Unknown last coordinates");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d("GPsA", " Google Play Service interrupted");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d("GPsA", " Google Play Service failed");
    }



    protected void createLocationRequest() {

        Log.d("GPsA", " Create LocUP");
        if (mLocationAccuracy)
        {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);// use GPS
        }
        else
        {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);// use NETWORK

        }


        startLocationUpdates();

    }

    protected void startLocationUpdates() {

        LocLis = new iLocationListener();
        Log.d("GPsA", "StartLocUP");

        if (mUpdateLocStatus == null)
        {
             // interface for update location
            mUpdateLocStatus = FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, LocLis);
            Log.d("GPsA", "first start");
        }
        else
        {
            FusedLocationApi.removeLocationUpdates(mGoogleApiClient, LocLis);
            mUpdateLocStatus = FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, LocLis);
            Log.d("GPsA", "second start");
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


             public class iLocationListener implements LocationListener
    {

        @Override
            public void onLocationChanged(Location location) {  //extend in cycle  in order with Interval (n)
            Log.d("POS", "Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());

            mCurLoc = new LatLng(location.getLatitude(), location.getLongitude());

            sendBroadcast( new Intent(MapsActivity.BROADCAST_ACTION));
        }
    }
}

package com.sls.wguide.wguide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Date;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by Sls on 01.06.2015.
 */
public class FillInMap extends Thread implements Runnable, GoogleMap.OnMarkerDragListener {

    private DB db;
    private ArrayList<AccessPoint> alAccessPoints;
    private Context context;
    private GoogleMap mMap;
    private ArrayList<Marker> mMarkerList;
    private String TAG = "FillInMap";
    private double mRadiusInLanLng = 0.00000960865339; // = 1 m
    private float mAvaRadius;
    private SharedPreferences sharedPref;


    public FillInMap (Context context, GoogleMap Map)
    {
        this.context = context;
        db = new DB(context);

        mMap = Map;
        mMarkerList = new ArrayList<>();
        mMap.setOnMarkerDragListener(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mAvaRadius = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_MAP_AVA_RADIUS, "1000"));


    }
    @Override
    public void run() {

        final Handler handler = new Handler(Looper.getMainLooper());
        db.getAllData();
        int count = 0;
        alAccessPoints = db.getmApList();

        for (int i = 0; i < alAccessPoints.size(); i++) {
            final int n = i;
            handler.post(new Runnable() {
                public void run() {
                    AccessPoint tmp = alAccessPoints.get(n);

                    Log.d("Map", "marker = "+tmp.getLat()+" lon = "+ tmp.getLon());
                   mMarkerList.add(n, mMap.addMarker(new MarkerOptions()           //добавлеие маркера на карту + в список маркеров
                           .position(new LatLng(tmp.getLat(), tmp.getLon()))
                           .title(tmp.getSSID())
                           .icon(BitmapDescriptorFactory
                                   .fromBitmap(BitmapFactory
                                           .decodeResource(context.getResources(), selectWifiMarker(tmp.getLevel()
                                                   , tmp.getEncrypt()))))
                           .snippet("Signal: " + tmp.getLevel()              //описание точки по нажатию на маркер
                                   + "  " + tmp.getEncrypt())
                           .visible((getVector(ServiceForLocation.mCurLoc,
                                   new LatLng(tmp.getLat(), tmp.getLon()))))
                                   .draggable(true)));


                }
            });
            count++;
        }
        Log.d(TAG, "Was load " + count + " WiFi markers");
         }

    private int selectWifiMarker(int signal, String encrypt)
    {
        int m4l = -60;
        if (encrypt.equalsIgnoreCase("open")) {
            if (signal >= m4l)
                return R.mipmap.ic_wifi_launcher;
            else if (signal >= m4l - 10)
                return R.mipmap.ic_wifi_launcher_bmid;
            else if (signal >= m4l - 20)
                return R.mipmap.ic_wifi_launcher_lmid;
            else if (signal >= m4l - 30)
                return R.mipmap.ic_wifi_launcher_bottom;

            return R.mipmap.ic_wifi_launcher;
        }

        else
            if (signal >= m4l)
            return R.mipmap.ic_wifi_launcher_lock;
        else if (signal >= m4l-10)
            return R.mipmap.ic_wifi_launcher_bmid_lock;
        else if(signal >= m4l-20)
            return R.mipmap.ic_wifi_launcher_lmid_lock;
        else if (signal >=m4l-30)
            return R.mipmap.ic_wifi_launcher_bottom_lock;

        return R.mipmap.ic_wifi_launcher;

    }


    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        AccessPoint tmp = getApByMarkerId(marker.getId());
        if (tmp != null)
        {
           if (db.insertRec(tmp.getBSSID(),
                    tmp.getSSID(),
                    tmp.getLevel(),
                    marker.getPosition().latitude,
                    marker.getPosition().longitude,
                    tmp.getEncrypt(),
                    tmp.getWho_add(),
                   tmp.getAmountSat(),
                    new Date().getTime()))
               Toast.makeText(context, tmp.getSSID()+ " location changed "+marker.getPosition().latitude+" "+ marker.getPosition().longitude, Toast.LENGTH_SHORT).show();

            marker.setVisible(getVector(ServiceForLocation.mCurLoc,
                    new LatLng(marker.getPosition().latitude,marker.getPosition().longitude)));
        }

    }
    public  boolean getVector ( LatLng a ,  LatLng b)
    {
        if (a == null)
            return true;
        if (b == null)
            return false;

        LatLng AB = new LatLng(b.latitude - a.latitude, b.longitude - a.longitude);
        double radius = sqrt(pow(AB.latitude, 2) + pow(AB.longitude, 2));
        // Log.d(TAG, "Radius = " + radius);
        if (radius > mRadiusInLanLng*mAvaRadius)
            return false;
        return true;
    }

    private AccessPoint getApByMarkerId (String id)
    {
        for (int i = 0; i<mMarkerList.size(); i++)
            if (mMarkerList.get(i).getId().equalsIgnoreCase(id))
                return alAccessPoints.get(i);

        return null;

    }

    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }


}

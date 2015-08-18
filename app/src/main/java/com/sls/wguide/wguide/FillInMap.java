package com.sls.wguide.wguide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
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

/**
 * Created by Sls on 01.06.2015.
 */
public class FillInMap extends Thread implements Runnable, GoogleMap.OnMarkerDragListener {

    private DB db;
    private ArrayList<AccessPoint> alAccessPoints;
    private Context context;
    private GoogleMap mMap;
    private ArrayList<Marker> mMarkerList;
    private ClusterManager<MyItem> mClusterManager;
    private String TAG = "FillInMap";

    private BroadcastReceiver br;
    public static final String PARAM_LON = "Longitude";
    public static final String PARAM_LAT = "Latitude";
    public final static String BROADCAST_ACTION = "com.nullxweight.servicebackbroadcast";
    private IntentFilter intFilt;
    private static LatLng mCurLoc;


    public FillInMap (Context context, GoogleMap Map)
    {
        this.context = context;
        db = new DB(context);

        mMap = Map;
        mMarkerList = new ArrayList<>();
        mMap.setOnMarkerDragListener(this);


    }
    @Override
    public void run() {

        mCurLoc = ServiceForLocation.mCurLoc;
        // создаем объект для создания и управления версиями БД
        // создаем фильтр для BroadcastReceiver
        final Handler handler = new Handler(Looper.getMainLooper());
        db.getAllData();
        int count = 0;
        alAccessPoints = db.getmApList();

        for (int i = 0; i < alAccessPoints.size(); i++) {
            final int n = i;
            handler.post(new Runnable() {
                public void run() {
                    alAccessPoints.get(n).getLevel();
                    Log.d("Map", "marker = "+alAccessPoints.get(n).getLat()+" lon = "+ alAccessPoints.get(n).getLon());
                   mMarkerList.add(n, mMap.addMarker(new MarkerOptions()           //добавлеие маркера на карту + в список маркеров
                           .position(new LatLng(alAccessPoints.get(n).getLat(), alAccessPoints.get(n).getLon()))
                           .title(alAccessPoints.get(n).getSSID())
                           .icon(BitmapDescriptorFactory
                                   .fromBitmap(BitmapFactory
                                           .decodeResource(context.getResources(), selectWifiMarker(alAccessPoints.get(n).getLevel()
                                                   , alAccessPoints.get(n).getEncrypt()))))
                           .snippet("Signal: " + alAccessPoints.get(n).getLevel()              //описание точки по нажатию на маркер
                                   + "  " + alAccessPoints.get(n).getEncrypt())
                           .draggable(true)));

                   // setUpClusterer();
                   // MyItem offsetItem = new MyItem(alAccessPoints.get(n).getLat(), alAccessPoints.get(n).getLon());
                   // mClusterManager.addItem(offsetItem);
                }
            });
            count++;
        }
        Log.d(TAG, "Was load " + count + " WiFi markers");
        //            Toast.makeText(getApplicationContext(), "Was load "+ count +" WiFi markers", Toast.LENGTH_SHORT).show();
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

        return R.mipmap.ic_wifi_launcher_lock;
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

            marker.setVisible(db.getVector(ServiceForLocation.mCurLoc,
                        new LatLng( marker.getPosition().latitude,marker.getPosition().longitude)));
        }

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

    private void setUpClusterer() {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyItem>(context,mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);




        // Add cluster items (markers) to the cluster manager.
    }

}

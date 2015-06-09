package com.sls.wguide.wguide;

import android.content.Context;
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
    private String TAG = "FillInMap";
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
        final Handler handler = new Handler(Looper.getMainLooper());
        db.open();
        db.getAllData();
        int count = 0;
        alAccessPoints = db.getmApList();

        for (int i = 0; i < alAccessPoints.size(); i++) {
            final int n = i;
            handler.post(new Runnable() {
                public void run() {

                   mMarkerList.add(n, mMap.addMarker(new MarkerOptions()           //добавлеие маркера на карту + в список маркеров
                            .position(new LatLng(alAccessPoints.get(n).getLat(), alAccessPoints.get(n).getLon()))
                            .title(alAccessPoints.get(n).getSSID())
                            .icon(BitmapDescriptorFactory
                                    .fromBitmap(BitmapFactory
                                            .decodeResource(context.getResources(), R.mipmap.ic_wifi_launcher)))
                            .snippet("Signal: " + alAccessPoints.get(n).getLevel()              //описание точки по нажатию на маркер
                                    + "\n " + alAccessPoints.get(n).getEncrypt())
                            .draggable(true)));

                }
            });
            count++;
        }
        db.close();
        Log.d(TAG, "Was load " + count + " WiFi markers");
        //            Toast.makeText(getApplicationContext(), "Was load "+ count +" WiFi markers", Toast.LENGTH_SHORT).show();
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
            db.open();
           if (db.insertRec(tmp.getBSSID(),
                    tmp.getSSID(),
                    tmp.getLevel(),
                    marker.getPosition().latitude,
                    marker.getPosition().longitude,
                    tmp.getEncrypt(),
                    tmp.getWho_add(),
                    new Date().getTime()))
               Toast.makeText(context, tmp.getSSID()+ " location changed ", Toast.LENGTH_SHORT).show();
        db.close();

        }

    }

    private AccessPoint getApByMarkerId (String id)
    {
        for (int i = 0; i<mMarkerList.size(); i++)
            if (mMarkerList.get(i).getId().equalsIgnoreCase(id))
                return alAccessPoints.get(i);

        return null;

    }
}

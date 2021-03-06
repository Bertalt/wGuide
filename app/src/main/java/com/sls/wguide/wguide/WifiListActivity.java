package com.sls.wguide.wguide;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

/**
 * Created on 08.05.2015.
 */
public class WifiListActivity
        extends FragmentActivity  implements SwipeRefreshLayout.OnRefreshListener{

    public static final String PARAM_SSID = "ssid";
    public static final String PARAM_BSSID = "bssid";
    public static final String PARAM_LEVEL = "level";
    public static final String PARAM_ECRYPT  = "encrypt";
    public static final String PARAM_COUNT = "count";
    public final static String BROADCAST_ACTION = "com.nullxweight.servicebackbroadcast_sat";
    public final static String BROADCAST_ACTION_WF = "com.nullxweight.servicebackbroadcast_wf";
    private BroadcastReceiver br_wf;
    private  DB db;
    private LinearLayout progressLayout;
    private ExecutorService mExecutorService;
    private static final String TAG = "WF_list";
    private static int mSatCount = 0;
    private String mSSID;
    private String mBSSID;
    private int mLEVEL;
    private int mCountWF;
    private String mEncrypt;
   // public static ArrayList<AccessPoint> mAPList;
    static FragmentManager fm;
    private ArrayList<HashMap<String, Object>> mWFList;
    private static final String lLEVEL = "level"; // ������
    private static final String lSSID = "SSID"; // ������ �������
    private static final String lBSSID = "BSSID";  // ������ �������
    private static final String lENCRYPT = "encrypt"; // ������
    private ListView listView;
    private LatLng mCurLoc;
    public static final String PARAM_LON = "Longitude";
    public static final String PARAM_LAT = "Latitude";
    private TextView pbTitle;
    private static final int CM_ADD_ID = 1;
    private SharedPreferences sharedPref;
    private Timer myTimer;
    private SimpleAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hand_add_main);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        db = new DB(this);
        db.open();
        mCurLoc = ServiceForLocation.mCurLoc;
        /*
        progressLayout = (LinearLayout) findViewById(R.id.linLayout_progress);
        pbTitle = (TextView) findViewById(R.id.pbTitle);
        pbTitle.setText(getResources().getText(R.string.header_search)                      // part title from resources
                + " -" + sharedPref.getString(SettingsActivity.KEY_PREF_WIFI_MIN_LEVEL, "90")// from references
                + "dB...");
                */
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getAllData();
            }
        }).start();

        startService(new Intent(this, WiFiviser.class));

        // ������� ������ �������
        mWFList = new ArrayList<HashMap<String, Object>>();

        listView = (ListView) findViewById(R.id.listView_hand_add);

        adapter = new SimpleAdapter(getApplicationContext(), mWFList,
                R.layout.hand_add_item, new String[]{lLEVEL, lSSID, lBSSID, lENCRYPT},
                new int[]{R.id.tvLevel_wf, R.id.tvSSID_wf, R.id.tvBSSID_wf, R.id.tvEncrypt_wf});

        listView.setAdapter(adapter);


                 fm = getFragmentManager();

        registerForContextMenu(listView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_hand_add);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
             }
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_ADD_ID, 0, R.string.add_record);
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_ADD_ID) {
            try {
                // �������� �� ������ ������������ ���� ������ �� ������ ������
                AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();

            db.getAllData();
                String tmp_bssid = mWFList.get(acmi.position).get(lBSSID).toString();
                AccessPoint tmp_obj_ex = db.getByBssid(tmp_bssid);
                if (tmp_obj_ex != null) {

                    if (tmp_obj_ex.getLevel() >= Integer.parseInt(mWFList.get(acmi.position).get(lLEVEL).toString().substring(0, 3))) {
                        Toast.makeText(this, tmp_obj_ex.getSSID() + " already exist with better signal", Toast.LENGTH_SHORT).show();
                        return true;
                    } else {
                        db.insertRec(mWFList.get(acmi.position).get(lBSSID).toString(),
                                mWFList.get(acmi.position).get(lSSID).toString(),
                                Integer.parseInt(mWFList.get(acmi.position).get(lLEVEL).toString().substring(0, 3)),
                                mCurLoc.latitude,
                                mCurLoc.longitude,
                                mWFList.get(acmi.position).get(lENCRYPT).toString(),
                                "U",
                                mSatCount,
                                new Date().getTime());
                        Toast.makeText(this, tmp_obj_ex.getSSID() + " was updated", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                db.insertRec(mWFList.get(acmi.position).get(lBSSID).toString(),
                        mWFList.get(acmi.position).get(lSSID).toString(),
                        Integer.parseInt(mWFList.get(acmi.position).get(lLEVEL).toString().substring(0, 3)),
                        mCurLoc.latitude,
                        mCurLoc.longitude,
                        mWFList.get(acmi.position).get(lENCRYPT).toString(),
                        "U",
                        mSatCount,
                        new Date().getTime());

                Toast.makeText(getApplicationContext(), mWFList.get(acmi.position).get(lSSID).toString() + " was added",
                        Toast.LENGTH_SHORT).show();
                // �������� ����� ������ � �������
//            getSupportLoaderManager().getLoader(0).forceLoad();

            }catch (NullPointerException NPE)
            {
                NPE.printStackTrace();
                Toast.makeText(getApplicationContext(), "Oops, error #2. Please, try again", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        db.close();
    }
    @Override
    public void onPause()
    {
        super.onPause();
        try {
            unregisterReceiver(br_wf);
        }
        catch (IllegalArgumentException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        onRefresh();
       // adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {

        mSwipeRefreshLayout.setRefreshing(true);
        mWFList.clear();
        adapter.notifyDataSetChanged();
        onApListener();

    }


    private void onApListener ()
    {
        mCountWF = 0;
        mCurLoc = MapsActivity.mCurLoc;
        mSatCount = MapsActivity.mSatCount;

                br_wf = new BroadcastReceiver() {
                    // �������� ��� ��������� ���������
                    public void onReceive(Context context, Intent intent) {

                        mSSID = intent.getStringExtra(PARAM_SSID);
                        mBSSID = intent.getStringExtra(PARAM_BSSID);
                        mLEVEL = intent.getIntExtra(PARAM_LEVEL, 0);
                        mEncrypt = intent.getStringExtra(PARAM_ECRYPT);
                        mCountWF = intent.getIntExtra(PARAM_COUNT, 0);

                        Log.d(TAG, "onReceive: SSID =  " + mSSID);
                        Log.d(TAG, "onReceive: BSSID =  " + mBSSID);
                        Log.d(TAG, "onReceive: Level=  " + mLEVEL);
                        Log.d(TAG, "onReceive: Encrypt =  " + mEncrypt);

                        if (mWFList.size() == 0) {
                            HashMap<String, Object> hm;
                            hm = new HashMap<>();
                            hm.put(lLEVEL, mLEVEL + "dB"); //
                            hm.put(lSSID, mSSID); //
                            hm.put(lBSSID, mBSSID); //
                            hm.put(lENCRYPT, mEncrypt);
                            mWFList.add(hm);
                            Log.d("inputing", "Input " + mBSSID + " in list");
                            adapter.notifyDataSetChanged();
                        }
                        if (mWFList.size() < mCountWF)
                            for (int i = 0; i < mWFList.size(); i++) {
                                if (mWFList.get(i).get(lBSSID).toString().equalsIgnoreCase(mBSSID)) {
                                    Log.d("inputing", mSSID + " already exist in list");
                                    return;
                                }
                            }
                        else {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Log.d("WF1", "GONE");
                           unregisterReceiver(br_wf);

                            return;
                        }

                        HashMap<String, Object> hm;
                        hm = new HashMap<>();
                        hm.put(lLEVEL, mLEVEL + "dB"); //
                        hm.put(lSSID, mSSID); //
                        hm.put(lBSSID, mBSSID); //
                        hm.put(lENCRYPT, mEncrypt);
                        mWFList.add(hm);
                        adapter.notifyDataSetChanged();
                        //mAPList.add(new AccessPoint(mSSID, mBSSID, mLEVEL, mEncrypt, new Date().getTime()));
                        Log.d("inputing", "Input " + mSSID + " in list");
                        Log.d("myList", "list size = " + mWFList.size());
                    }
                };
                IntentFilter intFilt_wf = new IntentFilter(BROADCAST_ACTION_WF);
                registerReceiver(br_wf, intFilt_wf);




       //     }
      //  });


    }
    }

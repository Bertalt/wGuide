package com.sls.wguide.wguide;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
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
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 08.05.2015.
 */
public class WifiListActivity
        extends FragmentActivity {

    public static final String PARAM_SSID = "ssid";
    public static final String PARAM_BSSID = "bssid";
    public static final String PARAM_LEVEL = "level";
    public static final String PARAM_ECRYPT  = "encrypt";
    public static final String PARAM_COUNT = "count";
    public static final String PARAM_SAT  = "satellites";
    public final static String BROADCAST_ACTION_LOC = "com.nullxweight.servicebackbroadcast";
    public final static String BROADCAST_ACTION = "com.nullxweight.servicebackbroadcast_sat";
    public final static String BROADCAST_ACTION_WF = "com.nullxweight.servicebackbroadcast_wf";
    private BroadcastReceiver br;
    private BroadcastReceiver br_wf;
    private BroadcastReceiver br_loc;
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
    private static final String lLEVEL = "level"; // первый
    private static final String lSSID = "SSID"; // первый второго
    private static final String lBSSID = "BSSID";  // второй второго
    private static final String lENCRYPT = "encrypt"; // третий
    private ListView listView;
    private LatLng mCurLoc;
    public static final String PARAM_LON = "Longitude";
    public static final String PARAM_LAT = "Latitude";
    private TextView pbTitle;
    private static final int CM_ADD_ID = 1;
    private SharedPreferences sharedPref;
    private Timer myTimer;
    SimpleAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hand_add_main);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        db = new DB(this);
        db.open();
        progressLayout = (LinearLayout) findViewById(R.id.linLayout_progress);
        pbTitle = (TextView) findViewById(R.id.pbTitle);
        pbTitle.setText(getResources().getText(R.string.header_search)                      // part title from resources
                +" -"+  sharedPref.getString( SettingsActivity.KEY_PREF_WIFI_MIN_LEVEL, "90")// from references
                + "dB...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getAllData();
            }
        }).start();

        startService(new Intent(this, WiFiviser.class));

        // создаем массив списков
            mWFList = new ArrayList<HashMap<String, Object>>();
        listView = (ListView) findViewById(R.id.listView);
        listView.setEmptyView( findViewById( R.id.empty_list_view ) );

        adapter = new SimpleAdapter(getApplicationContext(), mWFList,
                R.layout.list_item, new String[]{lLEVEL, lSSID, lBSSID, lENCRYPT},
                new int[]{R.id.tvLevel_wf, R.id.tvSSID_wf, R.id.tvBSSID_wf, R.id.tvEncrypt_wf});

        listView.setAdapter(adapter);
        mExecutorService = Executors.newFixedThreadPool(1);
        mCountWF = 0;
       // mAPList = new ArrayList<AccessPoint>();
        // создаем BroadcastReceiver
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {

                // создаем BroadcastReceiver
                br_loc = new BroadcastReceiver() {
                    // действия при получении сообщений
                    public void onReceive(Context context, Intent intent) {
                        double mLongitude = intent.getDoubleExtra(PARAM_LON, 0);
                        double mLatitude = intent.getDoubleExtra(PARAM_LAT, 0);
                        mCurLoc = new LatLng(mLatitude, mLongitude);
                        Log.d(TAG, "onReceive: Lat = " + mLatitude + ", Lon = " + mLongitude);

                    }
                };

                // создаем фильтр для BroadcastReceiver
                IntentFilter intFilt1 = new IntentFilter(BROADCAST_ACTION_LOC);
                // регистрируем (включаем) BroadcastReceiver
                registerReceiver(br_loc, intFilt1);

                br = new BroadcastReceiver() {
                    // действия при получении сообщений
                    public void onReceive(Context context, Intent intent) {

                        mSatCount = intent.getIntExtra(PARAM_SAT, 0);

                        Log.d(TAG, "onReceive: satellites =  " + mSatCount);
                    }
                };
                // создаем фильтр для BroadcastReceiver
                IntentFilter intFilt2 = new IntentFilter(BROADCAST_ACTION);
                // регистрируем (включаем) BroadcastReceiver
                registerReceiver(br, intFilt2);


                br_wf = new BroadcastReceiver() {
                    // действия при получении сообщений
                    public void onReceive(Context context, Intent intent) {

                        mSSID = intent.getStringExtra(PARAM_SSID);
                        mBSSID = intent.getStringExtra(PARAM_BSSID);
                        mLEVEL = intent.getIntExtra(PARAM_LEVEL, 0);
                        mEncrypt = intent.getStringExtra(PARAM_ECRYPT);
                        mCountWF = intent.getIntExtra(PARAM_COUNT,0);

                        Log.d(TAG, "onReceive: SSID =  " + mSSID);
                        Log.d(TAG, "onReceive: BSSID =  " + mBSSID);
                        Log.d(TAG, "onReceive: Level=  " + mLEVEL);
                        Log.d(TAG, "onReceive: Encrypt =  " + mEncrypt);

                        if (mWFList.size() == 0)
                        {
                            HashMap<String, Object> hm;
                            hm = new HashMap<>();
                            hm.put(lLEVEL, mLEVEL+"dB"); //
                            hm.put(lSSID, mSSID); //
                            hm.put(lBSSID, mBSSID); //
                            hm.put(lENCRYPT,mEncrypt);
                            mWFList.add(hm);
                            Log.d("inputing", "Input " + mBSSID + " in list");
                            adapter.notifyDataSetChanged();
                    }
                        if(mWFList.size() !=  mCountWF)
                        for (int i = 0; i<mWFList.size(); i++) {
                            if (mWFList.get(i).get(lBSSID).toString().equalsIgnoreCase(mBSSID)) {
                                Log.d("inputing", mSSID + " already exist in list");
                                return;
                            }
                        }
                          else
                        {
                            if(progressLayout != null)
                                progressLayout.removeAllViews();
                            return;
                        }

                        HashMap<String, Object> hm;
                        hm = new HashMap<>();
                        hm.put(lLEVEL, mLEVEL+"dB"); //
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

            }
        });

                 fm = getFragmentManager();

        registerForContextMenu(listView);

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mWFList.clear();
            }
        }, 0, 10 * 1000);

             }
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_ADD_ID, 0, R.string.add_record);
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_ADD_ID) {
            try {
                // получаем из пункта контекстного меню данные по пункту списка
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
                // получаем новый курсор с данными
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
        unregisterReceiver(br);
        unregisterReceiver(br_wf);
        unregisterReceiver(br_loc);
        db.close();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

             public static void showDialog() {

                 DialogFragment newFragment = AlertDialogSatellites.newInstance(
                         R.string.alert_dialog_two_buttons_title, mSatCount);
                 newFragment.show(fm, "dialog");
             }

             public static void doPositiveClick() {
                 // Do stuff here.
                 Log.i("FragmentAlertDialog", "Positive click!");
             }

             public static void doNegativeClick() {
                 // Do stuff here.
                 Log.i("FragmentAlertDialog", "Negative click!");
             }
    }

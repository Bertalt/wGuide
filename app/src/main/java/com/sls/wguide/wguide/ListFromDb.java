package com.sls.wguide.wguide;

/**
 * Created by Sls on 21.05.2015.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class ListFromDb extends FragmentActivity implements LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {

    private static final int CM_DELETE_ID = 1;
    private static final int CM_OPEN_ID = 2;

    private ListView lvData;
    private static LinearLayout linearLayout;
    private DB db;
    private View layerPbLoad;
    private ApAdapter scAdapter;
    private final String TAG = "open_db";
    private float mAvaRadius;
    private SharedPreferences sharedPref;

    SwipeRefreshLayout mSwipeRefreshLayout;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_db_main);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mAvaRadius = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_MAP_AVA_RADIUS, "1000"));

        linearLayout = (LinearLayout) findViewById(R.id.linLayout_progress);

        LayoutInflater inflater = getLayoutInflater();
        layerPbLoad = inflater.inflate(R.layout.load_layout, null);

        // открываем подключение к БД
        db = new DB(this);

        // формируем столбцы сопоставления
        String[] from = new String[]{db.LEVEL, db.SSID, db.BSSID, db.ENCRYPT};
        int[] to = new int[] { R.id.tvLevel, R.id.tvSSID, R.id.tvBSSID, R.id.tvEncrypt };

        // создааем адаптер и настраиваем список
        scAdapter = new ApAdapter(this, R.layout.list_db_item, null, from, to, 0);
        lvData = (ListView) findViewById(R.id.list_view_db);


            lvData.setAdapter(scAdapter);

            registerForContextMenu(lvData);
        // добавляем контекстное меню к списку


        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_db);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

    }


    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
        menu.add(0, CM_OPEN_ID, 0, R.string.open_record);

    }
    public AccessPoint getApByPosition(int position)
    {
        return db.getmApList().get(position);
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
                    .getMenuInfo();
            // извлекаем id записи и удаляем соответствующую запись в БД
            db.delRec(acmi.id);

            // получаем новый курсор с данными
            getSupportLoaderManager().getLoader(0).forceLoad();
            return true;
        }
        if (item.getItemId() == CM_OPEN_ID) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
                    .getMenuInfo();
            // извлекаем id записи и коодинаты AP
           AccessPoint tmp =getApByPosition(acmi.position);
            if (tmp != null)
            {
                Intent intent = new Intent(this, MapsActivity.class).putExtra("lat",tmp.getLat())
                        .putExtra("lon",tmp.getLon())
                      //  .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        ; /*  */
                startActivity(intent);

            }
            else
                Toast.makeText(this, "Cannot find selected access point", Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }
    protected void onResume()
    {
        super.onResume();

    }

    protected void onDestroy() {
        super.onDestroy();
        // закрываем подключение при выходе

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new MyCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() == 0)
        {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.toast_empty_database), Toast.LENGTH_SHORT).show();
            super.finish();
        }
        if  (linearLayout != null)
            linearLayout.removeAllViews();
        scAdapter.swapCursor(cursor);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onRefresh() {

        mSwipeRefreshLayout.setRefreshing(true);
        new MyCursorLoader(this, db);
    }

    static class MyCursorLoader extends CursorLoader {

        DB db;

        public MyCursorLoader(Context context, DB db) {
            super(context);
            this.db = db;

        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.getAllData();

            Log.d("curs", String.valueOf(cursor.getCount()));
            return cursor;
        }
    }
}
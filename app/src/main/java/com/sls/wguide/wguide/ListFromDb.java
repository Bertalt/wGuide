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

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class ListFromDb extends FragmentActivity implements LoaderCallbacks<Cursor> {

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
    private double mRadiusInLanLng = 0.00000960865339; // = 1 m

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_db_main);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mAvaRadius = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_MAP_AVA_RADIUS, "1000"));

        linearLayout = (LinearLayout) findViewById(R.id.linLayout_progress);

        LayoutInflater inflater = getLayoutInflater();
        layerPbLoad = inflater.inflate(R.layout.load_layout, null);



        // ��������� ����������� � ��
        db = new DB(this);
        db.open();

        // ��������� ������� �������������
        String[] from = new String[]{db.LEVEL, db.SSID, db.BSSID, db.ENCRYPT};
        int[] to = new int[] { R.id.tvLevel, R.id.tvSSID, R.id.tvBSSID, R.id.tvEncrypt };

        // �������� ������� � ����������� ������
        scAdapter = new ApAdapter(this, R.layout.list_db_item, null, from, to, 0);
        lvData = (ListView) findViewById(R.id.list_view_db);

            lvData.setAdapter(scAdapter);

            registerForContextMenu(lvData);
        // ��������� ����������� ���� � ������


        // ������� ������ ��� ������ ������
        getSupportLoaderManager().initLoader(0, null, this);

    }

    // ��������� ������� ������
    /*
    public void onButtonClick(View view) {
        // ��������� ������
        db.addRec("sometext " + (scAdapter.getCount() + 1), R.drawable.ic_launcher);
        // �������� ����� ������ � �������
        getSupportLoaderManager().getLoader(0).forceLoad();
    }
*/
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
            // �������� �� ������ ������������ ���� ������ �� ������ ������
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
                    .getMenuInfo();
            // ��������� id ������ � ������� ��������������� ������ � ��
            db.delRec(acmi.id);

            if (linearLayout != null && layerPbLoad != null)
            linearLayout.addView(layerPbLoad);
            else
            Log.e(TAG, "Can not find load layout ");
            // �������� ����� ������ � �������
            getSupportLoaderManager().getLoader(0).forceLoad();
            return true;
        }
        if (item.getItemId() == CM_OPEN_ID) {
            // �������� �� ������ ������������ ���� ������ �� ������ ������
            AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
                    .getMenuInfo();
            // ��������� id ������ � ��������� AP
           AccessPoint tmp =getApByPosition(acmi.position);
            if (tmp != null)
            {
                Intent intent = new Intent(this, MapsActivity.class).putExtra("lat",tmp.getLat())
                        .putExtra("lon",tmp.getLon())
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
            else
                Toast.makeText(this, "Cannot find selected access point", Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }

    protected void onDestroy() {
        super.onDestroy();
        // ��������� ����������� ��� ������

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

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
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
            return cursor;
        }

    }
    private boolean getVector ( LatLng a ,  LatLng b)
    {
        if (a == null)
            return true;
        if (b == null)
            return false;

        LatLng AB = new LatLng(b.latitude - a.latitude, b.longitude - a.longitude);
        double radius = sqrt(pow(AB.latitude,2) + pow(AB.longitude,2));
        Log.d(TAG, "Radius = " + radius);
        if (radius > mRadiusInLanLng*mAvaRadius)
            return false;

        return true;
    }
}
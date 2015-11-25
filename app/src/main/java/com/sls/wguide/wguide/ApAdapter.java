package com.sls.wguide.wguide;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Sls on 04.06.2015.
 */
public class ApAdapter extends SimpleCursorAdapter {

    int mLayoutInflater;


    public ApAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        mLayoutInflater = layout;

    }

    @Override
    public View newView(Context _context, Cursor _cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) _context.getSystemService(_context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(mLayoutInflater, parent, false);
        return view;
    }
    @Override
    public void bindView(View view, Context _context, Cursor _cursor) {
        int  level = _cursor.getInt(_cursor.getColumnIndex(DB.LEVEL));
        String SSID = _cursor.getString(_cursor.getColumnIndex(DB.SSID));
        String BSSID = _cursor.getString(_cursor.getColumnIndex(DB.BSSID));
        String ENCRYPT= _cursor.getString(_cursor.getColumnIndex(DB.ENCRYPT));
        String WHO_ADD = _cursor.getString(_cursor.getColumnIndex(DB.WHO_ADD));


        TextView tvLevel = (TextView) view.findViewById(R.id.tvLevel);
        TextView tvSSID = (TextView) view.findViewById(R.id.tvSSID);
        TextView tvBSSID= (TextView) view.findViewById(R.id.tvBSSID);
        TextView tvEncrypt= (TextView) view.findViewById(R.id.tvEncrypt);
        TextView tvWho_add = (TextView) view.findViewById(R.id.who_add);

        tvLevel.setText(level+"db");
        tvSSID.setText(SSID+"");
        tvBSSID.setText(BSSID+"");
        tvEncrypt.setText(ENCRYPT+"");
       tvWho_add.setText(WHO_ADD+"");


    }
}

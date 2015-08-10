package com.sls.wguide.wguide;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Sls on 28.07.2015.
 */
public class DialogFragmentWaitMap extends DialogFragment {

    public static DialogFragmentWaitMap newInstance() {

        return new DialogFragmentWaitMap();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .create();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wait_map, container, false);
        View tv = v.findViewById(R.id.tvDialogWait);
        ((TextView)tv).setText("Wait for Google Maps...");
        return v;
    }
}

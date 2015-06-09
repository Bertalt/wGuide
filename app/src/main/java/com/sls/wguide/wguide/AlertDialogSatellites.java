package com.sls.wguide.wguide;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Sls on 18.05.2015.
 */
public class AlertDialogSatellites extends DialogFragment{

    public static AlertDialogSatellites newInstance(int title, int countSatellites) {
        AlertDialogSatellites frag = new AlertDialogSatellites();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("countSat", countSatellites);


        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        int countSat = getArguments().getInt("countSat");
        String message = getString(R.string.recomend_part_1) +" "+countSat+" "+ getString(R.string.recomend_part_2);
        return new AlertDialog.Builder(getActivity())
               // .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                WifiListActivity.doPositiveClick();
                            }
                        }
                )
                .setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                WifiListActivity.doNegativeClick();
                            }
                        }
                )
                .create();
    }
}

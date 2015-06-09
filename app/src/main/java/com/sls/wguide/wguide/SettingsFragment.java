package com.sls.wguide.wguide;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

/**
 * Created by Sls on 27.05.2015.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.preferences);
        // Load the preferences from an XML resource
        findPreference(SettingsActivity.KEY_PREF_GPS_SYSTEM).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return false;
            }
        });

    }

    @Override
    public void onResume()
    {
        super.onResume();


    }


}

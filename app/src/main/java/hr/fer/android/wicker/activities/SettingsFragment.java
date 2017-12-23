package hr.fer.android.wicker.activities;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import hr.fer.android.wicker.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }
}

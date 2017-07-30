package hr.fer.android.wicker.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import hr.fer.android.wicker.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}

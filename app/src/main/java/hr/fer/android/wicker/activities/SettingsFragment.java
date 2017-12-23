package hr.fer.android.wicker.activities;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import hr.fer.android.wicker.R;
import hr.fer.android.wicker.db.CounterDatabase;
import hr.fer.android.wicker.entity.Counter;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        //set auto save
        setSummary(getPreferenceScreen().getSharedPreferences());

        //set version
        Preference p = findPreference(getString(R.string.pref_version_key));
        try {
            p.setSummary(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("Key", key);
        String prefNotificationKey = getString(R.string.pref_notification_key);
        Boolean prefNotificationDefault = getResources().getBoolean(R.bool.pref_notification_default);
        if (key.equals(prefNotificationKey) && !sharedPreferences.getBoolean(prefNotificationKey, prefNotificationDefault)) {
            //clear notifications
            CounterDatabase database = new CounterDatabase(getContext());
            for (Counter tmp : database.getDatabaseCounterListData())
                NotificationManagerCompat.from(getContext()).cancel(tmp.getId().intValue());
        } else if (key.equals(getString(R.string.pref_save_question_key))) {
            setSummary(sharedPreferences);
        } else if (key.equals(getString(R.string.pref_automatic_save_key))) {
            setSummary(sharedPreferences);
        }
    }

    private void setSummary(SharedPreferences preferences) {
        //for auto save
        String key = getString(R.string.pref_save_question_key);
        Boolean saveAlertEnabled = preferences.getBoolean(
                key, getResources().getBoolean(R.bool.pref_save_question_default));
        //enable/disable auto save pref
        Preference p = findPreference(getString(R.string.pref_automatic_save_key));
        Boolean autoSave = preferences.getBoolean(
                getString(R.string.pref_automatic_save_key),
                getResources().getBoolean(R.bool.pref_automatic_save_default));
        String enableMsg = getString(autoSave ? R.string.pref_automatic_save_summary_enabled_on
                : R.string.pref_automatic_save_summary_enabled_off);
        p.setEnabled(!saveAlertEnabled);
        p.setSummary(!saveAlertEnabled ? enableMsg : getString(R.string.pref_automatic_save_summary_disabled));

    }
}

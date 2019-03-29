package com.lc.monitor.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.View;

import com.lc.monitor.R;
import com.lc.monitor.ToolsCallback;

public class SettingsFragment extends PreferenceFragmentCompat implements ToolsCallback,SharedPreferences.OnSharedPreferenceChangeListener {

    private String TAG = getClass().getSimpleName();
    private ListPreference mRecordPreference;
    private EditTextPreference mStoragePreference;
    private EditTextPreference mMonitorNamePreference;

    private SharedPreferences mSp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setSharedPreferencesName("app_setting");
        addPreferencesFromResource(R.xml.setting_pref);
        mRecordPreference = (ListPreference) findPreference("pref_record_time");
        mStoragePreference = (EditTextPreference) findPreference("pref_storage");
        mMonitorNamePreference = (EditTextPreference) findPreference("pref_monitor_name");
        mSp = getPreferenceManager().getSharedPreferences();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        String storagePath = mSp.getString("pref_storage",getString(R.string.pref_title_storage_hint));
        String monitorName = mSp.getString("pref_monitor_name",getString(R.string.pref_title_storage_name_hint));
        mStoragePreference.setSummary(storagePath);
        mMonitorNamePreference.setSummary(monitorName);
        updateRecordTime();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onStop() {
        super.onStop();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updateRecordTime(){
        String value = mSp.getString("pref_record_time","30");
        CharSequence[] entrys = mRecordPreference.getEntryValues();
        int position = 0;
        for (int i=0;i<entrys.length;i++){
            if(value.equals( entrys[i])){
                position = i;
                break;
            }
        }
        Log.d(TAG,"position:"+position);
        CharSequence entry = mRecordPreference.getEntries()[position];
        mRecordPreference.setSummary(entry);
    }

    @Override
    public int getTitleRes() {
        return R.string.title_setting;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key == mRecordPreference.getKey()){
            updateRecordTime();
        }else if(key == mStoragePreference.getKey()){

        }else if(key == mMonitorNamePreference.getKey()){

        }
    }
}

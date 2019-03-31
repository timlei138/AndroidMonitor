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

import com.lc.monitor.CommCont;
import com.lc.monitor.R;
import com.lc.monitor.ToolsCallback;

import static com.lc.monitor.CommCont.SHAREDPREFS_NAME;

public class SettingsFragment extends PreferenceFragmentCompat implements ToolsCallback,
        SharedPreferences.OnSharedPreferenceChangeListener {

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
        getPreferenceManager().setSharedPreferencesName(CommCont.SHAREDPREFS_NAME);
        addPreferencesFromResource(R.xml.setting_pref);
        mRecordPreference = (ListPreference) findPreference(CommCont.SP_KEY_RECORD_TIME);
        mStoragePreference = (EditTextPreference) findPreference(CommCont.SP_KEY_STORAGE);
        mMonitorNamePreference = (EditTextPreference) findPreference(CommCont.SP_KEY_MONITOR_NAME);
        mSp = getPreferenceManager().getSharedPreferences();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        String storagePath = mSp.getString(CommCont.SP_KEY_STORAGE,getString(R.string.pref_title_storage_hint));
        String monitorName = mSp.getString(CommCont.SP_KEY_MONITOR_NAME,getString(R.string.pref_title_storage_name_hint));
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
        String value = mSp.getString(CommCont.SP_KEY_RECORD_TIME,"30");
        CharSequence[] entrys = mRecordPreference.getEntryValues();
        int position = 0;
        for (int i=0;i<entrys.length;i++){
            if(value.equals( entrys[i])){
                position = i;
                break;
            }
        }
        CharSequence entry = mRecordPreference.getEntries()[position];
        mRecordPreference.setSummary(entry);
    }

    @Override
    public int getTitleRes() {
        return R.string.title_setting;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG,"onSharedPreferenceChanged key=>"+key);
        if(key == mRecordPreference.getKey()){
            updateRecordTime();
        }else if(key == mStoragePreference.getKey()){

        }else if(key == mMonitorNamePreference.getKey()){

        }
    }
}

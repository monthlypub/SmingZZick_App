package com.monpub.sming.sming;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.monpub.sming.MusicListenService;
import com.monpub.sming.R;

public class SmingSettingFragment extends PreferenceFragment {
    private Handler handler;

    private PreferenceCategory preferenceCategory;
    private Preference preferenceStaticDisabled;

    public SmingSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        getPreferenceManager().setSharedPreferencesName(com.monpub.sming.etc.Preference.NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.pref_sming);

        preferenceCategory = (PreferenceCategory) findPreference("prefCategorySave");
        preferenceStaticDisabled = findPreference("prefStaticDisabled");

        if (Build.VERSION.SDK_INT < 21) {
            preferenceCategory.removePreference(findPreference(SmingManager.PREF_KEY_SAVE_SCREENSHOT));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (MusicListenService.isServiceRunning() == true) {
            preferenceCategory.setEnabled(false);
            if (getPreferenceScreen().findPreference(preferenceStaticDisabled.getKey()) == null) {
                getPreferenceScreen().addPreference(preferenceStaticDisabled);
            }
        } else {
            preferenceCategory.setEnabled(true);
            if (getPreferenceScreen().findPreference(preferenceStaticDisabled.getKey()) != null) {
                getPreferenceScreen().removePreference(preferenceStaticDisabled);
            }
        }

        IntentFilter intentFilter = new IntentFilter(MusicListenService.ACTION_STOP);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MusicListenService.ACTION_STOP.equals(intent.getAction())) {
                preferenceCategory.setEnabled(true);
                if (getPreferenceScreen().findPreference(preferenceStaticDisabled.getKey()) != null) {
                    getPreferenceScreen().removePreference(preferenceStaticDisabled);
                }
            }
        }
    };

}

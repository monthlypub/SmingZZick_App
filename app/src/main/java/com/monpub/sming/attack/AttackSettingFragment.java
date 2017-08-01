package com.monpub.sming.attack;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.monpub.sming.R;
import com.monpub.sming.etc.Preference;
import com.monpub.sming.sming.SmingManager;

public class AttackSettingFragment extends PreferenceFragment {
    private Handler handler;

    public AttackSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        getPreferenceManager().setSharedPreferencesName(Preference.NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.pref_attack);

        findPreference(AttackSetting.PREF_KEY_ALARM_BEFORE_V2).setOnPreferenceChangeListener(onPreferenceChangeListener);
        findPreference(AttackSetting.PREF_KEY_ALARM_NOTI).setOnPreferenceChangeListener(onPreferenceChangeListener);

        android.preference.Preference preference = findPreference(AttackSetting.PREF_KEY_SPECIAL_WORD);
        preference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        String specialWord = AttackSetting.getInstance().getSpecialWord();
        if (TextUtils.isEmpty(specialWord) == true) {
            preference.setSummary("없음");
        } else {
            preference.setSummary(specialWord);
        }

    }

    android.preference.Preference.OnPreferenceChangeListener onPreferenceChangeListener = new android.preference.Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
            if (preference instanceof EditTextPreference) {
                String value = (String) newValue;
                if (TextUtils.isEmpty(value) == false) {
                    preference.setSummary(value);
                } else {
                    preference.setSummary("없음");
                }
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    AttackSetting.getInstance().refresh();
                }
            });

            return true;
        }
    };

}

package com.monpub.sming.attack;

import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.monpub.sming.SmingApplication;
import com.monpub.sming.etc.Preference;
import com.monpub.sming.sming.SmingManager;

/**
 * Created by small-lab on 2016-08-17.
 */
public class AttackSetting {
    static final String PREF_KEY_ALARM_BEFORE_V1 = "prefKeyAlarmBefore";
    static final String PREF_KEY_ALARM_BEFORE_V2 = "prefKeyAlarmBeforeV2";
    static final String PREF_KEY_ALARM_NOTI = "prefKeyAlarmNoti";
    static final String PREF_KEY_SPECIAL_WORD = "prefKeySpecialWord";

    private static AttackSetting ourInstance = new AttackSetting();

    public static AttackSetting getInstance() {
        return ourInstance;
    }

    public static void migrate() {
        if (SmingApplication.getPreference().contains(PREF_KEY_ALARM_BEFORE_V1) == true) {
            int migrateValue = SmingApplication.getPreference().getInt(PREF_KEY_ALARM_BEFORE_V1, 3);
            SmingApplication.getPreference().put(PREF_KEY_ALARM_BEFORE_V2, "" + migrateValue);
            SmingApplication.getPreference().remove(PREF_KEY_ALARM_BEFORE_V1);
        }
    }

    private String mSpecialWord;
    private String mAlarmBefore;
    private String mAlarmNoti;

    private AttackSetting() {
        load();
        SmingApplication.getPreference().getPreferences().registerOnSharedPreferenceChangeListener(onPreferenceChangeListener);
    }

    public void refresh() {
        load();
    }

    private void load() {
        mSpecialWord = SmingApplication.getPreference().getString(PREF_KEY_SPECIAL_WORD);
        mAlarmBefore = SmingApplication.getPreference().getString(PREF_KEY_ALARM_BEFORE_V2);
        mAlarmNoti = SmingApplication.getPreference().getString(PREF_KEY_ALARM_NOTI);
    }

    public String getSpecialWord() {
        return mSpecialWord;
    }

    public int getAlarmBefore() {
        if (TextUtils.isEmpty(mAlarmBefore) == true) {
            return 3;
        }
        return Integer.valueOf(mAlarmBefore);
    }

    public int getAlarmNoti() {
        if (TextUtils.isEmpty(mAlarmNoti) == true) {
            return 2;
        }
        return Integer.valueOf(mAlarmNoti);

    }

    public static final int NOTI_TYPE_NONE = 0;
    public static final int NOTI_TYPE_SOUND = 1;
    public static final int NOTI_TYPE_VIBRATE = 2;
    public static final int NOTI_TYPE_ALL = 3;

    private SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            load();
            if (PREF_KEY_ALARM_BEFORE_V2.equals(key) == true || PREF_KEY_ALARM_NOTI.equals(key) == true) {
                AttackManager.getInstance().refreshAttackAlarm(SmingApplication.getAppContext());
            }
        }
    };
}

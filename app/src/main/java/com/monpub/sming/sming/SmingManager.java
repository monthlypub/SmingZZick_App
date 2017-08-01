package com.monpub.sming.sming;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.monpub.sming.Constant;
import com.monpub.sming.SmingApplication;

/**
 * Created by small-lab on 2016-08-20.
 */
public class SmingManager {
    private static SmingManager ourInstance = new SmingManager();

    public static SmingManager getInstance() {
        return ourInstance;
    }

    public static void migrate() {
        if (SmingApplication.getPreference().contains(PREF_KEY_FOLDER_V1) == true) {
            int migrateValue = SmingApplication.getPreference().getInt(PREF_KEY_FOLDER_V1, 0);
            SmingApplication.getPreference().put(PREF_KEY_FOLDER_V2, "" + migrateValue);
            SmingApplication.getPreference().remove(PREF_KEY_FOLDER_V1);
        }
    }

    static final String PREF_KEY_SONG_END_VIBRATE = "prefKeySongEndVibrate";
    static final String PREF_KEY_SMING_VIBRATE = "prefKeySmingVibrate";
    static final String PREF_KEY_FIRST_SHOT_VIBRATE = "prefKeyFirstShotVibrate";
    static final String PREF_KEY_VIBRATE_POWER = "prefKeyVibratePower";
    static final String PREF_KEY_OVERDRAW_STATUS = "prefKeyOverdrawStatus";

    private static final String PREF_KEY_SMING_WIDTH = "prefKeySmingWidth";
    private static final String PREF_KEY_FOLDER_V1 = "prefKeyFirstFolder";
    private static final String PREF_KEY_FOLDER_V2 = "prefKeyFirstFolderV2";
    private static final String PREF_KEY_SHOWN_UNDER_VERSION_GUIDE = "prefKeyShownUnderVersionGuide";
    static final String PREF_KEY_SAVE_SCREENSHOT = "prefKeySaveScreenshot";

    private boolean vibrateAfterSming = true;
    private boolean vibrateSongEnd = true;
    private boolean vibrateFirstShot = true;
    private boolean saveScreenshot = false;

    public boolean isShownUnderVersionGuide() {
        return shownUnderVersionGuide;
    }

    public void setShownUnderVersionGuide(boolean shownUnderVersionGuide) {
        this.shownUnderVersionGuide = shownUnderVersionGuide;
    }

    private boolean shownUnderVersionGuide = false;
    private int smingWidth;
    private String folderType;
    private String vibratePower;
    private boolean overdrawStatus;

    private SmingManager() {
        SmingApplication.getPreference().getPreferences().registerOnSharedPreferenceChangeListener(onPreferenceChangeListener);
        loadPref();
    }

    private void loadPref() {
        vibrateAfterSming = SmingApplication.getPreference().getBoolean(PREF_KEY_SMING_VIBRATE, true);
        vibrateSongEnd = SmingApplication.getPreference().getBoolean(PREF_KEY_SONG_END_VIBRATE, true);
        vibrateFirstShot = SmingApplication.getPreference().getBoolean(PREF_KEY_FIRST_SHOT_VIBRATE, true);
        smingWidth = SmingApplication.getPreference().getInt(PREF_KEY_SMING_WIDTH, Constant.MIN_SMING_WIDTH);
        folderType = SmingApplication.getPreference().getString(PREF_KEY_FOLDER_V2);
        vibratePower = SmingApplication.getPreference().getString(PREF_KEY_VIBRATE_POWER);
        overdrawStatus = SmingApplication.getPreference().getBoolean(PREF_KEY_OVERDRAW_STATUS, true);

        saveScreenshot = SmingApplication.getPreference().getBoolean(PREF_KEY_SAVE_SCREENSHOT, false);
        shownUnderVersionGuide = SmingApplication.getPreference().getBoolean(PREF_KEY_SHOWN_UNDER_VERSION_GUIDE, false);
    }

    public void refreshVibrate() {
        vibrateAfterSming = SmingApplication.getPreference().getBoolean(PREF_KEY_SMING_VIBRATE, true);
        vibrateSongEnd = SmingApplication.getPreference().getBoolean(PREF_KEY_SONG_END_VIBRATE, true);
        vibrateFirstShot = SmingApplication.getPreference().getBoolean(PREF_KEY_FIRST_SHOT_VIBRATE, true);
        vibratePower = SmingApplication.getPreference().getString(PREF_KEY_VIBRATE_POWER);
    }

    public boolean isVibrateAfterSming() {
        return vibrateAfterSming;
    }

    public boolean isVibrateSongEnd() {
        return vibrateSongEnd;
    }

    public boolean isVibrateFirstShot() {
        return vibrateFirstShot;
    }

    public boolean isSaveScreenshot() {
        return saveScreenshot;
    }

    public void setVibrateAfterSming(boolean vibrateAfterSming) {
        this.vibrateAfterSming = vibrateAfterSming;
        SmingApplication.getPreference().put(PREF_KEY_SMING_VIBRATE, vibrateAfterSming);
    }

    public void setSmingWidth(int width) {
        smingWidth = width;
        SmingApplication.getPreference().put(PREF_KEY_SMING_WIDTH, width);
    }

    public void setVibrateSongEnd(boolean vibrateSongEnd) {
        this.vibrateSongEnd = vibrateSongEnd;
        SmingApplication.getPreference().put(PREF_KEY_SONG_END_VIBRATE, vibrateSongEnd);
    }

    public void setVibrateFirstShot(boolean vibrateFirstShot) {
        this.vibrateFirstShot = vibrateFirstShot;
        SmingApplication.getPreference().put(PREF_KEY_FIRST_SHOT_VIBRATE, vibrateFirstShot);
    }

    public long[] getVibratePattern() {
        if (TextUtils.isEmpty(vibratePower) == true) {
            return Constant.VIBRATE_POWER_SETTTING[Constant.VIBRATE_POWER_MID];
        }

        int powerIndex = Integer.valueOf(vibratePower);

        return Constant.VIBRATE_POWER_SETTTING[powerIndex];
    }

    public int getSmingWdith() {
        return smingWidth;
    }

    public int getSmingHalfWidth() {
        return smingWidth / 2;
    }

    public int getFolderType() {
        if (TextUtils.isEmpty(folderType) == true) {
            return Constant.FOLDER_ALL;
        }
        return Integer.valueOf(folderType);
    }

    public boolean overdrawStatus() {
        return overdrawStatus;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            loadPref();
        }
    };
}


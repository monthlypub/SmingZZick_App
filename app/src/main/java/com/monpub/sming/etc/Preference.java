package com.monpub.sming.etc;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by small-lab on 2016-08-18.
 */
public class Preference {
    public static final String NAME = "SmingPref";
    SharedPreferences preferences;
    public Preference(Context context) {
        preferences = context.getSharedPreferences("SmingPref", Context.MODE_PRIVATE);
    }

    public synchronized boolean contains(String key) {
        return preferences.contains(key);
    }

    public synchronized void remove(String key) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
        return;
    }

    public synchronized void put(String key, Set<String> value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(key, value);
        editor.commit();
    }

    public synchronized Set<String> getStringSet(String key) {
        return preferences.getStringSet(key, null);
    }

    public synchronized void put(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public synchronized String getString(String key) {
        return preferences.getString(key, null);
    }

    public synchronized void put(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public synchronized boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public synchronized void put(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public synchronized int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public synchronized void put(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public synchronized long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    public synchronized SharedPreferences getPreferences() {
        return preferences;
    }
}

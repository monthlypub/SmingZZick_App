package com.monpub.sming;

import android.app.Application;
import android.content.Context;
import android.webkit.WebSettings;

import com.monpub.sming.attack.AttackSetting;
import com.monpub.sming.etc.Preference;
import com.monpub.sming.sming.SmingManager;

/**
 * Created by small-lab on 2016-08-18.
 */
public class SmingApplication extends Application {
    private static Preference preference;
    private static String sUserAgent;
    private static Context sContext;

    public static Context getAppContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        preference = new Preference(this);

        AttackSetting.migrate();
        SmingManager.migrate();

        sUserAgent = WebSettings.getDefaultUserAgent(this);
    }

    public static Preference getPreference() {
        return preference;
    }

    public static String getUserAgent() {
        return sUserAgent;
    }
}

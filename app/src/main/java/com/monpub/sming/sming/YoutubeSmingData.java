package com.monpub.sming.sming;

import android.text.TextUtils;

import java.util.Date;

/**
 * Created by small-lab on 2017-02-18.
 */

public class YoutubeSmingData extends SmingData {
    private boolean captureReady = false;
    private boolean runOutTime = false;

    public YoutubeSmingData(String song) {
        super("", song, 0);
    }

    public void captureReady() {
        captureReady = true;
    }

    public void runOutTime() {
        runOutTime = true;
    }

    @Override
    public boolean acceptNewShot() {
        if (captureReady == false) {
            return false;
        }

        long timeTerm;
        if (runOutTime == false) {
            timeTerm = 3000;
        } else {
            timeTerm = 500;
        }

        if (getLastShotMills() + timeTerm > System.currentTimeMillis()) {
            return false;
        }

        return true;
    }

    @Override
    public String getDropName() {
        String name;
        Date date = new Date(startMills);

        name = song.replaceAll(INVALID_FILENAME_REGEX, "_");
        return name + "_" + sdf.format(date) + ".png";
    }

    @Override
    public String getNotiText() {
        return song;
    }
}

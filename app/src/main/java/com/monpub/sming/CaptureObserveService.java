package com.monpub.sming;

import android.content.Context;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;
import android.os.Vibrator;

import java.io.File;

public class CaptureObserveService extends MusicListenService {
    private static FileObserver sFileObserver;

    public CaptureObserveService() {
        super();
        mTermToDrop = 1000;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected final void startSming() {
        startObserver();
    }

    @Override
    protected final void stopSming() {
        if (sFileObserver != null) {
            stopObserver();
        } else {
            terminate();
            afterTerminate();
        }
    }

    private void startObserver() {
        if (sFileObserver == null) {
            makeDirectory(null);
            final File screenshots = getScreenshotDirectory();

            sFileObserver = new FileObserver(screenshots.getAbsolutePath(), FileObserver.ALL_EVENTS) {
                @Override
                public void onEvent(int event, String path) {
                    event &= FileObserver.ALL_EVENTS;
                    if (event != FileObserver.CREATE) {
                        return;
                    }

                    synchronized (CaptureObserveService.this) {
                        if (mLastSmingData == null) {
                            return;
                        }

                        mLastSmingData.putNewShotPath(screenshots.getAbsolutePath() + "/" + path);
                    }
                }
            };
            sFileObserver.startWatching();
            updateNotification();

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }

    private void stopObserver() {
        sFileObserver.stopWatching();
        sFileObserver = null;

        terminate();
        afterTerminate();
    }

    @Override
    protected String getMusicPlayingNotiTitle() {
        return "캡쳐 감지 중";
    }

    @Override
    protected boolean isRunning() {
        return sFileObserver != null;
    }

    private static File getScreenshotDirectory() {
        File screenshots = Constant.getScreenshotFolder();

        if (screenshots.exists() == false) {
            screenshots.mkdirs();
        }

        return screenshots;
    }

    public static class CaptureFileNotReadyException extends IllegalStateException {
    }
}

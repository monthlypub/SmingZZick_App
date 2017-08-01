package com.monpub.sming;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.monpub.sming.sming.SmingData;
import com.monpub.sming.sming.SmingManager;
import com.monpub.sming.sming.YoutubeSmingData;
import com.monpub.sming.sticker.StickerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

/**
 * Created by small-lab on 2016-08-17.
 */
public abstract class MusicListenService extends Service {
    protected static String TAG;
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";

    public static final String ACTION_MUSIC_STATECHANGED = "com.android.music.playstatechanged";

    public static final String ACTION_YOUTUBE_CAPTURE_START = "com.monpub.sming.youtube.start";
    public static final String ACTION_YOUTUBE_CAPTURE_READY = "com.monpub.sming.youtube.ready";
    public static final String ACTION_YOUTUBE_CAPTURE_ALMOST = "com.monpub.sming.youtube.almost";
    public static final String ACTION_YOUTUBE_CAPTURE_FINISH = "com.monpub.sming.youtube.finish";

    protected HandlerThread mHandlerThread;
    protected Handler mHandler;

    protected boolean isPlaying;
    protected SmingData mLastSmingData;

    protected int mTermToDrop = 200;

    private static MusicListenService sInstance;
    public static boolean isServiceRunning() {
        return sInstance != null && sInstance.isRunning();
    }

    protected MusicListenService() {
        sInstance = this;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mHandlerThread = new HandlerThread("ScreenshotThread");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                File file = null;
                if (msg != null && msg.obj != null && msg.obj instanceof SmingData) {
                    SmingData data = (SmingData) msg.obj;
                    if (data.canDrop() == false) {
                        return;
                    }

                    FileOutputStream fos = null;

                    try {
                        File directory = makeDirectory(data);

                        if (SmingManager.getInstance().isSaveScreenshot() == true) {
                            Bitmap startBitmap = data.getStartShotBitmap();
                            Bitmap endBitmap = data.getEndShotBitmap();

                            if (startBitmap != null && startBitmap.isRecycled() == false
                                    && endBitmap != null && endBitmap.isRecycled() == false) {
                                File screenshotDirectory = Constant.getScreenshotFolder();

                                File startShotFile, endShotFile;
                                fos = new FileOutputStream(startShotFile = new File(screenshotDirectory, data.getDropName("_start")));
                                startBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos = new FileOutputStream(endShotFile = new File(screenshotDirectory, data.getDropName("_end")));
                                endBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{startShotFile.getAbsolutePath(), endShotFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                    @Override
                                    public void onScanCompleted(String path, Uri uri) {
                                        // do nothing
                                    }
                                });
                            }
                        }

                        Bitmap bitmap = data.getDropBitmap(MusicListenService.this);
                        if (bitmap != null && bitmap.isRecycled() == false) {
                            StickerManager.getInstance().drawStickers(MusicListenService.this, bitmap);
                            fos = new FileOutputStream(file = new File(directory, data.getDropName()));
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        }
                        data.clear();

                        if (SmingManager.getInstance().isVibrateAfterSming() == true) {
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(SmingManager.getInstance().getVibratePattern(), -1);
                        }
                    } catch (CaptureObserveService.CaptureFileNotReadyException e) {
                        if (data.getLastShotMills() < System.currentTimeMillis() + 2000) {
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(0, msg.obj), 500);
                        }
                        return;
                    } catch (Throwable t) {
                        t.printStackTrace();
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (Throwable t) {
                                // do nothing
                                t.printStackTrace();;
                            }
                        }
                    }

                    if (file != null) {
                        try {
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                // do nothing
                            }
                        });
                    }
                }
                // write bitmap to a file

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        initIntentFilter(intentFilter);

        registerReceiver(mBroadcastReceiver, intentFilter);

        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(ACTION_YOUTUBE_CAPTURE_START);
        localIntentFilter.addAction(ACTION_YOUTUBE_CAPTURE_READY);
        localIntentFilter.addAction(ACTION_YOUTUBE_CAPTURE_ALMOST);
        localIntentFilter.addAction(ACTION_YOUTUBE_CAPTURE_FINISH);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, localIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mNotificationManager != null) {
            stopForeground(true);
        } else {
            stopSelf();
        }

        try {
            unregisterReceiver(mBroadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        mHandlerThread.quit();
    }

    protected void initIntentFilter(IntentFilter intentFilter) {
        // do something
        intentFilter.addAction(ACTION_MUSIC_STATECHANGED);
        intentFilter.addAction("com.android.music.metachanged");
        intentFilter.addAction("com.android.music.queuechanged");
        intentFilter.addAction("com.android.music.playbackcomplete");
    }

    protected boolean isReady() {
        return true;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isReady() == false) {
            updateNotification();
            stopSelf();
            return START_NOT_STICKY;
        }
        if (ACTION_START.equals(intent.getAction()) == true) {
            startSming();
        } else if (ACTION_STOP.equals(intent.getAction()) == true) {
            stopSming();
        }
        return START_NOT_STICKY;
    }

    protected void terminate() {
        if (mLastSmingData != null) {
            mLastSmingData.clear();
            mLastSmingData = null;
        }

        stopForeground(true);
    }

    protected void afterTerminate() {
        mMainHandler.removeCallbacksAndMessages(null);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_STOP));

        stopSelf();
    }

    protected abstract void startSming();
    protected abstract void stopSming();

    protected abstract String getMusicPlayingNotiTitle();

    protected File makeDirectory(SmingData data) {
//        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File smingFoler = Constant.getSmingFolder();

        File result = null;
        if (smingFoler != null) {
            if (smingFoler.exists() == false) {
                smingFoler.mkdir();
            }

            String storePath = smingFoler.getAbsolutePath();
            if (storePath.endsWith("/") == false) {
                storePath += "/";
            }

            if (data != null) {
                int folderType = SmingManager.getInstance().getFolderType();
                String artist = data.getVaildArtistName();
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(data.startMills);
                String dateText = String.format("%d월 %d일", date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
                switch (folderType) {
                    case Constant.FOLDER_ARTIST:
                        storePath += artist;
                        break;
                    case Constant.FOLDER_DATE:
                        storePath += dateText;
                        break;
                    case Constant.FOLDER_ARTIST_DATE:
                        storePath += artist + "/" + dateText;
                        break;
                    case Constant.FOLDER_DATE_ARTIST:
                        storePath += dateText + "/" + artist;
                        break;
                }
                if (folderType != Constant.FOLDER_ALL) {
                    storePath += "/";
                }
            }



            result = new File(storePath);

            if (result.exists() == false) {
                boolean success = result.mkdirs();
                if (!success) {
                    Log.e(TAG, "failed to create file storage directory.");
                    return null;
                }
            }
        } else {
            Log.e(TAG, "failed to create file storage directory, getExternalFilesDir is null.");
            return null;
        }

        return result;
    }

    protected abstract boolean isRunning();

    protected NotificationManager mNotificationManager;
    protected NotificationCompat.Builder mNotifyBuilder;
    protected static final int notifyID = 1;

    protected void updateNotification() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (isRunning() == false) {
            mNotificationManager.cancel(notifyID);
            mNotifyBuilder = null;
            return;
        }

        String title, text;

        if (mLastSmingData == null) {
            title = "재생 대기중";
            text = "음악 좀 틀어봐요";
        } else {
            title = mLastSmingData instanceof YoutubeSmingData ? "유튭 캡쳐중" : getMusicPlayingNotiTitle();
            text = mLastSmingData.getNotiText();

        }
        // Sets an ID for the notification, so it can be updated
        boolean first = false;
        if (mNotifyBuilder == null) {
            Intent stopIntent = new Intent(MusicListenService.this, this.getClass());
            stopIntent.setAction(ACTION_STOP);

            mNotifyBuilder = new NotificationCompat.Builder(this)
                    .setOngoing(true)
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setVibrate(new long[]{0})
                    .setSmallIcon(R.drawable.status_rec_music)
                    .addAction(new NotificationCompat.Action(R.drawable.ic_stop_white_24dp, "정지", PendingIntent.getService(this, 0, stopIntent, 0)));

            first = true;
        } else {
            mNotifyBuilder
                    .setPriority(Notification.PRIORITY_MAX)
                    .setVibrate(null);
        }

        mNotifyBuilder
                .setContentTitle(title)
                .setContentText(text);

        if (first == true) {
            startForeground(notifyID, mNotifyBuilder.build());
        } else {
            mNotificationManager.notify(
                    notifyID,
                    mNotifyBuilder.build());
        }
    }



    private Runnable mClearSongRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLastSmingData != null) {
                mLastSmingData.clear();
                mLastSmingData = null;
            }

            updateNotification();
        }
    };

    protected Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (SmingManager.getInstance().isVibrateSongEnd() == true) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(SmingManager.getInstance().getVibratePattern(), -1);
            }
        }
    };

    protected boolean handleBraodcast(Context context, Intent intent) {
        return false;
    }

    private static final long MILLS_SECOND = 1000;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (handleBraodcast(context, intent) == true) {
                return;
            }

            if (isRunning() == false) {
                return;
            }

            String action = intent.getAction();
            Log.d("CCC_d", "action - " + action);

            switch (action) {
                case ACTION_YOUTUBE_CAPTURE_START:
                    if (mLastSmingData != null) {
                        mLastSmingData.clear();
                    }
                    String title = intent.getStringExtra("title");
                    mLastSmingData = new YoutubeSmingData(title);

                    isPlaying = true;
                    updateNotification();
                    break;
                case ACTION_YOUTUBE_CAPTURE_READY :
                    if (mLastSmingData != null && mLastSmingData instanceof YoutubeSmingData) {
                        ((YoutubeSmingData) mLastSmingData).captureReady();
                    }

                    break;
                case ACTION_YOUTUBE_CAPTURE_ALMOST :
                    if (mLastSmingData != null && mLastSmingData instanceof YoutubeSmingData) {
                        ((YoutubeSmingData) mLastSmingData).runOutTime();
                    }

                    break;
                case ACTION_YOUTUBE_CAPTURE_FINISH :
                    if (mLastSmingData != null) {
                        if (mLastSmingData.canDrop() == true) {
                            dropImage(mLastSmingData);
                        } else {
                            mLastSmingData.clear();
                        }

                        mLastSmingData = null;
                    }

                    isPlaying = false;
                    updateNotification();
                    break;
                case ACTION_MUSIC_STATECHANGED : {

                    String artist = intent.getStringExtra("artist");
                    String album = intent.getStringExtra("album");
                    String track = intent.getStringExtra("track");

                    boolean playing = intent.getBooleanExtra("playing", false);
                    long duration = intent.getLongExtra("duration", 0);

                    synchronized (MusicListenService.this) {
                        if (playing == false) {
                            if (mLastSmingData != null) {
                                if (mLastSmingData.canDrop() == true) {
                                    dropImage(mLastSmingData);
                                } else {
                                    mLastSmingData.clear();
                                }
                            }
                            mLastSmingData = null;
                            mMainHandler.removeMessages(0);
                        } else {
                            if (mLastSmingData != null) {
                                if (mLastSmingData.canDrop() == true) {
                                    dropImage(mLastSmingData);
                                } else {
                                    mLastSmingData.clear();
                                }
                            }

                            mLastSmingData = new SmingData(artist, track, duration);
                            mMainHandler.removeMessages(0);
                            if (duration > 17 * MILLS_SECOND) {
                                mMainHandler.sendEmptyMessageDelayed(0, duration - MILLS_SECOND * 15);
                            }
                        }
                    }
                    isPlaying = playing;
                    updateNotification();
                }
                break;
            }
        }
    };

    protected void dropImage(SmingData smingData) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(0, smingData), mTermToDrop);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (mNotificationManager != null) {
            stopForeground(true);
        } else {
            stopSelf();
        }
    }
}

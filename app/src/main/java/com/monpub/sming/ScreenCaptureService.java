package com.monpub.sming;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.monpub.sming.etc.Util;
import com.monpub.sming.sming.SmingManager;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@TargetApi(21)
public class ScreenCaptureService extends MusicListenService {
    private static int IMAGES_PRODUCED;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private static MediaProjection sMediaProjection;

    public static void setMediaProjection(MediaProjection mediaProjection) {
        if (mediaProjection == null) {
            return;
        }
        sMediaProjection =  mediaProjection;
    }

    public ScreenCaptureService() {
        super();
    }

    private ImageReader mImageReader;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private DisplayMetrics mDisplayMetrics;

    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;

    private boolean isScreenOff;

    @Override
    protected void initIntentFilter(IntentFilter intentFilter) {
        super.initIntentFilter(intentFilter);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            if (wakeLock.isHeld() == true) {
                wakeLock.release();
            }
            wakeLock = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private PowerManager.WakeLock wakeLock;

    @Override
    protected boolean isRunning() {
        return sMediaProjection != null;
    }

    @Override
    protected boolean isReady() {
        return isRunning() == true;
    }

    @Override
    protected void startSming() {
        synchronized (ScreenCaptureService.this) {
            if (sMediaProjection == null) {
                updateNotification();
                stopSelf();
                return;
            }
            isScreenOff = false;
            startCapture();
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"MyWakelockTag");
            wakeLock.acquire();
        }
    }

    @Override
    protected void stopSming() {
        if (wakeLock != null && wakeLock.isHeld() == true) {
            wakeLock.release();
            wakeLock = null;
        }

        if (sMediaProjection == null) {
            terminate();
            afterTerminate();
            return;
        }

        stopCapture();
    }

    public boolean acceptNewShot() {
        if (mLastSmingData == null) {
            return false;
        }

        return mLastSmingData.acceptNewShot() == true;
    }

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            boolean locked = km.inKeyguardRestrictedInputMode();

            Image image = null;
            Bitmap dumpBitmap = null;

            try {
                image = mImageReader.acquireLatestImage();

                if (isScreenOff == true) {
                    return;
                }

                if (locked == true) {
                    return;
                }

                if (isPlaying == false) {
                    return;
                }

                if (mLastSmingData == null) {
                    return;
                }

                if (acceptNewShot() == false) {
                    return;
                }

                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    int bitmapWidth = mWidth + rowPadding / pixelStride;
                    int bitmapHeight = mHeight;

                    // create bitmap
                    dumpBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                    dumpBitmap.copyPixelsFromBuffer(buffer);

                    Bitmap bitmap = Bitmap.createBitmap(dumpBitmap, 0, 0, image.getWidth(), image.getHeight());

                    synchronized (ScreenCaptureService.this) {
                        if (mLastSmingData != null) {
                            boolean drawStatusCover = SmingManager.getInstance().overdrawStatus();

                            if (drawStatusCover == true) {

                                Canvas canvas = new Canvas(bitmap);
                                Paint paint = new Paint();
                                paint.setColor(0xFF333333);

                                int statusWidth = bitmap.getWidth();
                                int statusHeight = (int) (Util.dp2px(mDisplayMetrics, 25f) * ((float) statusWidth / mDisplayMetrics.widthPixels));

                                canvas.drawRect(0, 0, statusWidth, statusHeight, paint);

                                int textSize = (int) (Util.dp2px(mDisplayMetrics, 13f) * ((float) statusWidth / mDisplayMetrics.widthPixels));

                                paint.setColor(Color.WHITE);
                                paint.setDither(true);
                                paint.setAntiAlias(true);
                                paint.setTextSize(textSize);
                                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

                                String text = new SimpleDateFormat("aa  KK:mm:ss").format(Calendar.getInstance().getTime());
                                int textWidth = (int) paint.measureText(text);
                                Rect textBound = new Rect();
                                paint.getTextBounds(text, 0, text.length(), textBound);

                                canvas.drawText(text, (statusWidth - textWidth) / 2, -textBound.top + (statusHeight - textBound.height()) / 2, paint);
                            }

                            boolean isFirstShot = mLastSmingData.beforeFirstShot();
                            mLastSmingData.putNewShot(bitmap);

                            if (isFirstShot == true && SmingManager.getInstance().isVibrateFirstShot() == true) {
                                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                vibrator.vibrate(SmingManager.getInstance().getVibratePattern(), -1);

                            }

                            if (mLastSmingData.checkDropTime() == true && mLastSmingData.canDrop() == true) {
                                dropImage(mLastSmingData);
                            }

                            if (mLastSmingData.checkClearPlayTime() == true) {
                                mLastSmingData.clear();
                                mLastSmingData = null;
                                updateNotification();
                            }
                        }
                    }

                    IMAGES_PRODUCED++;
                    Log.e(TAG, "captured image: " + IMAGES_PRODUCED);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (image!=null) {
                    image.close();
                }
                if (dumpBitmap != null) {
                    dumpBitmap.recycle();
                }
            }
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {
        public OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            synchronized (this) {
                final int rotation = mDisplay.getRotation();
                if (rotation != mRotation) {
                    mRotation = rotation;
                    try {
                        // clean up
                        if(mVirtualDisplay != null) mVirtualDisplay.release();
                        if(mImageReader != null) mImageReader.setOnImageAvailableListener(null, null);

                        // re-create virtual display depending on device width / height
                        createVirtualDisplay();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private MediaProjectionStopCallback mediaProjectionStopCallback;

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        private final boolean registed;

        private MediaProjectionStopCallback(boolean registed) {
            this.registed = registed;
        }

        @Override
        public void onStop() {
            Log.e("ScreenCapture", "stopping projection.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (ScreenCaptureService.this) {
                        if (mImageReader != null) {
                            mImageReader.setOnImageAvailableListener(null, null);
                        }
                        if (mOrientationChangeCallback != null) {
                            mOrientationChangeCallback.disable();
                        }
                        if (sMediaProjection != null && registed == true) {
                            sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                        }

                        mImageReader = null;
                        mOrientationChangeCallback = null;
                        sMediaProjection = null;

                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                afterTerminate();
                            }
                        });
                    }
                }
            });
        }
    }

    private void startCapture() {
        if (sMediaProjection != null) {
            makeDirectory(null);

            // display metrics
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mDensity = metrics.densityDpi;
            WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            mDisplay = window.getDefaultDisplay();

            // create virtual display depending on device width / height
            createVirtualDisplay();

            // register orientation change callback
            mOrientationChangeCallback = new OrientationChangeCallback(this);
            if (mOrientationChangeCallback.canDetectOrientation()) {
                mOrientationChangeCallback.enable();
            }

            // register media projection stop callback
            if (hasStopCallbackBug() == false) {
                mediaProjectionStopCallback = new MediaProjectionStopCallback(true);
                sMediaProjection.registerCallback(mediaProjectionStopCallback, mHandler);
            } else {
                mediaProjectionStopCallback = new MediaProjectionStopCallback(false);
            }

            updateNotification();

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
        }
    }
    private void stopCapture () {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) {
                        mVirtualDisplay.release();
                        mVirtualDisplay = null;
                    }

                    if (sMediaProjection != null) {
                        sMediaProjection.stop();
                        if (mediaProjectionStopCallback != null && mediaProjectionStopCallback.registed == false) {
                            mediaProjectionStopCallback.onStop();
                            mediaProjectionStopCallback = null;
                        }
                    }
                }
            });
        }

        terminate();
    }

    @Override
    protected void terminate() {
        super.terminate();

        if (isScreenOff == true) {
            mNotifyBuilder = new NotificationCompat.Builder(ScreenCaptureService.this)
                    .setOngoing(true)
                    .setContentIntent(PendingIntent.getActivity(ScreenCaptureService.this, 0, new Intent(ScreenCaptureService.this, MainActivity.class), 0))
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setVibrate(new long[]{10, 500})
                    .setSmallIcon(R.drawable.status_rec_music)
                    .setContentTitle("자동 캡쳐 강제 종료됨")
                    .setContentText("화면이 꺼져서 종료되었습니다.")
                    .setOngoing(false)
                    .setAutoCancel(true)
                    .setShowWhen(true);

            mNotificationManager.notify(notifyID, mNotifyBuilder.build());
            mNotifyBuilder = null;
        }
    }

    @Override
    protected String getMusicPlayingNotiTitle() {
        return "캡쳐 중";
    }

    /****************************************** Factoring Virtual Display creation ****************/

    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        mDisplay.getRealSize(size);
        mWidth = size.x;
        mHeight = size.y;
        mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);

        if (SmingManager.getInstance().isSaveScreenshot() == false) {
            if (mWidth > SmingManager.getInstance().getSmingHalfWidth()) {
                float downScale = (float) SmingManager.getInstance().getSmingHalfWidth() / mWidth;
                mWidth *= downScale;
                mHeight *= downScale;
                mHeight += 2;
            }
        }

        // start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
    }

    @Override
    protected boolean handleBraodcast(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                isScreenOff = true;
                stopSming();
                return true;
            case Intent.ACTION_SCREEN_ON:
                isScreenOff = false;
                return true;
        }
        return false;
    }

    private boolean hasStopCallbackBug() {
        return Build.VERSION.SDK_INT <= 21;
    }
}

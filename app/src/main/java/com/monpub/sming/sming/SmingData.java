package com.monpub.sming.sming;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.monpub.sming.CaptureObserveService;
import com.monpub.sming.R;
import com.monpub.sming.etc.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by small-lab on 2016-08-07.
 */
public class SmingData {
    protected static SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyMMdd_HHmmss");
    }

    private String startShotPath;
    private String endShotPath;

    private Bitmap startShotBitmap;
    private Bitmap endShotBitmap;
    private Bitmap endSecondShotBitmap;

    public final String artist;
    public final String song;

    public final long startMills;
    public final long duration;

    private long mLastShotMills;
    private final String filename;

    private boolean alreadyDrop = false;

    public SmingData(String artist, String song, long duration) {
        this.artist = artist;
        this.song = song;
        this.startMills = mLastShotMills = System.currentTimeMillis();
        this.duration = duration;

        this.filename = null;
    }

    public SmingData(String filename, String startShotPath, String endShotPath) {
        this.artist = null;
        this.song = null;
        this.startMills = mLastShotMills = System.currentTimeMillis();
        this.duration = 0;

        this.filename = filename;
        this.startShotPath = startShotPath;
        this.endShotPath = endShotPath;
    }

    public boolean beforeFirstShot() {
        return startShotBitmap == null;
    }

    public void putNewShot(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        mLastShotMills = System.currentTimeMillis();
        if (startShotBitmap == null) {
            startShotBitmap = bitmap;
            return;
        }

        if (duration == 0 && endShotBitmap != null) {
            if (endSecondShotBitmap != null) {
                endSecondShotBitmap.recycle();
            }

            endSecondShotBitmap = endShotBitmap;
        }

        endShotBitmap = bitmap;
    }

    public void putNewShotPath(String path) {
        if (TextUtils.isEmpty(path) == true) {
            return;
        }

        mLastShotMills = System.currentTimeMillis();
        if (startShotPath == null) {
            startShotPath = path;
            return;
        }

        endShotPath = path;

    }

    public boolean isNearEnd() {
        if (duration < 1000) {
            return false;
        }

        return System.currentTimeMillis() > (startMills + duration - 10 * 1000);
    }

    public long getLastShotMills() {
        return mLastShotMills;
    }

    private static final int MARK_TEXT_SIZE = 18;

    private Bitmap getBitmapFromPath(String path) {
        return getBitmapFromPath(path, false);
    }

    private Bitmap getBitmapFromPath(String path, boolean keepSize) {
        float smingHalfWidth = SmingManager.getInstance().getSmingHalfWidth();

        BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

        if (keepSize == false) {
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(path, options);

            if (options.outWidth >= smingHalfWidth * 2 || options.outHeight >= smingHalfWidth * 4) {
                int sampleSize = 1;
                while ((options.outWidth / sampleSize) >= smingHalfWidth * 2 || (options.outHeight / sampleSize) >= smingHalfWidth * 4) {
                    sampleSize *= 2;
                }
                decodeOptions.inSampleSize = sampleSize;
            }
        }

        Bitmap src = BitmapFactory.decodeFile(path, decodeOptions);

        if (src == null) {
            throw new CaptureObserveService.CaptureFileNotReadyException();
        }

        if (keepSize == true) {
            return src;
        }

        float scale = (float) SmingManager.getInstance().getSmingHalfWidth() / src.getWidth();
        int scaledHeight = (int) (src.getHeight() * scale);

        Bitmap scaled = Bitmap.createScaledBitmap(src, SmingManager.getInstance().getSmingHalfWidth(), scaledHeight, true);
        if (src != scaled) {
            src.recycle();
        }

        return scaled;
    }

    public boolean checkDropTime() {
        if (duration < 1000) {
            return false;
        }

        long dropTiming = startMills + duration - 3000;
        return System.currentTimeMillis() > dropTiming;
    }

    public boolean checkClearPlayTime() {
        if (duration < 1000) {
            return false;
        }

        return System.currentTimeMillis() > startMills + duration;
    }

    public boolean isAlreadyDrop() {
        return alreadyDrop;
    }

    public boolean canDrop() {
        if (alreadyDrop == true) {
            return false;
        }

        if ((startShotBitmap == null && endShotBitmap == null)
                && (startShotPath != null && endShotPath != null)) {
            return true;
        }

        return startShotBitmap != null && startShotBitmap.isRecycled() == false && endShotBitmap != null && endShotBitmap.isRecycled() == false;
    }

    public Bitmap getStartShotBitmap() {
        if (startShotBitmap != null && startShotBitmap.isRecycled() == false) {
            return startShotBitmap;
        }

        if (TextUtils.isEmpty(startShotPath) == true) {
            return null;
        }

        return startShotBitmap = getBitmapFromPath(startShotPath, true);
    }

    public Bitmap getEndShotBitmap() {
        if (endShotBitmap != null && endShotBitmap.isRecycled() == false) {
            return endShotBitmap;
        }

        if (TextUtils.isEmpty(endShotPath) == true) {
            return null;
        }

        return endShotBitmap = getBitmapFromPath(endShotPath, true);
    }

    public Bitmap getDropBitmap(Context context) {
        if ((startShotBitmap == null && endShotBitmap == null)
                && (startShotPath != null && endShotPath != null)) {
            startShotBitmap = getBitmapFromPath(startShotPath);
            endShotBitmap = getBitmapFromPath(endShotPath);
        } else {
            if (mLastShotMills > System.currentTimeMillis() - 2000) {
                if (endSecondShotBitmap != null && endSecondShotBitmap.isRecycled() == false) {
                    if (endShotBitmap != null) {
                        endShotBitmap.recycle();
                    }

                    endShotBitmap = endSecondShotBitmap;
                }
            }
        }

        if (startShotBitmap == null || startShotBitmap.isRecycled() == true || endShotBitmap == null | endShotBitmap.isRecycled() == true) {
            return null;
        }

        int width = startShotBitmap.getWidth() + endShotBitmap.getWidth();
        int height = startShotBitmap.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        bitmap.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        canvas.drawBitmap(startShotBitmap, 0, 0, paint);
        canvas.drawBitmap(endShotBitmap, startShotBitmap.getWidth(), 0, paint );

        int smingWidth = SmingManager.getInstance().getSmingWdith();
        float scale = (float) smingWidth / bitmap.getWidth();

        if (scale != 1.0f) {
            Bitmap scaledTemp = Bitmap.createScaledBitmap(bitmap, smingWidth, (int) (bitmap.getHeight() * scale), true);
            if (scaledTemp != bitmap) {
                bitmap.recycle();
                canvas = new Canvas(scaledTemp);
                bitmap = scaledTemp;
            }
        }

        int strokeWidth = Util.dp2px(context, 1f);
        int strokeHeight = Util.dp2px(context, 8f);

        Paint dashPaint = new Paint();
        dashPaint.setARGB(0x19, 0xff, 0xff, 0xff);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeWidth(strokeWidth);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{strokeHeight, strokeHeight / 2}, 0));
        canvas.drawLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight(), dashPaint);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int textSize = (int) (Util.dp2px(displayMetrics, MARK_TEXT_SIZE) * ((float) (width / 2) / displayMetrics.widthPixels));

        Paint markPaint = new Paint();
        markPaint.setDither(true);
        markPaint.setAntiAlias(true);
        markPaint.setFilterBitmap(true);
        markPaint.setTextSize(textSize);

        String dateText = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());

        Rect rect = new Rect();
        markPaint.getTextBounds(dateText, 0, dateText.length(), rect);

        int textWidth = rect.width();
        int textHeight = rect.height();

        Path path = new Path();
        path.moveTo(0, height);
        path.lineTo(width, height);
        path.close();

        markPaint.setColor(Color.BLACK);
        markPaint.setStyle(Paint.Style.STROKE);
        markPaint.setStrokeWidth(textSize / 5);
        canvas.drawTextOnPath(dateText, path, 5, -5, markPaint);

        markPaint.setColor(Color.WHITE);
        markPaint.setStyle(Paint.Style.FILL);
        canvas.drawTextOnPath(dateText, path, 5, -5, markPaint);


        alreadyDrop = true;
        return bitmap;
    }

    protected static final String INVALID_FILENAME_REGEX = "[\\|\\\\\\?\\*\\<\\\"\\:\\>\\+\\[\\]\\/\\']";
    public String getDropName() {
        return getDropName(null);
    }

    public String getDropName(String postfix) {

        String name;
        if (TextUtils.isEmpty(filename) == false) {
            name = filename;
        } else {
            name = artist;
            if (TextUtils.isEmpty(song) == false) {
                name += "_" + song;
            }
        }
        Date date = new Date(startMills);

        name = name.replaceAll(INVALID_FILENAME_REGEX, "_");
        return name + "_" + sdf.format(date) + (TextUtils.isEmpty(postfix) == false ? postfix : "") + ".png";
    }

    public void clear() {
        if (startShotBitmap != null) {
            startShotBitmap.recycle();
            startShotBitmap = null;
        }
        if (endShotBitmap != null) {
            endShotBitmap.recycle();
            endShotBitmap = null;
        }
        if (endSecondShotBitmap != null) {
            endSecondShotBitmap.recycle();
            endSecondShotBitmap = null;
        }

    }

    public String getVaildArtistName() {
        String artist = this.artist;
        if (TextUtils.isEmpty(artist) == true) {
            return "알수없음";
        }
        return artist.replaceAll(INVALID_FILENAME_REGEX, "_");
    }

    public boolean equals(String artist, String track) {
        if (this.artist.equals(artist) == true
                && ((TextUtils.isEmpty(track) == true && TextUtils.isEmpty(this.song) == true) || (this.song.equals(track) == true))) {
            return true;
        }

        return false;
    }

    private static final long TERM_SHOT = 3 * 1000;
    private static final long FIRST_TERM_SHOT = 5 * 1000;
    private static final long NEAR_END_THERM_SHOT = 1 * 1000;

    public boolean acceptNewShot() {
        if (isAlreadyDrop() == true) {
            return false;
        }

        if (isNearEnd() == true && getLastShotMills() + NEAR_END_THERM_SHOT <= System.currentTimeMillis()) {
            return true;
        }

        if (startMills + FIRST_TERM_SHOT > System.currentTimeMillis()) {
            return false;
        }

        if (getLastShotMills() + TERM_SHOT > System.currentTimeMillis()) {
            return false;
        }

        return true;
    }

    public String getNotiText() {
        String text = artist;
        if (TextUtils.isEmpty(song) == false) {
            text += " - " + song;
        }

        return text;
    }
}

package com.monpub.sming;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.IntDef;

import java.io.File;

/**
 * Created by small-lab on 2016-08-24.
 */
public final class Constant {
    public static final int MIN_SMING_WIDTH = 840;

    public static final int FOLDER_ALL          = 0;
    public static final int FOLDER_ARTIST       = 1;
    public static final int FOLDER_DATE         = 2;
    public static final int FOLDER_ARTIST_DATE = 3;
    public static final int FOLDER_DATE_ARTIST = 4;

    public static final int VIBRATE_POWER_LOW = 0;
    public static final int VIBRATE_POWER_MID = 1;
    public static final int VIBRATE_POWER_HIGH = 2;


    public static void makeAppDefaultDirectory(Context context) {
        File appDirecotry = Environment.getExternalStoragePublicDirectory("Sming");
        File appFontsDirectory = new File(appDirecotry, "Fonts");
        File stickerDirectory = new File(appDirecotry, "Stickers");

        if (appFontsDirectory.exists() == false) {
            appFontsDirectory.mkdirs();
        }
        if (stickerDirectory.exists() == false) {
            stickerDirectory.mkdirs();
        }

        File nomedia = new File(appDirecotry, ".nomedia");
        if (nomedia.exists() == false || nomedia.isFile() == false) {
            try {
                nomedia.createNewFile();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{appFontsDirectory.getAbsolutePath(), stickerDirectory.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                // do nothing
            }
        });

    }

    public static File getFontDirectory() {
        File appDirecotry = Environment.getExternalStoragePublicDirectory("Sming");
        File appFontsDirectory = new File(appDirecotry, "Fonts");

        if (appFontsDirectory.exists() == false) {
            appFontsDirectory.mkdirs();
        }

        return appFontsDirectory;
    }

    public static File getStickerDirectory() {
        File appDirecotry = Environment.getExternalStoragePublicDirectory("Sming");
        File stickerDirectory = new File(appDirecotry, "Stickers");

        if (stickerDirectory.exists() == false) {
            stickerDirectory.mkdirs();
        }

        return stickerDirectory;
    }

    public static File getSmingFolder() {
        File externalFilesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String storePath = externalFilesDir.getAbsolutePath() + "/sming/";
        File storeDirectory = new File(storePath);

        return storeDirectory;
    }

    public static File getScreenshotFolder() {
        File externalFilesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File screenshotFolder = new File(externalFilesDir, "Screenshots");

        if (screenshotFolder.exists() == false) {
            screenshotFolder.mkdirs();
        }

        return screenshotFolder;
    }


    public static long[][] VIBRATE_POWER_SETTTING = {
            {0, 30,5,30,5,30,5,30},
            {0, 100,30,100,30,100},
            {0, 500},
    };
}

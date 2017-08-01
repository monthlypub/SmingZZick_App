package com.monpub.sming.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Created by small-lab on 2016-09-05.
 */
public class ImageStickerAttachInfo extends StickerAttachInfo {
    public ImageStickerAttachInfo(long attachId, ImageSticker sticker, int index, float posX, float posY, float angle, float widthRatio, float alpha) {
        super(attachId, sticker, index, posX, posY, angle, widthRatio, alpha);
    }

    public ImageStickerAttachInfo(long attachId, String stickerId, int index, float posX, float posY, float angle, float widthRatio, float alpha) {
        super(attachId, stickerId, index, posX, posY, angle, widthRatio, alpha);
    }

    @Override
    public void draw(Context context, Canvas canvas) {
        ImageSticker imageSticker = (ImageSticker) sticker;
        if (imageSticker.imageFile == null || imageSticker.imageFile.exists() == false) {
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        if (alpha  < 1f) {
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imageSticker.imageFile.getAbsolutePath(), options);
        if (alpha < 1f && bitmap.hasAlpha() == false) {
            Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas newCanvas = new Canvas(newBitmap);
            newCanvas.drawBitmap(bitmap, 0, 0, null);

            bitmap.recycle();
            bitmap = newBitmap;
        }

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();

        float scale = (canvasWidth * widthRatio) / bitmapWidth ;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmapWidth * scale), (int) (bitmapHeight * scale), true);
        if (scaledBitmap != bitmap) {
            bitmap.recycle();
        }

        bitmap = scaledBitmap;
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.reset();

        matrix.postTranslate((canvasWidth - bitmapWidth) / 2, (canvasHeight - bitmapHeight) / 2);
        matrix.postRotate(angle, canvasWidth / 2, canvasHeight / 2);
        matrix.postTranslate(canvasWidth / 2 * posX, canvasHeight / 2 * posY);

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAlpha((int) (alpha * 255));
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawBitmap(bitmap, matrix, paint);

        bitmap.recycle();
    }
}

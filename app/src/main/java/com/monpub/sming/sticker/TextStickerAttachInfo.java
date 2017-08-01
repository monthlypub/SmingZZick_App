package com.monpub.sming.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import com.monpub.sming.etc.Util;
import com.monpub.textmaker.TextMakingInfo;

import java.io.File;

/**
 * Created by small-lab on 2016-09-05.
 */
public class TextStickerAttachInfo extends StickerAttachInfo {
    public TextStickerAttachInfo(long attachId, TextSticker sticker, int index, float posX, float posY, float angle, float widthRatio, float alpha) {
        super(attachId, sticker, index, posX, posY, angle, widthRatio, alpha);
    }

    public TextStickerAttachInfo(long attachId, String stickerId, int index, float posX, float posY, float angle, float widthRatio, float alpha) {
        super(attachId, stickerId, index, posX, posY, angle, widthRatio, alpha);
    }

    @Override
    public void draw(Context context, Canvas canvas) {
        TextSticker textSticker = (TextSticker) sticker;
        if (textSticker.textFile == null || textSticker.textFile.exists() == false) {
            return;
        }

        TextMakingInfo textMakingInfo = textSticker.getTextMakeInfo(context);

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        float targetWidthDP = Util.px2dp(context, canvasWidth * widthRatio);
        float textSize = targetWidthDP / textMakingInfo.getWidthTextRatio();

        textMakingInfo.setTextSize(textSize);

        Rect rect = new Rect();
        textMakingInfo.getTextRect(context, rect);

        float textRectWidth = rect.width();
        float textRectHeight = rect.height();

        Matrix matrix = new Matrix();
        matrix.reset();

        matrix.postTranslate((canvasWidth - textRectWidth) / 2, (canvasHeight - textRectHeight) / 2);
        matrix.postRotate(angle, canvasWidth / 2, canvasHeight / 2);
        matrix.postTranslate(canvasWidth / 2 * posX, canvasHeight / 2 * posY);

        canvas.setMatrix(matrix);
        canvas.saveLayerAlpha(0, 0, textRectWidth, textRectHeight, (int) (alpha * 255), Canvas.ALL_SAVE_FLAG);
        textMakingInfo.draw(context, (int) textRectWidth, (int) textRectHeight, canvas);
        canvas.restore();
        matrix.reset();
        canvas.setMatrix(matrix);
    }
}

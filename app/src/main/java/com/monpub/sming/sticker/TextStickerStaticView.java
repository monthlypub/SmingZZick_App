package com.monpub.sming.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.monpub.sming.etc.Util;
import com.monpub.textmaker.TextMakingInfo;

/**
 * Created by small-lab on 2016-09-16.
 */
public class TextStickerStaticView extends View {

    private TextMakingInfo textMakingInfo;

    public TextStickerStaticView(Context context) {
        super(context);
    }

    public TextStickerStaticView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextStickerStaticView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextStickerStaticView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TextMakingInfo getTextMakingInfo() {
        return textMakingInfo;
    }

    public void setTextMakingInfo(TextMakingInfo textMakingInfo) {
        this.textMakingInfo = textMakingInfo;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (textMakingInfo == null) {
            return;
        }

        float width = canvas.getWidth();
        float height = canvas.getHeight();

        float widthTextSize = width / textMakingInfo.getWidthTextRatio();
        float heightTextSize = height / textMakingInfo.getHeightTextRatio();

        float textSize = Math.min(widthTextSize, heightTextSize);

        textMakingInfo.setTextSize(Util.px2dp(getContext(), textSize));

        if (textMakingInfo != null && (width * height > 0)) {
            textMakingInfo.draw(getContext(), (int) width, (int) height, canvas);
        }
    }
}

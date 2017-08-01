package com.monpub.sming.sticker;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.monpub.sming.R;
import com.monpub.sming.etc.Util;
import com.monpub.textmaker.TextMakingInfo;
import com.monpub.textmaker.TextPreviewView;

/**
 * Created by small-lab on 2016-09-03.
 */
public class StickerTextView extends TextPreviewView implements Stickerable<TextSticker> {
    private String id;
    private float defautlTextSize = -1;
    private float defaultTextScale = 1f;
    private long attachId;

    public StickerTextView(Context context) {
        super(context);
        init(context);
    }

    public StickerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public StickerTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        attachId = System.currentTimeMillis();

        setAutoResize(false);
        setBackgroundResource(R.drawable.selector_sticker_apply_bg);
    }

    @Override
    public float getRelativeWidthRatio(int smingWidth) {
        float widthTextRatio = textMakingInfo.getWidthTextRatio();
        float heightTextRatio = textMakingInfo.getHeightTextRatio();

        float biggerTextRatio;
        float relativeRatio = 0.4f;
        if (widthTextRatio < heightTextRatio) {
            relativeRatio *= widthTextRatio / heightTextRatio;
        }

        return relativeRatio;
    }

    @Override
    public float getRelativeHeightRatio(int smingHeight) {
        float widthTextRatio = textMakingInfo.getWidthTextRatio();
        float heightTextRatio = textMakingInfo.getHeightTextRatio();

        float relativeRatio = 0.4f;
        if (heightTextRatio < widthTextRatio) {
            relativeRatio *= heightTextRatio / widthTextRatio;
        }

        return relativeRatio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (defautlTextSize <= 0) {
            int width = getLayoutParams().width;
            if (width > 0) {
                float widthTextRatio = textMakingInfo.getWidthTextRatio();

                defautlTextSize = Util.px2dp(getContext(), width) / widthTextRatio;
                setTextSize(defautlTextSize);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setData(TextSticker sticker) {
        setTextMakingInfo(sticker.getTextMakeInfo(getContext()));
        id = sticker.id;
    }

    @Override
    public void setAngle(float angle) {
        setRotation(angle);
    }

    @Override
    public void setScale(float scale) {
        setTextScale(scale * defaultTextScale);
        requestLayout();
    }

    @Override
    public void setStickerAlpha(float alpha) {
        setAlpha(alpha);
    }

    @Override
    public float getAngle() {
        return getRotation();
    }

    @Override
    public void setPosX(float x) {
        int width = ((View) getParent()).getMeasuredWidth();
        if (Math.abs(x) > width / 2) {
            x = width / 2 * (x / Math.abs(x));
        }
        setTranslationX(x);
    }

    @Override
    public void setPosY(float y) {
        int height = ((View) getParent()).getMeasuredHeight();
        if (Math.abs(y) > height / 2) {
            y = height / 2 * (y / Math.abs(y));
        }

        setTranslationY(y);
    }

    @Override
    public float getPosX() {
        return getTranslationX();
    }

    @Override
    public float getPosY() {
        return getTranslationY();
    }

    @Override
    public float getScale() {
        return getTextScale() / defaultTextScale;
    }

    @Override
    public float getStickerAlpha() {
        return getAlpha();
    }

    @Override
    public void setStickerSelected(boolean select) {
        setSelected(select);
    }

    @Override
    public boolean isStickerSelected() {
        return isSelected();
    }

    @Override
    public String getStickerId() {
        return id;
    }

    @Override
    public void setStickerId(String id) {
        this.id = id;
    }

    @Override
    public StickerAttachInfo getAttachInfo(View board, int index) {
        float boardWidth = board.getWidth();
        float boardHeight = board.getHeight();

        float posX = getPosX() / (boardWidth / 2);
        float posY = getPosY() / (boardHeight / 2);
        float angle = getAngle();
        float widthRatio = (getLayoutParams().width * getScale() * defaultTextScale) / boardWidth;
        float alpha = getStickerAlpha();

        return new TextStickerAttachInfo(attachId, id, index, posX, posY, angle, widthRatio, alpha);
    }

    @Override
    public void applyAttachInfo(StickerAttachInfo info, View board) {
        float boardWidth = board.getWidth();
        float boardHeight = board.getHeight();

        setPosX(boardWidth / 2 * info.posX);
        setPosY(boardHeight / 2 * info.posY);
        setAngle(info.angle);

        if (defautlTextSize <= 0) {
            int width = getLayoutParams().width;
            if (width > 0) {
                float widthTextRatio = textMakingInfo.getWidthTextRatio();

                defautlTextSize = Util.px2dp(getContext(), width) / widthTextRatio;
                setTextSize(defautlTextSize);
            }
        }
        defaultTextScale = (boardWidth * info.widthRatio) / getLayoutParams().width;
        setScale(1f);

        setStickerAlpha(info.alpha);

        id = info.sticker.id;
        attachId = info.attachId;
    }

    @Override
    public void setAttachId(long attachId) {
        this.attachId = attachId;
    }

    @Override
    public long getAttachId() {
        return attachId;
    }
}

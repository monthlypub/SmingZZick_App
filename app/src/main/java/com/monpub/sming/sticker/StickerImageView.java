package com.monpub.sming.sticker;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.monpub.sming.R;
import com.monpub.sming.etc.Util;

/**
 * Created by small-lab on 2016-09-03.
 */
public class StickerImageView extends ImageView implements Stickerable<ImageSticker> {
    private String id;
    private long attacId;

    public StickerImageView(Context context) {
        super(context);
        init();
    }

    public StickerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StickerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public StickerImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setScaleType(ScaleType.FIT_XY);
        setBackgroundResource(R.drawable.selector_sticker_apply_bg);

        attacId = System.currentTimeMillis();
    }

    @Override
    public float getRelativeWidthRatio(int smingWidth) {
        float width = ((BitmapDrawable) getDrawable()).getBitmap().getWidth();
        return width / smingWidth;
    }

    @Override
    public float getRelativeHeightRatio(int smingHeight) {
        float height = ((BitmapDrawable) getDrawable()).getBitmap().getHeight();
        return height / smingHeight;
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
    }

    @Override
    public void setData(ImageSticker sticker) {
        setImageURI(Uri.fromFile(sticker.imageFile));
        id = sticker.id;
    }

    @Override
    public void setAngle(float angle) {
        setRotation(angle);
    }

    @Override
    public void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    @Override
    public void setStickerAlpha(float alpha) {
        setImageAlpha((int)(alpha * 255));
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
        return getScaleX();
    }

    @Override
    public float getStickerAlpha() {
        return (float) getImageAlpha() / 255;
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
        float widthRatio = (getLayoutParams().width * getScale()) / boardWidth;
        float alpha = getStickerAlpha();

        return new ImageStickerAttachInfo(attacId, id, index, posX, posY, angle, widthRatio, alpha);
    }

    @Override
    public void applyAttachInfo(StickerAttachInfo info, View board) {
        float boardWidth = board.getWidth();
        float boardHeight = board.getHeight();

        setPosX(boardWidth / 2 * info.posX);
        setPosY(boardHeight / 2 * info.posY);
        setAngle(info.angle);
        setScale((boardWidth * info.widthRatio) / getLayoutParams().width);
        setStickerAlpha(info.alpha);

        id = info.sticker.id;
        attacId = info.attachId;
    }

    @Override
    public void setAttachId(long attachId) {
        this.attacId = attachId;
    }

    @Override
    public long getAttachId() {
        return attacId;
    }
}

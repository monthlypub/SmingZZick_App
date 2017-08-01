package com.monpub.sming.sticker;

import android.view.View;

/**
 * Created by small-lab on 2016-09-03.
 */
public interface Stickerable<T extends Sticker> {

    public float getRelativeWidthRatio(int smingWidth);

    public float getRelativeHeightRatio(int smingHeight);

    public void setData(T sticker);

    public abstract void setStickerId(String id);

    public abstract void setAngle(float angle);

    public abstract void setPosX(float x);

    public abstract void setPosY(float y);

    public abstract void setScale(float scale);

    public abstract void setStickerAlpha(float alpha);

    public abstract String getStickerId();

    public abstract float getAngle();

    public abstract float getPosX();

    public abstract float getPosY();

    public abstract float getScale();

    public abstract float getStickerAlpha();

    public abstract boolean isStickerSelected();

    public abstract void setStickerSelected(boolean select);

    public StickerAttachInfo getAttachInfo(View board, int index);

    public void applyAttachInfo(StickerAttachInfo info, View board);

    public void setAttachId(long attachId);

    public long getAttachId();
}
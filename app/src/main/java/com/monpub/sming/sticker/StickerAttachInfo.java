package com.monpub.sming.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by small-lab on 2016-09-04.
 */
public abstract class StickerAttachInfo {
    public final Sticker sticker;
    public final long attachId;
    public final int index;
    public final float posX;
    public final float posY;
    public final float angle;
    public final float widthRatio;
    public final float alpha;

    public StickerAttachInfo(long attachId, Sticker sticker, int index, float posX, float posY, float angle, float widthRatio, float alpha) {
        this.attachId = attachId;

        this.sticker = sticker;
        this.index = index;

        this.posX = posX;
        this.posY = posY;
        this.angle = angle;
        this.widthRatio = widthRatio;
        this.alpha = alpha;
    }

    public StickerAttachInfo(long attachId, String stickerId, int index, float posX, float posY, float angle, float widthRatio, float alpha) {
        this(
                attachId,
                StickerManager.getInstance().getSticker(stickerId),
                index,
                posX,
                posY,
                angle,
                widthRatio,
                alpha
        );
    }

    public abstract void draw(Context context, Canvas canvas);

    private static long dummyAttachId = 100;
    public static StickerAttachInfo fromPrefString(String prefString) {
        long attachId = -1;
        Matcher matcher = Pattern.compile(PREF_STRING_REGEX).matcher(prefString);
        int groundIndex = 1;
        if (matcher.find() == true) {
            attachId = Long.valueOf(matcher.group(groundIndex++));
        } else {
            matcher = Pattern.compile(PREF_STRING_REGEX_OLD).matcher(prefString);
            if (matcher.find() == true) {
                attachId = dummyAttachId++;
            }
        }

        StickerAttachInfo attachInfo = null;
        if (attachId > 0) {
            try {
                int index = Integer.valueOf(matcher.group(groundIndex++));
                float posX = Float.valueOf(matcher.group(groundIndex++));
                float posY = Float.valueOf(matcher.group(groundIndex++));
                float angle = Float.valueOf(matcher.group(groundIndex++));
                float widthRatio = Float.valueOf(matcher.group(groundIndex++));
                float alpha = Float.valueOf(matcher.group(groundIndex++));
                String id = matcher.group(groundIndex++);

                Sticker sticker = StickerManager.getInstance().getSticker(id);
                if (sticker == null) {
                    return null;
                }
                if (sticker instanceof ImageSticker) {
                    attachInfo = new ImageStickerAttachInfo(attachId, (ImageSticker) sticker, index, posX, posY, angle, widthRatio, alpha);
                } else if (sticker instanceof TextSticker) {
                    attachInfo = new TextStickerAttachInfo(attachId, (TextSticker) sticker, index, posX, posY, angle, widthRatio, alpha);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return attachInfo;
    }

    public String toString() {
        return String.format(PREF_STRING_FORMAT, attachId, index, posX, posY, angle, widthRatio, alpha, sticker.id);
    }

    private static final String PREF_STRING_FORMAT = "%d=[%d]X:%f|Y:%f|AN:%f|S:%f|AL:%f|ID:%S";

    private static final String PREF_STRING_REGEX_OLD = "\\[(\\d+)\\]X:([-+]?[0-9]*\\.?[0-9]*)\\|Y:([-+]?[0-9]*\\.?[0-9]*)\\|AN:([-+]?[0-9]*\\.?[0-9]*)\\|S:([-+]?[0-9]*\\.?[0-9]*)\\|AL:([-+]?[0-9]*\\.?[0-9]*)\\|ID:(.+)";
    private static final String PREF_STRING_REGEX = "(\\d+)=\\[(\\d+)\\]X:([-+]?[0-9]*\\.?[0-9]*)\\|Y:([-+]?[0-9]*\\.?[0-9]*)\\|AN:([-+]?[0-9]*\\.?[0-9]*)\\|S:([-+]?[0-9]*\\.?[0-9]*)\\|AL:([-+]?[0-9]*\\.?[0-9]*)\\|ID:(.+)";
}

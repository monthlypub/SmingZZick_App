package com.monpub.sming.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.monpub.sming.Constant;
import com.monpub.sming.SmingApplication;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by small-lab on 2016-09-04.
 */
public class StickerManager {
    private static final String PREF_KEY_DEFAULT_STICKER_ATTACHS = "prefKeyDefaultStickerAttaches";
    private static StickerManager ourInstance = null;

    public static StickerManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new StickerManager();
            ourInstance.reload();
            ourInstance.loadAttachInfo();
        }
        return ourInstance;
    }

    private List<Sticker> stickers;
    private List<StickerAttachInfo> defaultAttachInfos;

    private StickerManager() {
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public Sticker getSticker(String id) {
        for (Sticker sticker : stickers) {
            if (sticker.id.equalsIgnoreCase(id) == true) {
                return sticker;
            }
        }

        return null;
    }

    public void reload() {
        File stickerDirectory = Constant.getStickerDirectory();

        if (stickerDirectory.exists() == true) {
            final List<Sticker> stickers = new ArrayList<>();

            File[] imageFiles = stickerDirectory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    String lowerFileName = filename.toLowerCase();
                    boolean isImageSticker = lowerFileName.endsWith(".png") || lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg");
                    return  isImageSticker;
                }
            });
            if (imageFiles != null && imageFiles.length > 0) {
                for (File imageFIle : imageFiles) {
                    stickers.add(new ImageSticker(imageFIle));
                }
            }

            File[] textStickerFiles = stickerDirectory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    String lowerFileName = filename.toLowerCase();
                    boolean isTextSticker = lowerFileName.endsWith(".json");
                    return  isTextSticker;
                }
            });

            if (textStickerFiles != null && textStickerFiles.length > 0) {
                for (File textStickerFile : textStickerFiles) {
                    stickers.add(new TextSticker(textStickerFile));
                }
            }

            Collections.sort(stickers, new Comparator<Sticker>() {
                @Override
                public int compare(Sticker lhs, Sticker rhs) {
                    return Long.valueOf(rhs.getFile().lastModified()).compareTo(Long.valueOf(lhs.getFile().lastModified()));

                }
            });

            this.stickers = stickers;
        }
    }

    public void loadAttachInfo() {
        Set<String> prefValues = SmingApplication.getPreference().getStringSet(PREF_KEY_DEFAULT_STICKER_ATTACHS);
        if (prefValues == null || prefValues.isEmpty() == true) {
            return;
        }

        List<StickerAttachInfo> list = new ArrayList<>();
        for (String prefValue : prefValues) {
            StickerAttachInfo attachInfo = StickerAttachInfo.fromPrefString(prefValue);
            if (attachInfo != null) {
                list.add(attachInfo);
            }
        }

        if (list.isEmpty() == true) {
            return;
        }

        Collections.sort(list, new Comparator<StickerAttachInfo>() {
            @Override
            public int compare(StickerAttachInfo lhs, StickerAttachInfo rhs) {
                return lhs.index - rhs.index;
            }
        });

        this.defaultAttachInfos = list;
    }

    public void saveAttachInfo(List<StickerAttachInfo> attachInfos) {
        if (this.defaultAttachInfos == null) {
            this.defaultAttachInfos = new ArrayList<>();
        } else {
            this.defaultAttachInfos.clear();
        }

        this.defaultAttachInfos.addAll(attachInfos);
        Set<String> prefValue = new HashSet<>();
        for (StickerAttachInfo attachInfo : attachInfos) {
            prefValue.add(attachInfo.toString());
        }
        SmingApplication.getPreference().put(PREF_KEY_DEFAULT_STICKER_ATTACHS, prefValue);
    }

    public List<StickerAttachInfo> getDefaultAttachInfos() {
        return defaultAttachInfos;
    }

    public void drawStickers(Context context, Bitmap bitmap) {
        drawStickers(context, new Canvas(bitmap), defaultAttachInfos);
    }


    public void drawStickers(Context context, Canvas canvas) {
        drawStickers(context, canvas, defaultAttachInfos);
    }

    public void drawStickers(Context context, Canvas canvas, List<StickerAttachInfo> attachInfos) {
        if (attachInfos == null || attachInfos.isEmpty() == true) {
            return;
        }

        for (StickerAttachInfo attachInfo : attachInfos) {
            attachInfo.draw(context, canvas);
        }
    }

    public boolean hasDefaultSticker() {
        return defaultAttachInfos != null && defaultAttachInfos.isEmpty() == false;
    }
}

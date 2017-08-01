package com.monpub.sming.sticker;

import android.content.Context;
import android.text.TextUtils;
import android.util.JsonReader;

import com.monpub.textmaker.TextMakingInfo;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by small-lab on 2016-09-03.
 */
public final class TextSticker extends Sticker {
    public final File textFile;
    private TextMakingInfo textMakeInfo;
    private boolean parsed = false;

    public TextSticker(File textFile) {
        super(textFile.getName().toLowerCase());
        this.textFile = textFile;
    }

    public TextMakingInfo getTextMakeInfo(Context context) {
        if (parsed == true) {
            return textMakeInfo;
        }

        parsed = true;

        String json = null;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(textFile);
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        return textMakeInfo = TextMakingInfo.fromJSONString(context, json);
    }

    @Override
    public File getFile() {
        return textFile;
    }
}

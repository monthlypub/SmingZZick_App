package com.monpub.sming.sticker;

import android.view.View;

import java.io.File;

/**
 * Created by small-lab on 2016-09-03.
 */
public abstract class Sticker {
    public final String id;

    public Sticker(String id) {
        this.id = id;
    }

    public abstract File getFile();
}

package com.monpub.sming.sticker;

import java.io.File;

/**
 * Created by small-lab on 2016-09-03.
 */
public final class ImageSticker extends Sticker {
    public final File imageFile;

    public ImageSticker(File imageFile) {
        super(imageFile.getName().toLowerCase());
        this.imageFile = imageFile;
    }

    @Override
    public File getFile() {
        return imageFile;
    }
}

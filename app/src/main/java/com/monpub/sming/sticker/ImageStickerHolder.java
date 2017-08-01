package com.monpub.sming.sticker;

import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.monpub.sming.R;

/**
 * Created by small-lab on 2016-09-03.
 */
public  class ImageStickerHolder extends StickerHolder<ImageSticker> {
    public ImageView imageView;
    public ImageStickerHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.image);
    }

    @Override
    public void setValue(ImageSticker sticker) {
        imageView.setImageURI(Uri.fromFile(sticker.imageFile));
    }
}

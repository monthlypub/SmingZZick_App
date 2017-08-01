package com.monpub.sming.sticker;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by small-lab on 2016-09-03.
 */
public abstract class StickerHolder<T extends Sticker> extends RecyclerView.ViewHolder {
    public StickerHolder(View itemView) {
        super(itemView);
    }

    public abstract void setValue(T sticker);
}

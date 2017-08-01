package com.monpub.sming.sticker;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.monpub.sming.R;

/**
 * Created by small-lab on 2016-09-03.
 */
public abstract class StickerApplyViewHolder extends RecyclerView.ViewHolder {
    public final View selected;

    public StickerApplyViewHolder(View itemView) {
        super(itemView);
        selected = itemView.findViewById(R.id.selected);
    }
}

package com.monpub.sming.sming;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.monpub.sming.R;

/**
 * Created by small-lab on 2016-08-10.
 */
public class SmingViewHolder extends RecyclerView.ViewHolder {
    public final TextView date;
    public final TextView title;
    public final ImageView image;
    public final View delete;
    public final View sticker;

    public SmingViewHolder(View itemView) {
        super(itemView);

        date = (TextView) itemView.findViewById(R.id.date);
        title = (TextView) itemView.findViewById(R.id.title);
        image = (ImageView) itemView.findViewById(R.id.image);
        delete = itemView.findViewById(R.id.delete);
        sticker = itemView.findViewById(R.id.sticker);
    }
}

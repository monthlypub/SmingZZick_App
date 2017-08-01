package com.monpub.sming.attack;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.monpub.sming.R;

/**
 * Created by small-lab on 2016-08-10.
 */
public class AttackHolder extends RecyclerView.ViewHolder {
    public final TextView date;
    public final TextView title;
    public final TextView name;
    public final TextView sming;
    public final View delete;

    public AttackHolder(View itemView) {
        super(itemView);

        date = (TextView) itemView.findViewById(R.id.date);
        name = (TextView) itemView.findViewById(R.id.name);
        title = (TextView) itemView.findViewById(R.id.title);
        sming = (TextView) itemView.findViewById(R.id.smingTarget);
        delete = itemView.findViewById(R.id.delete);
    }
}

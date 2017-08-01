package com.monpub.sming.attack;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monpub.sming.R;
import com.monpub.sming.SmingApplication;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by small-lab on 2016-08-10.
 */
public class AttackAdaptor extends RecyclerView.Adapter<AttackHolder> {
    private List<AttackData> mAttacks;
    private OnItemClickListener mOnItemClickListener;
    private OnItemDeleteClickListener mOnItemDeleteClickListener;

    private String youtubeListId;

    public void setAttacks(List<AttackData> attackDatas) {
        if (attackDatas == null) {
            mAttacks = null;
            return;
        }
        mAttacks = new ArrayList<>(attackDatas);
    }

    public void setYoutubeListId(String youtubeListId) {
        this.youtubeListId = youtubeListId;
    }

    public String getYoutubeListId() {
        if (mAttacks == null || mAttacks.isEmpty() == true) {
            return null;
        }

        return youtubeListId;
    }

    public void remove(int position) {
        mAttacks.remove(position);
        if (mAttacks.isEmpty() == true) {
            this.youtubeListId = null;
        }
    }

    @Override
    public int getItemCount() {
        return mAttacks == null ? 0 : mAttacks.size();
    }

    public List<AttackData> getAttacks() {
        return mAttacks;
    }

    @Override
    public AttackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history_attack, parent, false);
        AttackHolder attackHolder = new AttackHolder(itemView);
        itemView.setOnClickListener(mOnClickListener);
        attackHolder.delete.setOnClickListener(mOnClickListener);
        attackHolder.delete.setOnLongClickListener(mOnLongClickListener);

        CardView cardView = (CardView) attackHolder.itemView.findViewById(R.id.card);
        cardView.setPreventCornerOverlap(false);

        return attackHolder;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemDeleteClickListener(OnItemDeleteClickListener onItemDeleteClickListener) {
        mOnItemDeleteClickListener = onItemDeleteClickListener;
    }

    @Override
    public void onBindViewHolder(AttackHolder holder, int position) {
        AttackData attackData = mAttacks.get(position);

        holder.date.setText(new SimpleDateFormat("yy/MM/dd HH:mm").format(attackData.time));
        holder.name.setText(attackData.getTargetName());

        if (TextUtils.isEmpty(attackData.title) == false) {
            holder.title.setText(attackData.title);
        } else {
            holder.title.setText("제목 미정");

        }

        if (TextUtils.isEmpty(attackData.smingUrl) == false) {
            holder.sming.setText(
                    Html.fromHtml(
                            "<a href=\"" + attackData.smingUrl + "\">" + attackData.smingTarget +  "</a> "));
            holder.sming.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            holder.sming.setText(attackData.smingTarget);
        }



        holder.itemView.setTag(attackData);
        holder.delete.setTag(attackData);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.delete) {
                int position = -1;
                AttackData attackData = (AttackData) v.getTag();
                position = mAttacks.indexOf(attackData);

                if (position != -1) {
                    mOnItemDeleteClickListener.onItemDeleteClick(attackData, position);
                }
            } else {
                mOnItemClickListener.onItemClick((AttackData) v.getTag());
            }
        }
    };

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (R.id.delete == v.getId()) {
                mOnItemDeleteClickListener.onAllItemDeleteClick();
                return true;
            }

            return false;
        }
    };

    public interface OnItemClickListener {
        public void onItemClick(AttackData attackData);
    }

    public interface OnItemDeleteClickListener {
        public void onItemDeleteClick(AttackData attackData, int position);
        public void onAllItemDeleteClick();
    }

}

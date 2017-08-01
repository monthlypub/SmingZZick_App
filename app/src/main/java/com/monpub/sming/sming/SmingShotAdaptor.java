package com.monpub.sming.sming;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monpub.sming.R;
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
public class SmingShotAdaptor extends RecyclerView.Adapter<SmingViewHolder> {
    private List<File> mSmings;
    private OnItemClickListener mOnItemClickListener;
    private OnItemDeleteClickListener mOnItemDeleteClickListener;
    private OnItemDeleteAllClickListener mOnItemDeleteAllClickListener;
    private OnItemStickerClickListener mOnItemStickerClickListener;

    public void setFiles(File[] files) {
        mSmings = new ArrayList<File>(Arrays.asList(files));
    }

    public List<File> getFIles() {
        return mSmings;
    }

    public void remove(int position) {
        mSmings.remove(position);
    }

    @Override
    public int getItemCount() {
        return mSmings == null ? 0 : mSmings.size();
    }

    @Override
    public SmingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history_sming, parent, false);
        SmingViewHolder smingViewHolder = new SmingViewHolder(itemView);
        itemView.setOnClickListener(mOnClickListener);
        smingViewHolder.delete.setOnClickListener(mOnClickListener);
        smingViewHolder.delete.setOnLongClickListener(mOnLongClickDeleteListener);
        smingViewHolder.sticker.setOnClickListener(mOnClickListener);
        return smingViewHolder;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemDeleteClickListener(OnItemDeleteClickListener onItemDeleteClickListener) {
        mOnItemDeleteClickListener = onItemDeleteClickListener;
    }

    public void setOnItemDeleteAllClickListener(OnItemDeleteAllClickListener onItemDeleteAllClickListener) {
        mOnItemDeleteAllClickListener = onItemDeleteAllClickListener;
    }

    public void setOnItemStickerClickListener(OnItemStickerClickListener onItemStickerClickListener) {
        mOnItemStickerClickListener = onItemStickerClickListener;
    }

    @Override
    public void onBindViewHolder(SmingViewHolder holder, int position) {
        File file = mSmings.get(position);

        long lastModified = file.lastModified();
        String name = file.getName();
        ;
        holder.date.setText(new SimpleDateFormat("yy/MM/dd HH:mm").format(new Date(lastModified)));
        holder.title.setText(name);

        ImageLoader imageLoader = ImageLoader.getInstance();
        String path = file.getAbsolutePath();

//        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        holder.image.setImageDrawable(null);
        imageLoader.displayImage("file://" + path, holder.image);

        holder.itemView.setTag(path);
        holder.delete.setTag(path);
        holder.sticker.setTag(path);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String filePath = (String) v.getTag();

            if (filePath != null) {
                if (v.getId() == R.id.delete) {
                    int position = -1;

                    for (int i = 0; i < mSmings.size(); i++) {
                        if (mSmings.get(i).getAbsolutePath().equals(filePath) == true) {
                            position = i;
                        }
                    }

                    if (position != -1) {
                        mOnItemDeleteClickListener.onItemDeleteClick(filePath, position);
                    }
                } else if (v.getId() == R.id.sticker) {
                    if (mOnItemStickerClickListener != null) {
                        mOnItemStickerClickListener.onItemStickerClick(filePath);
                    }
                } else {
                    mOnItemClickListener.onItemClick(filePath);
                }
            }
        }
    };

    private View.OnLongClickListener mOnLongClickDeleteListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mOnItemDeleteAllClickListener != null) {
                mOnItemDeleteAllClickListener.onItemDeleteAllClick();
            }
            return true;
        }
    };

    public interface OnItemClickListener {
        public void onItemClick(String imageFilePath);
    }

    public interface OnItemDeleteClickListener {
        public void onItemDeleteClick(String imageFilePath, int position);
    }

    public interface OnItemDeleteAllClickListener {
        public void onItemDeleteAllClick();
    }

    public interface OnItemStickerClickListener {
        public void onItemStickerClick(String imageFilePath);
    }

}

package com.monpub.sming.sticker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.monpub.sming.R;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by small-lab on 2016-09-03.
 */
public class StickerAdapter extends RecyclerView.Adapter<StickerHolder> {
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_TEXT = 2;
    private static final int TYPE_ADD = 3;
    private static final int TYPE_EMPTY_GUIDE = 4;

    private List<Sticker> list;
    private OnStickerClickListener onStickerClickListener;
    private OnStickerAddClickListener onStickerAddClickListener;

    public void refresh() {
        List<Sticker>list = StickerManager.getInstance().getStickers();
        if (this.list != null) {
            this.list.clear();
        }
        if (list == null) {
            return;
        }
        this.list = new ArrayList<>(list);
    }

    public void setOnStickerClickListener(OnStickerClickListener listener) {
        onStickerClickListener = listener;
    }

    public void setOnStickerAddClickListener(OnStickerAddClickListener listener) {
        onStickerAddClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return list == null || list.isEmpty() == true ? 2 : list.size() + 1;
    }

    public int getStickerCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public void onBindViewHolder(StickerHolder holder, int position) {
        int itemType = getItemViewType(position);

        switch (itemType) {
            case TYPE_ADD :
            case TYPE_EMPTY_GUIDE :
                // do nothing
                break;
            default:
                Sticker sticker = list.get(position - 1);
                holder.setValue(sticker);
                holder.itemView.setTag(sticker);
        }
    }

    @Override
    public StickerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        StickerHolder holder = null;
        if (viewType == TYPE_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_sticker, parent, false);
            holder = new ImageStickerHolder(view);
            view.setOnClickListener(onClickListener);
        } else if (viewType == TYPE_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_text_sticker, parent, false);
            holder = new TextStickerHolder(view);
            view.setOnClickListener(onClickListener);
        } else if (viewType == TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_image_sticker, parent, false);
            ImageStickerHolder imageStickerHolder = new ImageStickerHolder(view);
            imageStickerHolder.imageView.setImageResource(R.drawable.ic_add_box_white_48dp);
            imageStickerHolder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            view.setOnClickListener(onAddClickListener);
            holder = imageStickerHolder;
        } else if (viewType == TYPE_EMPTY_GUIDE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_empty_sticker_guide, parent, false);
            ImageStickerHolder imageStickerHolder = new ImageStickerHolder(view);
            holder = imageStickerHolder;
        }
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        int type;

        if (position == 0) {
            type = TYPE_ADD;
        } else if (list == null || list.isEmpty() == true && position == 1) {
            type = TYPE_EMPTY_GUIDE;
        } else if (list.get(position - 1) instanceof ImageSticker) {
            type = TYPE_IMAGE;
        } else if (list.get(position - 1)instanceof TextSticker) {
            type = TYPE_TEXT;
        } else {
            throw new IllegalArgumentException("unknown sticker type");
        }

        return type;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onStickerClickListener != null) {
                onStickerClickListener.onStickerClick((Sticker) v.getTag());
            }
        }
    };

    private View.OnClickListener onAddClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onStickerAddClickListener != null) {
                onStickerAddClickListener.onStickerAddClick();
            }
        }
    };


    public static interface OnStickerClickListener {
        public void onStickerClick(Sticker sticker);
    }

    public static interface OnStickerAddClickListener {
        public void onStickerAddClick();
    }

}

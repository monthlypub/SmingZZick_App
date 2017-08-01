package com.monpub.sming.sticker;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.monpub.sming.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by small-lab on 2016-09-03.
 */
public class StickerApplyAdapter extends RecyclerView.Adapter<StickerApplyViewHolder> {
    private final static int TYPE_IMAGE = 1;
    private final static int TYPE_TEXT = 2;

    private List<Stickerable> stickers;
    private OnStickerApplyEventListener onStickerApplyEventListener;

    private View dragGuideView;
    private View dragDeleteView;
    private TextView dragMsgTextView;

    public void setStickers(List<Stickerable> stickers) {
        if (this.stickers != null) {
            this.stickers.clear();
        }

        this.stickers = new ArrayList<>(stickers);
    }

    public void setDrag(View dragGuideView, View dragDeleteView, TextView dragMsgTextView) {
        this.dragGuideView = dragGuideView;
        this.dragDeleteView = dragDeleteView;
        this.dragMsgTextView = dragMsgTextView;
    }

    public void setOnStickerApplyEventListener(OnStickerApplyEventListener listener) {
        onStickerApplyEventListener = listener;
    }

    public void add(Stickerable stickerable) {
        if (stickers == null) {
            stickers = new ArrayList<>();
        }
        stickers.add(stickerable);
    }

    public void refresh() {
        if (stickers == null || stickers.isEmpty() == true) {
            return;
        }

        List<String> removedStickerIds = new ArrayList<>();

        for (Stickerable stickerable : stickers) {
            if (StickerManager.getInstance().getSticker(stickerable.getStickerId()) == null) {
                removedStickerIds.add(stickerable.getStickerId());
            }
        }

        for (String stickerId : removedStickerIds) {
            deleteStickerByStickerId(stickerId);
        }
    }

    @Override
    public void onBindViewHolder(StickerApplyViewHolder holder, int position) {
        holder.selected.setVisibility(stickers.get(position).isStickerSelected() ? View.VISIBLE : View.GONE);
        holder.itemView.setTag(position);
        holder.itemView.setAlpha(1f);
    }

    @Override
    public StickerApplyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        StickerApplyViewHolder stickerApplyViewHolder = null;
        View view = null;
        if (viewType == TYPE_IMAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_type_image, parent, false);
            stickerApplyViewHolder = new ImageStickerApplyViewHolder(view);
        } else if (viewType == TYPE_TEXT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_type_text, parent, false);
            stickerApplyViewHolder = new TextStickerApplyViewHolder(view);
        }
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);
        view.setOnDragListener(onDragListener);

        return stickerApplyViewHolder;
    }

    public int indexOf(Stickerable stickerable) {
        return stickers == null ? -1 : stickers.indexOf(stickerable);
    }

    @Override
    public int getItemViewType(int position) {
        Stickerable stickerable = stickers.get(position);
        int type;
        if (stickerable instanceof StickerImageView) {
            type = TYPE_IMAGE;
        } else if (stickerable instanceof StickerTextView) {
            type = TYPE_TEXT;
        } else {
            throw new IllegalStateException("unknown type" );
        }

        return type;
    }

    @Override
    public int getItemCount() {
        int size = stickers == null ? 0 : stickers.size();
        return size;
    }

    private void performClick(int position) {
        Stickerable clicked = stickers.get(position);

        if (clicked != null) {
            for (Stickerable stickerable : stickers) {
                stickerable.setStickerSelected(stickerable == clicked);
            }
            notifyDataSetChanged();
            onStickerApplyEventListener.onStickerApplyClick(position, clicked);
        }
    }

    public View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (onStickerApplyEventListener != null) {
                int position = (Integer) v.getTag();
                performClick(position);
            }
        }
    };

    public static interface OnStickerApplyEventListener {
        public void onStickerApplyClick(int position, Stickerable stickerable);
        public void onStickerRemove(Stickerable stickerable);
    }

    public static interface OnStickerApplyDeleteListener {
        public void onStickerApplyDelete(int position, Stickerable stickerable);
    }

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            ClipData.Item item = new ClipData.Item("" + v.getTag());
            ClipData dragData = new ClipData("" + v.getTag(), new String[] {ClipDescription.MIMETYPE_TEXT_PLAIN},item);

            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(v);

            showDragBuide();
            v.startDrag(dragData,  // the data to be dragged
                    myShadow,  // the drag shadow builder
                    null,      // no need to use local data
                    0          // flags (not currently used, set to 0)
            );
            v.setAlpha(0.2f);
            return true;
        }
    };

    public void deleteSticker(long attachId) {
        if (stickers == null) {
            return;
        }

        Stickerable stickerableToDelete = null;
        for (Stickerable stickerable : stickers) {
            if (attachId == stickerable.getAttachId()) {
                stickerableToDelete = stickerable;
                break;
            }
        }

        if (stickerableToDelete == null) {
            return;
        }

        stickers.remove(stickerableToDelete);
        ViewGroup parent = (ViewGroup) ((View) stickerableToDelete).getParent();
        parent.removeView((View) stickerableToDelete);

        notifyDataSetChanged();
        if (onStickerApplyEventListener != null) {
            onStickerApplyEventListener.onStickerRemove(stickerableToDelete);
        }
    }

    public void deleteStickerByStickerId(String stickerId) {
        if (stickers == null) {
            return;
        }

        Stickerable stickerableToDelete = null;
        for (Stickerable stickerable : stickers) {
            if (stickerId != null && stickerId.equals(stickerable.getStickerId()) == true) {
                stickerableToDelete = stickerable;
                break;
            }
        }

        if (stickerableToDelete == null) {
            return;
        }

        stickers.remove(stickerableToDelete);
        ViewGroup parent = (ViewGroup) ((View) stickerableToDelete).getParent();
        parent.removeView((View) stickerableToDelete);

        notifyDataSetChanged();
        if (onStickerApplyEventListener != null) {
            onStickerApplyEventListener.onStickerRemove(stickerableToDelete);
        }
    }

    public void deleteSticker(String id) {
        if (stickers == null) {
            return;
        }

        Stickerable stickerableToDelete = null;
        for (Stickerable stickerable : stickers) {
            if (id != null && id.equals(stickerable.getStickerId()) == true) {
                stickerableToDelete = stickerable;
                break;
            }
        }

        if (stickerableToDelete == null) {
            return;
        }

        stickers.remove(stickerableToDelete);
        ViewGroup parent = (ViewGroup) ((View) stickerableToDelete).getParent();
        parent.removeView((View) stickerableToDelete);

        notifyDataSetChanged();
        if (onStickerApplyEventListener != null) {
            onStickerApplyEventListener.onStickerRemove(stickerableToDelete);
        }
    }


    private View.OnDragListener onDragListener = new View.OnDragListener() {
        private int from = -1;
        private int to = -1;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();

            if (action == DragEvent.ACTION_DRAG_STARTED) {
                if (from == -1) {
                    from = Integer.valueOf(event.getClipDescription().getLabel().toString());
                }
            } else if (action == DragEvent.ACTION_DRAG_ENTERED) {
                if (v != dragDeleteView) {
                    to = (Integer) v.getTag();
                }

                updateDragGuide(from, to, v == dragDeleteView);
            } else  if (action == DragEvent.ACTION_DRAG_EXITED) {
                to = -1;

                updateDragGuide(from, to, false);
                // TODO update message
            } else if (action == DragEvent.ACTION_DRAG_ENDED) {
                v.setAlpha(1f);

                if (from != -1) {
                    // TODO do drag job
                    dismissDragGuide();
                    from = -1;
                    to = -1;
                }
            } else if (action == DragEvent.ACTION_DROP) {
                v.setAlpha(1f);
                if (v == dragDeleteView) {
                    Stickerable stickerable = stickers.get(from);
                    deleteSticker(stickerable.getAttachId());
                } else if (from != to && to > -1) {
                    View stickerView = (View) stickers.remove(from);
                    ViewGroup.LayoutParams layoutParams = stickerView.getLayoutParams();
                    ViewGroup parent = (ViewGroup) stickerView.getParent();
                    parent.removeView((View) stickerView);

                    stickers.add(to, (Stickerable) stickerView);
                    parent.addView(stickerView, to, layoutParams);

                    notifyDataSetChanged();
                }
            }

            return true;
        }
    };

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {
        private int width;
        private int height;
        public MyDragShadowBuilder(View v) {
            super(v);

            width = v.getMeasuredWidth();
            height = v.getMeasuredHeight();
        }

        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            size.set((int) (width * 1.3), (int) (height * 1.3));
            touch.set((int) (width * 1.3 / 2), height);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            Matrix matrix = new Matrix();
            matrix.reset();
            matrix.setScale(1.3f, 1.3f);
            canvas.setMatrix(matrix);
            super.onDrawShadow(canvas);
        }
    }

    private void showDragBuide() {
        dragGuideView.setVisibility(View.VISIBLE);
        dragDeleteView.setAlpha(0.8f);
        dragMsgTextView.setText("");

        dragDeleteView.setOnDragListener(onDragListener);
    }

    private void dismissDragGuide() {
        dragGuideView.setVisibility(View.GONE);
    }

    private void updateDragGuide(int from, int to, boolean isDelete) {
        String msg = "";

        if (isDelete == true) {
            msg = "스티커 땜";
            dragDeleteView.setAlpha(1f);
        } else {
            dragDeleteView.setAlpha(0.8f);

            if (to > -1 && from != to) {
                from++;
                to++;
                msg = from + "번째 스티커를\n" + to + "번째로 이동";
            }
        }
        dragMsgTextView.setText(msg);
    }

    public void hit(int x, int y) {
        View view;
        for (int i = stickers.size() - 1; i >= 0; i--) {
            view = (View) stickers.get(i);

            Rect rect = new Rect();
            view.getHitRect(rect);

            if (rect.contains(x, y)) {
                performClick(i);
                break;
            }
        }
    }

}

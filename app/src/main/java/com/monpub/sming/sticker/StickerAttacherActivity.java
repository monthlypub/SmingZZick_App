package com.monpub.sming.sticker;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.monpub.sming.Constant;
import com.monpub.sming.R;
import com.monpub.sming.etc.Util;
import com.monpub.sming.sming.SmingManager;
import com.monpub.textmaker.TextMakingInfo;

import org.metalev.multitouch.controller.MultiTouchController;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPicker;

/**
 * Created by small-lab on 2016-09-02.
 */
public class StickerAttacherActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PICKER = 201;

    public static final String EXTRA_SMING_PATH = "extra_sming_path";

    private RelativeLayout mBoard;

    private float smingWidth;
    private float smingHeight;

    private StickerAdapter stickerAdapter;
    private RecyclerView stickerRecyclerView;
    private LinearLayoutManager stickerLayoutManager;

    private StickerApplyAdapter stickerApplyAdapter;
    private RecyclerView stickerApplyRecyclerView;
    private LinearLayoutManager stickerApplyLayoutManager;

    private Stickerable mCurrentStickeable;
    private SeekBar seekBarAlpha;

    private View layoutStickerAlpha;

    private View stickerChoiceGuide;
    private View stickerSelectGuide;

    private ViewGroup previewFrame;
    private View previewDelete;
    private View previewAttach;
    private View previewEdit;
    private View previewCancel;
    private View layoutPreview;

    private boolean isAppend;
    private String filePathToAppend;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (data == null) {
            return;
        }
        ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
        if (photos == null) {
            return;
        }

        for (String photo : photos) {
            File file = new File(photo);
            if (file != null && file.exists() == true && file.isFile() == true) {
                File destFile = new File(Constant.getStickerDirectory(), file.getName());

                try {
                    Util.copy(file, destFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attach_sticker);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_SMING_PATH) == true) {
            String path = intent.getStringExtra(EXTRA_SMING_PATH);

            if (TextUtils.isEmpty(path) == false) {
                File file = new File(path);

                if (file.isFile() && file.exists() == true) {
                    ImageView smingImageView = (ImageView) findViewById(R.id.dummy_view);
                    smingImageView.setImageURI(Uri.fromFile(file));

                    filePathToAppend = path;
                    isAppend = true;
                }
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call
        if (isAppend == true) {
            toolbar.setTitle("스티커 추가");
        }

        float width = Util.getWidth(this);
        float height = Util.getHeight(this);

//        float aspectRatio = height / width;
        float aspectRatio = width * 2 / height;
//        float aspectRatio = 2f;

        int smingWidth = SmingManager.getInstance().getSmingWdith();

        this.smingWidth = smingWidth;
        this.smingHeight = smingWidth / aspectRatio;

        View dummyView =  findViewById(R.id.dummy_view);

        if (Build.VERSION.SDK_INT <= 19) {
            Util.fixBackgroundRepeat(dummyView);
            Util.fixBackgroundRepeat(findViewById(R.id.bg));
        }

        final PercentRelativeLayout.LayoutParams layoutParams;

        layoutParams = (PercentRelativeLayout.LayoutParams) dummyView.getLayoutParams();
        layoutParams.getPercentLayoutInfo().aspectRatio = aspectRatio;
        dummyView.setLayoutParams(layoutParams);
        dummyView.requestLayout();

        mBoard = (RelativeLayout) findViewById(R.id.sticker_board);

        stickerRecyclerView = (RecyclerView) findViewById(R.id.recycler_sticker);
        stickerAdapter = new StickerAdapter();
        stickerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        stickerRecyclerView.setLayoutManager(stickerLayoutManager);
        stickerRecyclerView.setAdapter(stickerAdapter);
        stickerAdapter.setOnStickerClickListener(new StickerAdapter.OnStickerClickListener() {
            @Override
            public void onStickerClick(Sticker sticker) {
                applyPreview(sticker);
            }
        });
        stickerAdapter.setOnStickerAddClickListener(new StickerAdapter.OnStickerAddClickListener() {
            @Override
            public void onStickerAddClick() {
                AlertDialog.Builder builder = new AlertDialog.Builder(StickerAttacherActivity.this);
                builder.setSingleChoiceItems(new String[]{"이미지 스티커 추가", "텍스트 스티커 추가"}, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            PhotoPicker.builder()
                                    .setShowCamera(false)
                                    .setShowGif(false)
                                    .setPreviewEnabled(true)
                                    .setGridColumnCount(2)
                                    .start(StickerAttacherActivity.this, REQUEST_CODE_PICKER);
                        } else if (which == 1) {
                            Intent intent = new Intent(StickerAttacherActivity.this, TextMakeActivity.class);
                            startActivity(intent);
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });

        stickerApplyRecyclerView = (RecyclerView) findViewById(R.id.recycler_apply);
        stickerApplyAdapter = new StickerApplyAdapter();
        stickerApplyLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        stickerApplyRecyclerView.setLayoutManager(stickerApplyLayoutManager);
        stickerApplyRecyclerView.setAdapter(stickerApplyAdapter);
        stickerApplyAdapter.setOnStickerApplyEventListener(new StickerApplyAdapter.OnStickerApplyEventListener() {
            @Override
            public void onStickerApplyClick(int position, Stickerable stickerable) {
                mCurrentStickeable = stickerable;
                applyAlphaToSeekbar();
            }

            @Override
            public void onStickerRemove(Stickerable stickerable) {
                if (mCurrentStickeable == stickerable) {
                    mCurrentStickeable = null;
                }
                applyAlphaToSeekbar();
            }
        });

        seekBarAlpha = (SeekBar) findViewById(R.id.progress_alpha);
        seekBarAlpha.setMax(255);
        seekBarAlpha.setProgress(255);
        seekBarAlpha.setOnSeekBarChangeListener(onAlphaSeekBarChangeListener);

        mBoard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return multiTouchController.onTouchEvent(event);
            }
        });

        stickerApplyAdapter.setDrag(
                findViewById(R.id.layout_drag),
                findViewById(R.id.trash),
                (TextView) findViewById(R.id.msg_drag)
        );

        stickerChoiceGuide = findViewById(R.id.guide_sticker_choice);
        stickerSelectGuide = findViewById(R.id.guide_sticker_select);

        layoutStickerAlpha = findViewById(R.id.layout_alpha);

        layoutPreview = findViewById(R.id.layout_preview);
        previewFrame = (ViewGroup) layoutPreview.findViewById(R.id.preview_frame);
        previewDelete = layoutPreview.findViewById(R.id.delete);
        previewAttach = layoutPreview.findViewById(R.id.attach);
        previewEdit = layoutPreview.findViewById(R.id.edit);
        previewCancel = layoutPreview.findViewById(R.id.cancel);

        previewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sticker sticker = (Sticker) previewFrame.getTag();
                final File file = sticker.getFile();

                AlertDialog.Builder builder = new AlertDialog.Builder(StickerAttacherActivity.this);
                builder.setMessage("스티커 파일을 지울라고요?\n\n");
                builder.setPositiveButton("지우기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        file.delete();

                        refresh();
                        layoutPreview.setVisibility(View.GONE);
                    }
                });
                builder.setNegativeButton("냅두기", null);
                builder.show();
            }
        });

        previewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPreview.setVisibility(View.GONE);
            }
        });

        previewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPreview.setVisibility(View.GONE);
                Sticker sticker = (Sticker) previewFrame.getTag();
                if (sticker == null || sticker instanceof TextSticker == false) {
                    return;
                }

                TextSticker textSticker = (TextSticker) sticker;
                String filePath = textSticker.id;
                TextMakingInfo textMakingInfo = textSticker.getTextMakeInfo(StickerAttacherActivity.this);

                Intent intent = new Intent(StickerAttacherActivity.this, TextMakeActivity.class);
                intent.putExtra(TextMakeActivity.EXTRA_MAKING_INFO, textMakingInfo);
                intent.putExtra(TextMakeActivity.EXTRA_FILE_NAME, filePath);

                startActivity(intent);
            }
        });

        previewAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPreview.setVisibility(View.GONE);
                Sticker sticker = (Sticker) previewFrame.getTag();
                if (sticker == null) {
                    return;
                }

                if (mCurrentStickeable != null) {
                    mCurrentStickeable.setStickerSelected(false);
                }

                mCurrentStickeable = attachSticker(sticker);

                mCurrentStickeable.setStickerSelected(true);
                stickerApplyAdapter.add(mCurrentStickeable);
                stickerApplyAdapter.notifyDataSetChanged();

                applyAlphaToSeekbar();
            }
        });

        if (isAppend == false) {
            mBoard.post(new Runnable() {
                @Override
                public void run() {
                    if (mBoard.getWidth() == 0 || mBoard.getHeight() == 0) {
                        mBoard.post(this);
                        return;
                    }

                    List<StickerAttachInfo> attachInfos = StickerManager.getInstance().getDefaultAttachInfos();
                    if (attachInfos == null || attachInfos.isEmpty() == true) {
                        return;
                    }

                    List<StickerAttachInfo> existAttachInfos = new ArrayList<StickerAttachInfo>();
                    for (StickerAttachInfo info : attachInfos) {
                        if (info.sticker instanceof ImageSticker) {
                            ImageSticker imageSticker = (ImageSticker) info.sticker;
                            if (imageSticker.imageFile.exists() == false) {
                                stickerApplyAdapter.deleteSticker(imageSticker.id);
                                continue;
                            }
                        } else if (info.sticker instanceof TextSticker){
                            TextSticker textSticker = (TextSticker) info.sticker;
                            if (textSticker.textFile.exists() == false) {
                                stickerApplyAdapter.deleteSticker(textSticker.id);
                                continue;
                            }
                        }
                        existAttachInfos.add(info);
                    }
                    attachInfos = existAttachInfos;

                    for (StickerAttachInfo info : attachInfos) {
                        Stickerable stickerable = attachSticker(info.sticker);
                        stickerable.applyAttachInfo(info, mBoard);

                        stickerApplyAdapter.add(stickerable);
                    }
                    stickerApplyAdapter.notifyDataSetChanged();
                    applyAlphaToSeekbar();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
    }

    private void refresh() {
        StickerManager.getInstance().reload();
        stickerAdapter.refresh();

        if (stickerAdapter.getStickerCount() > 0) {
            stickerSelectGuide.setVisibility(View.GONE);
            stickerChoiceGuide.setVisibility(View.GONE);
        }
        stickerAdapter.notifyDataSetChanged();

        stickerApplyAdapter.refresh();
        stickerApplyAdapter.notifyDataSetChanged();

        applyAlphaToSeekbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attach_sticker_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_sticker) {
            int childCount = mBoard.getChildCount();
            if (childCount == 0) {
                if (isAppend == false) {
                    StickerManager.getInstance().saveAttachInfo(new ArrayList<StickerAttachInfo>());
                }
                finish();
                return true;
            }

            List<StickerAttachInfo> stickerAttachInfos = new ArrayList<>();
            for (int i = 0; i < childCount; i++) {
                Stickerable stickerable = (Stickerable) mBoard.getChildAt(i);
                StickerAttachInfo stickerAttachInfo = stickerable.getAttachInfo(mBoard, i);

                stickerAttachInfos.add(stickerAttachInfo);
            }

            if (isAppend == false) {
                StickerManager.getInstance().saveAttachInfo(stickerAttachInfos);
            } else {
                FileOutputStream fos = null;
                try {
                    Glide.get(getApplicationContext()).clearMemory();
                    new Thread() {
                        @Override
                        public void run() {
                            Glide.get(getApplicationContext()).clearDiskCache();

                        }
                    }.start();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inMutable = true;

                    Bitmap bitmap = BitmapFactory.decodeFile(filePathToAppend, options);
                    StickerManager.getInstance().drawStickers(StickerAttacherActivity.this, new Canvas(bitmap), stickerAttachInfos);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos = new FileOutputStream(new File(filePathToAppend)));

                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{filePathToAppend}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            // do nothing
                        }
                    });

                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public Stickerable attachSticker(Sticker sticker) {
        Stickerable stickerable = null;
        if (sticker instanceof ImageSticker) {
            StickerImageView stickerImageView = new StickerImageView(this);
            stickerImageView.setData((ImageSticker) sticker);

            stickerable = stickerImageView;
        } else if (sticker instanceof TextSticker) {
            StickerTextView stickerTextView = new StickerTextView(this);
            stickerTextView.setData((TextSticker) sticker);

            stickerable = stickerTextView;
        }

        if (sticker != null) {
            addToBoard(stickerable);
        }

        return stickerable;
    }

    public void addToBoard(Stickerable sticker) {
        RelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(StickerAttacherActivity.this, null);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        float boardWidth = mBoard.getMeasuredWidth();
        float boardHeight = mBoard.getMeasuredHeight();

        float widthRatio = sticker.getRelativeWidthRatio((int) smingWidth);
        float heightRatio = sticker.getRelativeHeightRatio((int) smingHeight);

        while (widthRatio > 0.6f || heightRatio > 0.6f) {
            widthRatio *= 0.8f;
            heightRatio *= 0.8f;
        }

        int stickerWidth = (int) (boardWidth * widthRatio);
        int stickerHeight = (int) (boardHeight * heightRatio);

        layoutParams.width = stickerWidth;
        layoutParams.height = stickerHeight;

        mBoard.addView((View) sticker, layoutParams);
    }

    private void applyAlphaToSeekbar() {
        if (mCurrentStickeable == null) {
            layoutStickerAlpha.setVisibility(View.GONE);
            if (stickerAdapter.getItemCount() == 0) {
                layoutStickerAlpha.setVisibility(View.GONE);
                stickerChoiceGuide.setVisibility(View.GONE);
                stickerSelectGuide.setVisibility(View.GONE);
            } else {
                if (stickerApplyAdapter.getItemCount() == 0) {
                    stickerChoiceGuide.setVisibility(View.GONE);
                    stickerSelectGuide.setVisibility(View.VISIBLE);
                } else {
                    stickerChoiceGuide.setVisibility(View.VISIBLE);
                    stickerSelectGuide.setVisibility(View.GONE);
                }
            }
        } else {
            layoutStickerAlpha.setVisibility(View.VISIBLE);
            stickerChoiceGuide.setVisibility(View.GONE);
            stickerSelectGuide.setVisibility(View.GONE);

            seekBarAlpha.setProgress((int) (mCurrentStickeable.getStickerAlpha() * seekBarAlpha.getMax()));
        }
    }

    private SeekBar.OnSeekBarChangeListener onAlphaSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mCurrentStickeable != null && fromUser == true) {
                mCurrentStickeable.setStickerAlpha((float) progress / seekBar.getMax());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void applyPreview(Sticker sticker) {
        layoutPreview.setVisibility(View.VISIBLE);
        if (previewFrame.getChildCount() > 1) {
            previewFrame.removeViewAt(0);
        }
        if (sticker instanceof ImageSticker) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            Glide.with(StickerAttacherActivity.this)
                    .load(Uri.fromFile(((ImageSticker) sticker).imageFile))
                    .into(imageView);

            previewFrame.addView(imageView, 0, new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            previewEdit.setVisibility(View.GONE);
        } else if (sticker instanceof  TextSticker && ((TextSticker) sticker).getTextMakeInfo(StickerAttacherActivity.this) != null) {
            TextStickerStaticView textStickerStaticView = new TextStickerStaticView(this);
            textStickerStaticView.setTextMakingInfo(((TextSticker) sticker).getTextMakeInfo(StickerAttacherActivity.this));

            previewFrame.addView(textStickerStaticView, 0, new RelativeLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            previewEdit.setVisibility(View.VISIBLE);
        }
        previewFrame.setTag(sticker);
    }

    private MultiTouchController<Object> multiTouchController = new MultiTouchController<Object>(new MultiTouchController.MultiTouchObjectCanvas<Object>() {
            private static final float BOUND_XY = 20f;
            private static final float BOUND_ANGLE = 15f;

            private float startX;
            private float startY;
            private float startAngle;
            private float startDiameter;

            private float startTargetX;
            private float startTargetY;
            private float startTargetAngle;
            private float startTargetScale;

            private boolean isCleared = true;
            private boolean isClick;

            private boolean applyBoundAdjustX;
            private boolean applyBoundAdjustY;
            private boolean applyBoundAdjustAngle;

        private void clear() {
            isCleared = true;

            startX = startY = startAngle = startDiameter = 0;
            startTargetX = startTargetY = startTargetAngle = startTargetScale = 0;
            applyBoundAdjustAngle = applyBoundAdjustX = applyBoundAdjustY = false;
        }

        private void startTouch(MultiTouchController.PointInfo touchPoint) {
            isCleared = false;

            startTargetX = mCurrentStickeable.getPosX();
            startTargetY = mCurrentStickeable.getPosY();

            if (touchPoint.isMultiTouch() == true) {
                startTargetAngle = mCurrentStickeable.getAngle();
                startTargetScale = mCurrentStickeable.getScale();
            }
            startX = touchPoint.getX();
            startY = touchPoint.getY();

            if (touchPoint.isMultiTouch() == true) {
                startAngle = touchPoint.getMultiTouchAngle();
                startDiameter = touchPoint.getMultiTouchDiameter();
            }
        }

        private void applyTouch(MultiTouchController.PointInfo touchPoint) {
            float touchX = touchPoint.getX();
            float touchY = touchPoint.getY();
            int bound;
            float x = startTargetX + (touchX - startX);

            if (startTargetX * x < 0 || startTargetX == 0) {
                applyBoundAdjustX = true;
            }

            if (applyBoundAdjustX == true) {
                bound = Util.dp2px(StickerAttacherActivity.this, BOUND_XY);
                if (Math.abs(x) < bound) {
                    x = 0;
                } else if (x > 0) {
                    x -= bound;
                } else if (x < 0) {
                    x += bound;
                }
            }

            float y = startTargetY + (touchY - startY);
            if (startTargetY * y < 0 || startTargetY == 0) {
                applyBoundAdjustY = true;
            }

            if (applyBoundAdjustY == true) {
                bound = Util.dp2px(StickerAttacherActivity.this, BOUND_XY);
                if (Math.abs(y) < bound) {
                    y = 0;
                } else if (y > 0) {
                    y -= bound;
                } else if (y < 0) {
                    y += bound;
                }
            }

            mCurrentStickeable.setPosX(x);
            mCurrentStickeable.setPosY(y);

            if (touchPoint.isMultiTouch() == true) {
                float touchAngle = touchPoint.getMultiTouchAngle();
                float touchDiameter = touchPoint.getMultiTouchDiameter();

                float scale = touchDiameter / startDiameter;
                mCurrentStickeable.setScale(scale * startTargetScale);

                float angle = startTargetAngle + (float) Math.toDegrees(touchAngle - startAngle);
                angle = angle % 360;
                if (angle < 0) {
                    angle += 360;
                }

                if ((angle - 180) * (startTargetAngle - 180) < 0) {
                    applyBoundAdjustAngle = true;
                }

                if (applyBoundAdjustAngle == true) {
                    if (angle < BOUND_ANGLE || angle > 360 - BOUND_ANGLE) {
                        angle = 0;
                    } else if (angle > 180 && applyBoundAdjustAngle == true) {
                        angle += BOUND_ANGLE;
                    } else if (angle < 180 && applyBoundAdjustAngle == true) {
                        angle -= BOUND_ANGLE;
                    }
                }

                mCurrentStickeable.setAngle(angle);
            }
        }


        @Override
        public void getPositionAndScale(Object obj, MultiTouchController.PositionAndScale objPosAndScaleOut) {
            clear();
        }

        @Override
        public void selectObject(Object obj, MultiTouchController.PointInfo touchPoint) {

            if (touchPoint.isDown() == false) {
                float x = touchPoint.getX();
                float y = touchPoint.getY();

                stickerApplyAdapter.hit((int) x, (int) y);
            } else {
                isClick = true;
            }
        }

        @Override
        public Object getDraggableObjectAtPoint(MultiTouchController.PointInfo touchPoint) {
            return new Object();
        }

        @Override
        public boolean setPositionAndScale(Object obj, MultiTouchController.PositionAndScale newObjPosAndScale, MultiTouchController.PointInfo touchPoint) {
            isClick = false;

            if (mCurrentStickeable == null) {
                return false;
            }

            if (isCleared == true) {
                startTouch(touchPoint);
            } else {
                applyTouch(touchPoint);
            }

            return true;
        }
    });
}

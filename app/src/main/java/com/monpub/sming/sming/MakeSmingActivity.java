package com.monpub.sming.sming;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.monpub.sming.Constant;
import com.monpub.sming.R;
import com.monpub.sming.etc.Util;
import com.monpub.sming.sticker.StickerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import me.iwf.photopicker.PhotoPicker;

public class MakeSmingActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PICKER_LEFT = 201;
    public static final int REQUEST_CODE_PICKER_RIGHT = 202;

    private EditText editText;

    private ImageView leftImageView;
    private ImageView rightImageView;
    private View swap;

    private String leftImagePath;
    private String rightImagePath;

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

        if (photos.size() == 2) {
            if (requestCode == REQUEST_CODE_PICKER_LEFT) {
                leftImagePath = photos.get(0);
                rightImagePath = photos.get(1);
            } else if (requestCode == REQUEST_CODE_PICKER_RIGHT) {
                rightImagePath = photos.get(0);
                leftImagePath = photos.get(1);
            }
        } else if (photos.size() == 1) {
            if (requestCode == REQUEST_CODE_PICKER_LEFT) {
                leftImagePath = photos.get(0);
            } else if (requestCode == REQUEST_CODE_PICKER_RIGHT) {
                rightImagePath = photos.get(0);
            }
        }

        showImage();
    }

    private void showImage() {
        if (TextUtils.isEmpty(leftImagePath) == true &&  TextUtils.isEmpty(rightImagePath) == true ) {
            return;
        }

        swap.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(leftImagePath) == true) {
            leftImageView.setImageDrawable(null);
        } else {
            Glide.with(MakeSmingActivity.this)
                    .load(new File(leftImagePath))
                    .into(leftImageView);
        }

        if (TextUtils.isEmpty(rightImagePath) == true) {
            rightImageView.setImageDrawable(null);
        } else {
            Glide.with(MakeSmingActivity.this)
                    .load(new File(rightImagePath))
                    .into(rightImageView);
        }

        if (TextUtils.isEmpty(leftImagePath) == false &&  TextUtils.isEmpty(rightImagePath) == false) {
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_sming);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call

        float width = Util.getWidth(this);
        float height = Util.getHeight(this);

//        float aspectRatio = height / width;
        float aspectRatio = width / height;
//        float aspectRatio = 2f;

        leftImageView = (ImageView) findViewById(R.id.left_image);
        rightImageView = (ImageView) findViewById(R.id.right_image);

        if (Build.VERSION.SDK_INT <= 19) {
            try {
                Util.fixBackgroundRepeat(leftImageView);
                Util.fixBackgroundRepeat(rightImageView);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        PercentRelativeLayout.LayoutParams layoutParams;

        layoutParams = (PercentRelativeLayout.LayoutParams) leftImageView.getLayoutParams();
        layoutParams.getPercentLayoutInfo().aspectRatio = aspectRatio;
        leftImageView.setLayoutParams(layoutParams);
        leftImageView.requestLayout();

        layoutParams = (PercentRelativeLayout.LayoutParams) rightImageView.getLayoutParams();
        layoutParams.getPercentLayoutInfo().aspectRatio = aspectRatio;
        rightImageView.setLayoutParams(layoutParams);
        rightImageView.requestLayout();

        editText = (EditText) findViewById(R.id.filename);

        leftImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(2)
                        .setShowCamera(false)
                        .setShowGif(false)
                        .setPreviewEnabled(true)
                        .setGridColumnCount(2)
                        .start(MakeSmingActivity.this, REQUEST_CODE_PICKER_LEFT);

            }
        });
        rightImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(2)
                        .setShowCamera(false)
                        .setShowGif(false)
                        .setPreviewEnabled(true)
                        .setGridColumnCount(2)
                        .start(MakeSmingActivity.this, REQUEST_CODE_PICKER_RIGHT);
            }
        });
        swap = findViewById(R.id.swap);
        swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempPath = null;
                tempPath = leftImagePath;
                leftImagePath = rightImagePath;
                rightImagePath = tempPath;

                showImage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.make_sming_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_sming) {
            boolean result = false;
            if (TextUtils.isEmpty(leftImagePath) == false &&  TextUtils.isEmpty(rightImagePath) == false) {
                String filename = editText.getText().toString();
                if (TextUtils.isEmpty(filename) == true) {
                    filename = editText.getHint().toString();
                }

                final SmingData smingData = new SmingData(filename, leftImagePath, rightImagePath);
                if (smingData.canDrop() == true) {
                    if (StickerManager.getInstance().hasDefaultSticker() == false) {
                        dropSming(smingData, false);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MakeSmingActivity.this);
                        builder.setMessage("기본 스티커 설정이 있습니다.\n적용하시겠습니까?");
                        builder.setPositiveButton("적용", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dropSming(smingData, true);
                            }
                        });
                        builder.setNegativeButton("적용 안함", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dropSming(smingData, false);
                            }
                        });
                        builder.show();
                    }
                }
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dropSming(SmingData smingData, boolean withSticker) {
        boolean result = false;
        Bitmap bitmap = null;
        FileOutputStream fos = null;
        try {
            File storeDirectory = Constant.getSmingFolder();
            if (storeDirectory.exists() == false) {
                storeDirectory.mkdirs();
            }

            bitmap = smingData.getDropBitmap(MakeSmingActivity.this);
            if (StickerManager.getInstance().hasDefaultSticker() == true && withSticker == true) {
                StickerManager.getInstance().drawStickers(MakeSmingActivity.this, bitmap);
            }
            String fullFilename = smingData.getDropName();

            File file = new File(storeDirectory, fullFilename);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos = new FileOutputStream(file));
            result = true;

            try {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    // do nothing
                }
            });

        } catch (Throwable t) {
            // do nothing
            Toast.makeText(MakeSmingActivity.this, "스밍짤 저장에 실패했습니다ㅠㅠ", Toast.LENGTH_SHORT).show();
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable t) {
                    // do nothing
                    t.printStackTrace();
                }
            }
        }

        if (result == true) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}

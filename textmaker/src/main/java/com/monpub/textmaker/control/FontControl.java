package com.monpub.textmaker.control;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.monpub.textmaker.R;
import com.monpub.textmaker.TextMakingInfo;
import com.monpub.textmaker.TextPreviewView;
import com.monpub.textmaker.Util;
import com.rarepebble.colorpicker.ColorPickerView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by small-lab on 2016-08-30.
 */
public class FontControl extends AbsControl {
    public SeekBar textStrokeSeekBar;
    public ImageView textColorView;
    public TextView textFontView;

    private String[] assetFonts;

    public FontControl(TextPreviewView view, ColorPickerView colorPickerView,  final View controlPanel, final String fontPath) {
        super(view, colorPickerView);

        TextMakingInfo textMakingInfo = view.getTextMakingInfo();

        int seekBarMax = 100;

        textStrokeSeekBar = (SeekBar) controlPanel.findViewById(R.id.text_stroke);
        textStrokeSeekBar.setMax(seekBarMax);
        textStrokeSeekBar.setProgress((int) (textMakingInfo.getTextStrokeRatio() * seekBarMax));
        textStrokeSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        textColorView = (ImageView) controlPanel.findViewById(R.id.text_color);
        textColorView.setImageDrawable(new ColorDrawable(textMakingInfo.getTextColor()));
        textColorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final ColorPickerView colorPickerView = requestColorPickerView(v.getContext());
                colorPickerView.setColor(((ColorDrawable) textColorView.getDrawable()).getColor());
                colorPickerView.setOriginalColor(((ColorDrawable) textColorView.getDrawable()).getColor());
                colorPickerView.showAlpha(false);

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(colorPickerView);
                builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int color = colorPickerView.getColor();
                        textPreviewView.setTextColor(color);
                        textColorView.setImageDrawable(new ColorDrawable(color));

                        update();
                    }
                });

                builder.show();
            }
        });

        try {
            if (TextUtils.isEmpty(fontPath) == false) {
                File fontDirectory = new File(fontPath);
                if (fontDirectory.exists() == true && fontDirectory.isDirectory() == true) {
                    assetFonts = fontDirectory.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return filename != null && filename.toLowerCase().endsWith(".ttf");
                        }
                    });
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        textFontView = (TextView) controlPanel.findViewById(R.id.text_font);
        String fontFilePath = textMakingInfo.getFontPath();
        if (TextUtils.isEmpty(fontFilePath) == false) {
            File file = new File(fontFilePath);
            if (file != null && file.exists() == true && file.isFile() == true) {
                String fileName = file.getName();
                for (String font : assetFonts) {
                    if (fileName.equalsIgnoreCase(font) == true) {
                        textFontView.setText(fileName);
                        break;
                    }
                }
            }
        }
        if (assetFonts != null && assetFonts.length > 0) {
            final String[] fonts = new String[assetFonts.length + 1];
            fonts[0] = "기본 폰트";
            for (int i = 0; i < assetFonts.length; i++) {
                fonts[i + 1] = assetFonts[i];
            }
            textFontView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(controlPanel.getContext());

                    builder.setSingleChoiceItems(fonts, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean result;

                            if (which == 0) {
                                result = textPreviewView.setFont(null);
                            } else {
                                String fontFullPath = fontPath + "/" + assetFonts[which - 1];
                                result = textPreviewView.setFont(fontFullPath);
                            }

                            if (result == true) {
                                textFontView.setText(fonts[which]);
                            } else {
                                Toast.makeText(textFontView.getContext(), "폰트 적용에 실패했습니다.", Toast.LENGTH_LONG).show();
                            }

                            update();
                            dialog.dismiss();
                        }
                    });
                    builder.setTitle("/Sming/Fonts/안에 폰트를 넣으세요");
                    builder.show();
                }
            });
        }

        if (Build.VERSION.SDK_INT <= 19) {
            Util.fixBackgroundRepeat(textColorView);
        }
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int id = seekBar.getId();
            if (seekBar == textStrokeSeekBar) {
                textPreviewView.setTextStrokeRatio((float) progress / seekBar.getMax());
            }
            update();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}

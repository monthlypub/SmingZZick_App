package com.monpub.textmaker.control;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.monpub.textmaker.R;
import com.monpub.textmaker.TextMakingInfo;
import com.monpub.textmaker.TextPreviewView;
import com.monpub.textmaker.Util;
import com.rarepebble.colorpicker.ColorPickerView;

/**
 * Created by small-lab on 2016-08-30.
 */
public class OutlineControl extends AbsControl {
    public ImageView outlineColorView;
    public SeekBar outlineWidthSeekBar;

    public OutlineControl(TextPreviewView view, ColorPickerView colorPickerView, View controlPanel) {
        super(view, colorPickerView);

        TextMakingInfo textMakingInfo = view.getTextMakingInfo();

        outlineColorView = (ImageView) controlPanel.findViewById(R.id.outline_color);
        outlineColorView.setImageDrawable(new ColorDrawable(textMakingInfo.getOutlineColor()));
        outlineColorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final ColorPickerView colorPickerView = requestColorPickerView(v.getContext());

                colorPickerView.setColor(((ColorDrawable) outlineColorView.getDrawable()).getColor());
                colorPickerView.setOriginalColor(((ColorDrawable) outlineColorView.getDrawable()).getColor());
                colorPickerView.showAlpha(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(colorPickerView);
                builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int color = colorPickerView.getColor();
                        textPreviewView.setOutlineColor(color);
                        outlineColorView.setImageDrawable(new ColorDrawable(color));

                        update();
                    }
                });

                builder.show();

            }
        });

        int seekBarMax = 200;
        outlineWidthSeekBar = (SeekBar) controlPanel.findViewById(R.id.outline_width);
        outlineWidthSeekBar.setMax(seekBarMax);
        outlineWidthSeekBar.setProgress((int) (textMakingInfo.getTextOutlineRatio() * seekBarMax));
        outlineWidthSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        if (Build.VERSION.SDK_INT <= 19) {
            Util.fixBackgroundRepeat(outlineColorView);
        }
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int id = seekBar.getId();
            if (seekBar == outlineWidthSeekBar) {
                textPreviewView.setOutlineRatio((float) progress / seekBar.getMax());
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

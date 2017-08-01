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
import com.monpub.textmaker.widget.JogView;
import com.rarepebble.colorpicker.ColorPickerView;

/**
 * Created by small-lab on 2016-08-30.
 */
public class ShadowControl extends AbsControl {
    public SeekBar shadowRadiusSeekBar;
    public ImageView shadowColorView;
    public JogView shadowPosJogView;

    public ShadowControl(TextPreviewView view, ColorPickerView colorPickerView, View controlPanel) {
        super(view, colorPickerView);

        TextMakingInfo textMakingInfo = view.getTextMakingInfo();

        int seekBarMax = 200;
        shadowRadiusSeekBar = (SeekBar) controlPanel.findViewById(R.id.shadow_radius);
        shadowRadiusSeekBar.setMax(seekBarMax);
        shadowRadiusSeekBar.setProgress((int) (seekBarMax * textMakingInfo.getShadowRadius()));
        shadowRadiusSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        shadowColorView =  (ImageView) controlPanel.findViewById(R.id.shadow_color);
        shadowColorView.setImageDrawable(new ColorDrawable(textMakingInfo.getShadowColor()));
        shadowColorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final ColorPickerView colorPickerView = requestColorPickerView(v.getContext());

                colorPickerView.setColor(((ColorDrawable) shadowColorView.getDrawable()).getColor());
                colorPickerView.setOriginalColor(((ColorDrawable) shadowColorView.getDrawable()).getColor());
                colorPickerView.showAlpha(true);

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setView(colorPickerView);
                builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int color = colorPickerView.getColor();
                        textPreviewView.setShadowColor(color);
                        shadowColorView.setImageDrawable(new ColorDrawable(color));

                        update();
                    }
                });

                builder.show();

            }
        });

        shadowPosJogView = (JogView) controlPanel.findViewById(R.id.shadow_dxdy);
        shadowPosJogView.setOnJogChangeListener(new JogView.OnJogChangeListener() {
            @Override
            public void onJog(int dx, int dy) {
                textPreviewView.setShadowDxRatio((float) dx / shadowPosJogView.getWidth());
                textPreviewView.setShadowDyRatio((float) dy / shadowPosJogView.getHeight());
                update();
            }

            @Override
            public void onJogStart() {

            }

            @Override
            public void onJogEnd() {

            }
        });

        if (Build.VERSION.SDK_INT <= 19) {
            Util.fixBackgroundRepeat(shadowColorView);
        }
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int id = seekBar.getId();
            if (seekBar == shadowRadiusSeekBar) {
                textPreviewView.setShadowRadius((float) progress / seekBar.getMax());
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

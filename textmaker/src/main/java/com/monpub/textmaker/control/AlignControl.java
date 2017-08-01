package com.monpub.textmaker.control;

import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.monpub.textmaker.R;
import com.monpub.textmaker.TextMakingInfo;
import com.monpub.textmaker.TextPreviewView;
import com.rarepebble.colorpicker.ColorPickerView;

/**
 * Created by small-lab on 2016-08-30.
 */
public class AlignControl extends AbsControl {
    public SeekBar textSkewSeekBar;
    public SeekBar textScaleXSeekBar;
    public SeekBar textLetterSpacingSeekBar;
    public SeekBar textLineSpacingSeekBar;

    public AlignControl(TextPreviewView view, ColorPickerView colorPickerView, View controlPanel) {
        super(view, colorPickerView);

        TextMakingInfo textMakingInfo = view.getTextMakingInfo();

        int seekBarMax = 200;

        textSkewSeekBar = (SeekBar) controlPanel.findViewById(R.id.skew);
        textSkewSeekBar.setMax(seekBarMax);
        textSkewSeekBar.setProgress((int)(textMakingInfo.getSkewX() * seekBarMax + seekBarMax) / 2);
        textSkewSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        textLetterSpacingSeekBar = (SeekBar)  controlPanel.findViewById(R.id.letter_spacing);
        textLetterSpacingSeekBar.setMax(seekBarMax);
        textLetterSpacingSeekBar.setProgress((int)(textMakingInfo.getLetterSpacing() * seekBarMax + seekBarMax) / 2);
        textLetterSpacingSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        textScaleXSeekBar = (SeekBar)  controlPanel.findViewById(R.id.scalex);
        textScaleXSeekBar.setMax(seekBarMax);
        textScaleXSeekBar.setProgress((int)((textMakingInfo.getScaleX() - 0.5f) * seekBarMax));
        textScaleXSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        textLineSpacingSeekBar = (SeekBar)  controlPanel.findViewById(R.id.line_spacing);
        textLineSpacingSeekBar.setMax(seekBarMax);
        textLineSpacingSeekBar.setProgress((int)((textMakingInfo.getLineSpacing() - 0.5f) * seekBarMax));
        textLineSpacingSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        View align = controlPanel.findViewById(R.id.align);
        align.findViewById(R.id.left).setTag(Paint.Align.LEFT);
        align.findViewById(R.id.left).setOnClickListener(onAlignClickListener);
        align.findViewById(R.id.center).setTag(Paint.Align.CENTER);
        align.findViewById(R.id.center).setOnClickListener(onAlignClickListener);
        align.findViewById(R.id.right).setTag(Paint.Align.RIGHT);
        align.findViewById(R.id.right).setOnClickListener(onAlignClickListener);

    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int id = seekBar.getId();
            if (seekBar == textSkewSeekBar) {
                textPreviewView.setSkewX((float) (progress - seekBar.getMax() / 2) / seekBar.getMax() * 2);
            } else if (seekBar == textLetterSpacingSeekBar) {
                textPreviewView.setLetterSpacing((float) (progress - seekBar.getMax() / 2) / seekBar.getMax() * 2);
            } else if (seekBar == textScaleXSeekBar) {
                textPreviewView.setScaleX((float) progress / seekBar.getMax() + 0.5f);
            } else if (seekBar == textLineSpacingSeekBar) {
                textPreviewView.setLineSpacing((float) progress / seekBar.getMax() + 0.5f);
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

    private View.OnClickListener onAlignClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Paint.Align align = (Paint.Align) v.getTag();
            textPreviewView.setAlign(align);
            update();
        }
    };
}

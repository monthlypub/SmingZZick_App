package com.monpub.textmaker.control;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.monpub.textmaker.TextPreviewView;
import com.rarepebble.colorpicker.ColorPickerView;

/**
 * Created by small-lab on 2016-08-30.
 */
public abstract class AbsControl {
    final protected  TextPreviewView textPreviewView;
    final private ColorPickerView colorPickerView;

    public AbsControl(TextPreviewView view, ColorPickerView colorPickerView) {
        this.textPreviewView = view;
        this.colorPickerView = colorPickerView;

        ViewGroup pickerLayout =  (ViewGroup) colorPickerView.getChildAt(0);
        int childCount = pickerLayout.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = pickerLayout.getChildAt(i);
            if (child instanceof EditText) {
                ((EditText) child).setTextColor(Color.WHITE);
                break;
            }
        }
    }

    public void update() {
        textPreviewView.postInvalidate();
        textPreviewView.requestLayout();
    }

    protected ColorPickerView requestColorPickerView(Context context) {
        if (colorPickerView.getParent() != null) {
            ((ViewGroup) colorPickerView.getParent()).removeView(colorPickerView);
        }

        return colorPickerView;
    }
}

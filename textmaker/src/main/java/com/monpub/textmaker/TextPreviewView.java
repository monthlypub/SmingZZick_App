package com.monpub.textmaker;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.Calendar;

/**
 * Created by small-lab on 2016-08-28.
 */
public class TextPreviewView extends View {
    private String[] defaultText = new String[] {"텍스트를", "입력해", "보아요"};
    protected TextMakingInfo textMakingInfo;

    private float textScale = 1f;
    private boolean autoResize = true;

    public TextPreviewView(Context context) {
        super(context);
        init(context);
    }

    public TextPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public TextPreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
//        setLayerType(LAYER_TYPE_SOFTWARE, null);
        textMakingInfo = new TextMakingInfo();
        textMakingInfo.setDefaultText(defaultText);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        textMakingInfo.draw(getContext(), textScale, getWidth(), getHeight(), canvas);
    }

    private ValueAnimator fontSizeAnimator;
    private ValueAnimator.AnimatorUpdateListener fontSizeAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float size = (Float) animation.getAnimatedValue();
//            Log.d("SSS_d", "size - " + size);
            setTextSize(size);
            requestLayout();
        }
    };
    private Animator.AnimatorListener fontSizeAnimatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            fontSizeAnimator = null;
            requestLayout();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidthDimen = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeightDimen = MeasureSpec.getSize(heightMeasureSpec);

        int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        ViewGroup parent = (ViewGroup) getParent();
        int parentWidth = parent.getMeasuredWidth();
        int parentHeight = Math.min(parent.getMeasuredHeight(), measureHeightDimen);

//        Log.d("ZZZ_d", "parent - " + parentWidth + ", " + parentHeight);
        int width = 0, height = 0;

        Rect rect = new Rect();
        textMakingInfo.getTextRect(getContext(), textScale, rect);

        width = rect.width();
        height = rect.height();
        if (width == 0) {
            width = 1;
        }

        if (height == 0) {
            height = 1;
        }

        setMeasuredDimension(width, height);

        if (parentWidth * parentHeight * width * height == 0) {
            return;
        }

        if (autoResize == true) {
            if (fontSizeAnimator == null) {
                if (parentWidth < width || parentHeight < height) {
                    float widthZoomRatio = (float) (parentWidth * 0.7f) / width;
                    float heightZoomRatio = (float) (parentHeight * 0.7f) / height;

                    float zoomRatio = widthZoomRatio > heightZoomRatio ? heightZoomRatio : widthZoomRatio;

//                Log.d("ZZZ_d", "smaller - " + zoomRatio);

                    fontSizeAnimator = ValueAnimator.ofFloat(textMakingInfo.getTextSize(), textMakingInfo.getTextSize() * zoomRatio);
                    fontSizeAnimator.addUpdateListener(fontSizeAnimatorUpdateListener);
                    fontSizeAnimator.addListener(fontSizeAnimatorListener);
                    fontSizeAnimator.setDuration(300);
                    fontSizeAnimator.start();
                } else if (width < (parentWidth * 0.6f) && height < (parentHeight * 0.6f)) {
                    if (textMakingInfo.getTextSize() >= TextMakingInfo.MAX_TEXT_SIZE) {
                        // do nothing
                        return;
                    }

                    float widthZoomRatio = (float) (parentWidth * 0.8f) / width;
                    float heightZoomRatio = (float) (parentHeight * 0.8f) / height;

                    float zoomRatio = widthZoomRatio > heightZoomRatio ? heightZoomRatio : widthZoomRatio;

//                Log.d("ZZZ_d", "bigger - " + zoomRatio);

                    float toSize = textMakingInfo.getTextSize() * zoomRatio;
                    if (toSize > TextMakingInfo.MAX_TEXT_SIZE) {
                        toSize = TextMakingInfo.MAX_TEXT_SIZE;
                    }

                    fontSizeAnimator = ValueAnimator.ofFloat(textMakingInfo.getTextSize(), toSize);
                    fontSizeAnimator.addUpdateListener(fontSizeAnimatorUpdateListener);
                    fontSizeAnimator.addListener(fontSizeAnimatorListener);
                    fontSizeAnimator.setDuration(300);
                    fontSizeAnimator.start();
                }
            }
        }
    }

    public String getText() {
        return textMakingInfo.getTextRaw();
    }

    public void setText(String text) {
        String[] splits;
        if (TextUtils.isEmpty(text) == true) {
            splits = new String[0];
        } else {
            splits = text.split("\n");
        }
        textMakingInfo.setText(splits);

        requestLayout();
    }

    public float getTextSize() {
        return textMakingInfo.getTextSize();
    }

    public void setTextSize(float textSize) {
        textMakingInfo.setTextSize(textSize);
    }

    public int getTextColor() {
        return textMakingInfo.getTextColor();
    }

    public void setTextColor(int textColor) {
        textMakingInfo.setTextColor(textColor);
    }

    public float getOutlineRatio() {
        return textMakingInfo.getTextOutlineRatio();
    }

    public void setOutlineRatio(float outlineRatio) {
        textMakingInfo.setTextOutlineRatio(outlineRatio);
    }

    public int getOutlineColor() {
        return textMakingInfo.getOutlineColor();

    }

    public void setOutlineColor(int outlineColor) {
        textMakingInfo.setOutlineColor(outlineColor);
    }

    public float getSkewX() {
        return textMakingInfo.getSkewX();
    }

    public void setSkewX(float skewX) {
        textMakingInfo.setSkewX(skewX);
    }

    public float getShadowRadius() {
        return textMakingInfo.getShadowRadius();
    }

    public void setShadowRadius(float shadowRadius) {
        textMakingInfo.setShadowRadius(shadowRadius);
    }

    public float getShadowDxRatio() {
        return textMakingInfo.getShadowDxRatio();
    }

    public void setShadowDxRatio(float shadowDxRatio) {
        if (Math.abs(shadowDxRatio) > 1) {
            shadowDxRatio = shadowDxRatio / Math.abs(shadowDxRatio);
        }
        textMakingInfo.setShadowDxRatio(shadowDxRatio);
    }

    public float getShadowDyRatio() {
        return textMakingInfo.getShadowDyRatio();
    }

    public void setShadowDyRatio(float shadowDyRatio) {
        textMakingInfo.setShadowDyRatio(shadowDyRatio);
    }

    public int getShadowColor() {
        return textMakingInfo.getShadowColor();
    }

    public void setShadowColor(int shadowColor) {
        textMakingInfo.setShadowColor(shadowColor);
    }

    public float getTextStrokeRatio() {
        return textMakingInfo.getTextStrokeRatio();
    }

    public void setTextStrokeRatio(float textStrokeRatio) {
        textMakingInfo.setTextStrokeRatio(textStrokeRatio);
    }

    public float getLetterSpacing() {
        return textMakingInfo.getLetterSpacing();
    }

    public void setLetterSpacing(float letterSpacing) {
        textMakingInfo.setLetterSpacing(letterSpacing);
    }

    @Override
    public float getScaleX() {
        return textMakingInfo.getScaleX();
    }

    @Override
    public void setScaleX(float scaleX) {
        textMakingInfo.setScaleX(scaleX);
    }

    public float getLineSpacing() {
        return textMakingInfo.getLineSpacing();
    }

    public void setLineSpacing(float lineSpacing) {
        textMakingInfo.setLineSpacing(lineSpacing);
    }

    public Paint.Align getAlign() {
        return textMakingInfo.getAlign();
    }

    public void setAlign(Paint.Align align) {
        textMakingInfo.setAlign(align);
    }

    public boolean setFont(String fontPath) {
        return textMakingInfo.setFont(getContext(), fontPath);
    }

    public TextMakingInfo getTextMakingInfo() {
        return textMakingInfo;
    }

    public void setTextMakingInfo(TextMakingInfo textMakingInfo) {
        this.textMakingInfo = textMakingInfo;
        postInvalidate();
        requestLayout();
    }

    public void setAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
    }

    public float getTextScale() {
        return textScale;
    }

    public void setTextScale(float textScale) {
        this.textScale = textScale;
    }
}


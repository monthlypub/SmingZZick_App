package com.monpub.textmaker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by small-lab on 2016-09-14.
 */
public class TextMakingInfo implements Parcelable {
    public static final float MAX_TEXT_SIZE = 40f;

    protected String[] text;
    protected String[] defaultText;

    protected float textSize = MAX_TEXT_SIZE;
    protected int textColor = Color.WHITE;
    protected float textStrokeRatio = 0f;

    protected int outlineColor = Color.BLACK;
    protected float textOutlineRatio;

    protected float letterSpacing = 0f;
    protected float lineSpacing = 1f;
    protected float scaleX = 1f;
    protected float skewX;

    protected float shadowRadius = 1f;
    protected float shadowDxRatio;
    protected float shadowDyRatio;
    protected int shadowColor = Color.BLACK;

    protected Paint.Align align = Paint.Align.LEFT;

    protected Typeface typeface;
    protected String fontPath;

    protected float widthTextRatio;
    protected float heightTextRatio;

    public Paint getPaint(Context context, boolean outline) {
        return getPaint(context, 1f, outline);
    }

    public Paint getPaint(Context context, float scale, boolean outline) {
        float textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this.textSize,
                context.getResources().getDisplayMetrics()) * scale;

        Paint textPaint = new Paint();

        if (typeface != null) {
            textPaint.setTypeface(typeface);
        }

        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);

        if (outline == false) {
            textPaint.setStrokeWidth(textSize * textStrokeRatio);
            textPaint.setColor(textColor);
        } else {
            textPaint.setStrokeWidth(textSize * textStrokeRatio + textSize * textOutlineRatio);
            textPaint.setColor(outlineColor);
        }


        // skew
        textPaint.setTextSkewX(skewX);


        // scaleX
        textPaint.setTextScaleX(scaleX);

        if (Build.VERSION.SDK_INT >= 21) {
            textPaint.setLetterSpacing(letterSpacing);
        }

        return textPaint;
    }

    public void draw(Context context, int width, int height, Canvas canvas) {
        draw(context, 1f, width, height, canvas);
    }

    private String[] preprocessText() {
        String[] text;
        if (this.text == null || this.text.length == 0 || (this.text.length == 1 && TextUtils.isEmpty(this.text[0]) == true)) {
            text = defaultText;
        } else {
            text = new String[this.text.length];
            for (int i = 0; i < text.length; i++) {
                text[i] = new String(this.text[i] != null ? this.text[i] : "");

                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat sdf = new SimpleDateFormat();
                for (int j = 0; j < DATE_REPLACEMENTS.length; j++) {
                    while (text[i].indexOf(DATE_REPLACEMENTS[j]) >= 0) {
                        sdf.applyPattern(DATE_FORMAT[j]);
                        text[i] = text[i].replace(DATE_REPLACEMENTS[j], sdf.format(date));
                    }
                }
            }
        }

        if (text == null) {
            if (defaultText == null) {
                text = new String[]{""};
            } else {
                text = defaultText;
            }
        }

        return text;
    }

    public void draw(Context context, float scale,  int width, int height, Canvas canvas) {
        if (TextUtils.isEmpty(fontPath) == false && typeface == null) {
            setFont(context, fontPath);
        }

        float textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this.textSize,
                context.getResources().getDisplayMetrics()) * scale;
        ;
        String[] text = preprocessText();

        Paint textPaint = getPaint(context, scale, false);
        Paint outlineTextPaint = getPaint(context, scale, true);

        // outline
        float shadowRadius = this.shadowRadius;
        shadowRadius *= this.textSize / 40f;

        if (shadowRadius > 25f) {
            shadowRadius = 25;
        }

        if (textOutlineRatio > 0) {
            outlineTextPaint.setShadowLayer(shadowRadius, this.textSize * shadowDxRatio, this.textSize * shadowDyRatio, shadowColor);
        } else {
            textPaint.setShadowLayer(shadowRadius, this.textSize * shadowDxRatio, this.textSize * shadowDyRatio, shadowColor);
        }

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float fontHeight = fontMetrics.bottom - fontMetrics.top;

        float fullheight = 0, fullwidth = 0;
        float[] offsetXArray = new float[text.length];
        float[] offsetYArray = new float[text.length];

        float[] heightArray = new float[text.length];
        float[] widthArray = new float[text.length];

        for (int i = 0; i < text.length; i++) {
            Rect bounds = new Rect();
            textPaint.getTextBounds(text[i], 0, text[i].length(), bounds);

            widthArray[i] = bounds.width();
            heightArray[i] = fontMetrics.bottom - fontMetrics.top;

            if (i < text.length - 1) {
                heightArray[i] = heightArray[i] + (int) (heightArray[i] * (lineSpacing - 1));
            }

            offsetXArray[i] = -bounds.left;
            offsetYArray[i] = -bounds.top + (bounds.top - fontMetrics.top);

            fullheight += heightArray[i];
            fullwidth = fullwidth < bounds.width() ? bounds.width() : fullwidth;
        }

        float textBoxX = (width - fullwidth) / 2;
        float textBoxY=  (height - fullheight) / 2;
        for (int i = 0; i < text.length; i++) {
            float x = textBoxX;
            float y = textBoxY;

            x += offsetXArray[i];
            y += offsetYArray[i];

            if (align == Paint.Align.RIGHT) {
                x += fullwidth - widthArray[i];
            } else if (align == Paint.Align.CENTER) {
                x += (fullwidth - widthArray[i]) / 2;
            }

            if (y > 0) {
                for (int j = 0; j < i; j++) {
                    y += heightArray[j];
                }
            }

            if (outlineTextPaint != null) {
                canvas.drawText(text[i], x, y, outlineTextPaint);
            }

            // draw text
            canvas.drawText(text[i], x, y, textPaint);

        }

    }

    public void getTextRect(Context context, Rect outRect) {
        getTextRect(context, 1f, outRect);
    }

    public void getTextRect(Context context, float scale, Rect outRect) {
        Paint textPaint = getPaint(context, scale, false);
        Paint outlineTextPaint = getPaint(context, scale, true);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();

        String[] text = preprocessText();

        float fullheight = 0, fullwidth = 0;
        float[] heightArray = new float[text.length];
        float[] widthArray = new float[text.length];

        for (int i = 0; i < text.length; i++) {
            Rect bounds = new Rect();
            if (outlineTextPaint != null) {
                outlineTextPaint.getTextBounds(text[i], 0, text[i].length(), bounds);
            } else {
                textPaint.getTextBounds(text[i], 0, text[i].length(), bounds);
            }

            widthArray[i] = bounds.width();
            heightArray[i] = fontMetrics.bottom - fontMetrics.top;

            if (i < text.length - 1) {
                heightArray[i] = heightArray[i] + (int) (heightArray[i] * (lineSpacing - 1));
            }

            fullheight += heightArray[i];
            fullwidth = fullwidth < widthArray[i] ? widthArray[i] : fullwidth;
        }

        fullwidth = fullwidth * 1.2f;
        fullheight = fullheight * 1.2f;

        float fullWidthDP = fullwidth / context.getResources().getDisplayMetrics().density;
        float fullHeightDP = fullheight / context.getResources().getDisplayMetrics().density;

        widthTextRatio = fullWidthDP / (textSize * scale);
        heightTextRatio = fullHeightDP / (textSize * scale);

        outRect.set(0, 0, (int) fullwidth, (int) fullheight);
    }

    public void setDefaultText(String[] defaultText) {
        this.defaultText = defaultText;
    }

    public String getTextRaw() {
        if (text == null) {
            return null;
        }
        return TextUtils.join("\n", text);
    }

    public String[] getText() {
        return text;
    }

    public void setText(String[] texts) {
        this.text = texts;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public float getTextStrokeRatio() {
        return textStrokeRatio  / 0.1f;
    }

    public void setTextStrokeRatio(float textStrokeRatio) {
        this.textStrokeRatio = textStrokeRatio * 0.1f;
    }

    public int getOutlineColor() {
        return outlineColor;
    }

    public void setOutlineColor(int outlineColor) {
        this.outlineColor = outlineColor;
    }

    public float getTextOutlineRatio() {
        return textOutlineRatio / 0.25f;
    }

    public void setTextOutlineRatio(float textOutlineRatio) {
        this.textOutlineRatio = textOutlineRatio * 0.25f;
    }

    public float getLetterSpacing() {
        return letterSpacing / 0.2f;
    }

    public void setLetterSpacing(float letterSpacing) {
        if (letterSpacing == 0) {
            return;
        }

        this.letterSpacing = letterSpacing * 0.2f;
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getSkewX() {
        return skewX;
    }

    public void setSkewX(float skewX) {
        this.skewX = skewX;
    }

    public float getShadowRadius() {
        return shadowRadius / 25;
    }

    public void setShadowRadius(float shadowRadius) {
        shadowRadius *= 25;
        if (shadowRadius < 1f) {
            shadowRadius = 1f;
        }
        this.shadowRadius = shadowRadius;
    }

    public float getShadowDxRatio() {
        return shadowDxRatio;
    }

    public void setShadowDxRatio(float shadowDxRatio) {
        if (Math.abs(shadowDxRatio) > 1) {
            shadowDxRatio = shadowDxRatio / Math.abs(shadowDxRatio);
        }
        this.shadowDxRatio = shadowDxRatio;
    }

    public float getShadowDyRatio() {
        return shadowDyRatio;
    }

    public void setShadowDyRatio(float shadowDyRatio) {
        if (Math.abs(shadowDyRatio) > 1) {
            shadowDyRatio = shadowDyRatio / Math.abs(shadowDyRatio);
        }
        this.shadowDyRatio = shadowDyRatio;
    }

    public int getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    public Paint.Align getAlign() {
        return align;
    }

    public void setAlign(Paint.Align align) {
        this.align = align;
    }

    public boolean setFont(Context context, String fontPath) {
        try {
            if (TextUtils.isEmpty(fontPath) == true) {
                typeface = null;
                this.fontPath = null;
                return true;
            } else {
                typeface = Typeface.createFromFile(fontPath);
                if (typeface != null) {
                    this.fontPath = fontPath;
                    return true;
                } else {
                    this.fontPath = null;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return false;
    }

    public float getWidthTextRatio() {
        return widthTextRatio;
    }

    public void setWidthTextRatio(float widthTextRatio) {
        this.widthTextRatio = widthTextRatio;
    }

    public float getHeightTextRatio() {
        return heightTextRatio;
    }

    public void setHeightTextRatio(float heightTextRatio) {
        this.heightTextRatio = heightTextRatio;
    }

    public String getFontPath() {
        return fontPath;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(text);
        dest.writeFloat(textSize);
        dest.writeInt(textColor);
        dest.writeFloat(textStrokeRatio);

        dest.writeInt(outlineColor);
        dest.writeFloat(textOutlineRatio);

        dest.writeFloat(letterSpacing);
        dest.writeFloat(lineSpacing);
        dest.writeFloat(scaleX);
        dest.writeFloat(skewX);

        dest.writeFloat(shadowRadius);
        dest.writeFloat(shadowDxRatio);
        dest.writeFloat(shadowDyRatio);
        dest.writeInt(shadowColor);

        dest.writeInt(align.ordinal());
        dest.writeString(fontPath);

        dest.writeFloat(widthTextRatio);
        dest.writeFloat(heightTextRatio);
    }

    public TextMakingInfo() {

    }

    private TextMakingInfo(Parcel in) {
        text = in.createStringArray();
        textSize = in.readFloat();
        textColor = in.readInt();
        textStrokeRatio = in.readFloat();

        outlineColor = in.readInt();
        textOutlineRatio = in.readFloat();

        letterSpacing = in.readFloat();
        lineSpacing = in.readFloat();
        scaleX = in.readFloat();
        skewX = in.readFloat();

        shadowRadius = in.readFloat();
        shadowDxRatio = in.readFloat();
        shadowDyRatio = in.readFloat();
        shadowColor = in.readInt();

        align = Paint.Align.values()[in.readInt()];
        fontPath = in.readString();

        widthTextRatio = in.readFloat();
        heightTextRatio = in.readFloat();
    }

    public static TextMakingInfo fromJSONString(Context context, String jsonString) {
        TextMakingInfo textMakingInfo = null;
        try {
            TextMakingInfo parsed = new TextMakingInfo();

            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray textJSONArray = jsonObject.getJSONArray("text");
            String[] text = new String[textJSONArray.length()];
            for (int i = 0; i < textJSONArray.length(); i++) {
                text[i] = textJSONArray.getString(i);
            }
            parsed.setText(text);

            float textSize = (float) jsonObject.getDouble("textSize");
            parsed.setTextSize(textSize);

            int textColor = jsonObject.getInt("textColor");
            parsed.setTextColor(textColor);

            float textStrokeRatio = (float) jsonObject.getDouble("textStrokeRatio");
            parsed.setTextStrokeRatio(textStrokeRatio);

            int outlineColor = jsonObject.getInt("outlineColor");
            parsed.setOutlineColor(outlineColor);

            float textOutlineRatio = (float) jsonObject.getDouble("textOutlineRatio");
            parsed.setTextOutlineRatio(textOutlineRatio);

            float letterSpacing = (float) jsonObject.getDouble("letterSpacing");
            parsed.setLetterSpacing(letterSpacing);

            float lineSpacing = (float) jsonObject.getDouble("lineSpacing");
            parsed.setLineSpacing(lineSpacing);

            float scaleX = (float) jsonObject.getDouble("scaleX");
            parsed.setScaleX(scaleX);

            float skewX = (float) jsonObject.getDouble("skewX");
            parsed.setSkewX(skewX);

            float shadowRadius = (float) jsonObject.getDouble("shadowRadius");
            parsed.setShadowRadius(shadowRadius);

            float shadowDxRatio = (float) jsonObject.getDouble("shadowDxRatio");
            parsed.setShadowDxRatio(shadowDxRatio);

            float shadowDyRatio = (float) jsonObject.getDouble("shadowDyRatio");
            parsed.setShadowDyRatio(shadowDyRatio);

            int shadowColor = jsonObject.getInt("shadowColor");
            parsed.setShadowColor(shadowColor);

            int align = jsonObject.getInt("align");
            parsed.setAlign(Paint.Align.values()[align]);

            if (jsonObject.has("fontPath") == true) {
                String fontPath = jsonObject.getString("fontPath");
                parsed.setFont(context, fontPath);
            }

            float widthTextRatio = (float) jsonObject.getDouble("widthTextRatio");
            parsed.setWidthTextRatio(widthTextRatio);

            float heightTextRatio = (float) jsonObject.getDouble("heightTextRatio");
            parsed.setHeightTextRatio(heightTextRatio);

            textMakingInfo = parsed;
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return textMakingInfo;
    }

    public String toJSONString() {
        String jsonString = null;

        try {
            JSONObject jsonObject = new JSONObject();

            JSONArray jsonArray = new JSONArray();
            for (String textPart : text) {
                jsonArray.put(textPart);
            }
            jsonObject.put("text", jsonArray);

            jsonObject.put("textSize", getTextSize());
            jsonObject.put("textColor", getTextColor());
            jsonObject.put("textStrokeRatio", getTextStrokeRatio());

            jsonObject.put("outlineColor", getOutlineColor());
            jsonObject.put("textOutlineRatio", getTextOutlineRatio());

            jsonObject.put("letterSpacing", getLetterSpacing());
            jsonObject.put("lineSpacing", getLineSpacing());
            jsonObject.put("scaleX", getScaleX());
            jsonObject.put("skewX", getSkewX());

            jsonObject.put("shadowRadius", getShadowRadius());
            jsonObject.put("shadowDxRatio", getShadowDxRatio());
            jsonObject.put("shadowDyRatio", getShadowDyRatio());
            jsonObject.put("shadowColor", getShadowColor());

            jsonObject.put("align", align.ordinal());
            jsonObject.put("fontPath", fontPath);

            jsonObject.put("widthTextRatio", widthTextRatio);
            jsonObject.put("heightTextRatio", heightTextRatio);

            jsonString = jsonObject.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return jsonString;
    }

    public boolean isEmpty() {
        return text == null || text.length == 0 || (text.length == 1 && TextUtils.isEmpty(text[0]) == true);
    }

    public static final Parcelable.Creator<TextMakingInfo> CREATOR = new Parcelable.Creator<TextMakingInfo>() {
        public TextMakingInfo createFromParcel(Parcel in) {
            return new TextMakingInfo(in);
        }

        public TextMakingInfo[] newArray(int size) {
            return new TextMakingInfo[size];
        }
    };


    public static String[] DATE_REPLACEMENTS = new String[]{
            "[[M.D]]",
            "[[월.일]]",
            "[[M/D]]",
            "[[MMDD]]",
            "[[시.분]]",
            "[[시:분]]"
    };

    public static String[] DATE_FORMAT = new String[]{
            "M.d",
            "M월 d일",
            "M/d",
            "MMdd",
            "H시 m분",
            "HH:mm"
    };

}

package com.monpub.textmaker.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by small-lab on 2016-08-31.
 */
public class JogView extends View{
    private OnJogChangeListener mOnJogChangeListener;
    private float centerRadius = dp2px(20f);

    public JogView(Context context) {
        super(context);
        init();
    }

    public JogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public JogView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        float minDP = 60;
        int minPx = dp2px(minDP);

        setMinimumWidth(minPx);
        setMinimumHeight(minPx);
    }

    public void setOnJogChangeListener(OnJogChangeListener listener) {
        mOnJogChangeListener = listener;
    }


    private boolean centerPressed;
    private float dx;
    private float dy;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        int action = event.getActionMasked();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN : {
                if (isCenterPoint(x, y) == true) {
                    centerPressed = true;
                    if (mOnJogChangeListener != null) {
                        mOnJogChangeListener.onJogStart();
                    }
                    return true;
                }
            }
            break;
            case MotionEvent.ACTION_MOVE : {
                if (centerPressed == true) {

                    if (isCenterPoint(x, y) == true) {
//                        Log.d("CCC_d", "in center");
                        dx = 0;
                        dy = 0;
                    } else {
//                        Log.d("CCC_d", "out center");
                        float centerX = getWidth() / 2;
                        float centerY = getHeight() / 2;

                        float bigDx = x - centerX;
                        float bigDy = y - centerY;
                        float bigDr = (float) Math.sqrt(Math.pow(bigDx, 2) + Math.pow(bigDy, 2));

                        float ratio = 1f - centerRadius / bigDr;
                        dx = bigDx * ratio;
                        dy = bigDy * ratio;

                    }

                    if (mOnJogChangeListener != null) {
                        mOnJogChangeListener.onJog((int) dx, (int) dy);
                    }
                    postInvalidate();
                }
            }
            break;
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL : {
                if (centerPressed == true) {
                    if (mOnJogChangeListener != null) {
                        mOnJogChangeListener.onJogEnd();
                    }
                }
                centerPressed = false;
                dx = 0;
                dy = 0;
                postInvalidate();
            }
            break;
        }
        return false;
    }

    private boolean isCenterPoint(float x, float y) {
        float width = getWidth();
        float height = getHeight();

        float absDx = Math.abs(x - width / 2);
        float absDy = Math.abs(y - height / 2);

        if (absDx > centerRadius) {
            return false;
        }

        float powX = (float) Math.pow(absDx, 2);
        float powR = (float) Math.pow(centerRadius, 2);
        float powY = Math.abs(powR - powX);

        return absDy <= Math.sqrt(powY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float radius, cx, cy;

        cx = width / 2;
        cy = height / 2;


        Paint paint = new Paint();
        paint.setColor(0x99ffffff & Color.DKGRAY);

        float strokeWidth = dp2px(3f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);

        radius = (width - strokeWidth) / 2;
        canvas.drawCircle(cx, cy, radius, paint);

        paint.setStyle(Paint.Style.FILL);
        if (centerPressed == true) {
            paint.setColor(0x99ff0000);
        }

        radius = dp2px(20f);

        float minX = radius;
        float maxX = width - radius;
        float minY = radius;
        float maxY = height - radius;

        cx += dx;
        cy += dy;

        if (cx < minX) {
            cx = minX;
        }
        if (cx > maxX) {
            cx = maxX;
        }
        if (cy < minY) {
            cy = minY;
        }
        if (cy > maxY) {
            cy = maxY;
        }

        canvas.drawCircle(cx, cy, radius, paint);

//        Log.d("EEE_d", "dx : " + dx + ", dy : " + dy);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public interface OnJogChangeListener {
        public void onJog(int dx, int dy);
        public void onJogStart();
        public void onJogEnd();
    }
}

package com.monpub.sming.sming;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.monpub.sming.Constant;
import com.monpub.sming.MainActivity;
import com.monpub.sming.R;
import com.monpub.sming.etc.Util;

/**
 * Created by small-lab on 2016-09-28.
 */

public class CaptureWidthPreference extends DialogPreference {

    @TargetApi(21)
    public CaptureWidthPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CaptureWidthPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CaptureWidthPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(21)
    public CaptureWidthPreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.layout_width_progress, null);

        final SeekBar seekBar = (SeekBar) viewGroup.findViewById(R.id.seek);
        final TextView widthText = (TextView) viewGroup.findViewById(R.id.width);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                widthText.setText("" + (progress * 2 + Constant.MIN_SMING_WIDTH));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int maxValue = Util.getWidth((Activity) getContext());
        seekBar.setMax((maxValue * 2 - Constant.MIN_SMING_WIDTH) / 2);
        seekBar.setProgress((SmingManager.getInstance().getSmingWdith() - Constant.MIN_SMING_WIDTH) / 2);

        builder.setView(viewGroup);

        builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SmingManager.getInstance().setSmingWidth(seekBar.getProgress() * 2 + Constant.MIN_SMING_WIDTH);
                notifyChanged();
            }
        });
        builder.setNegativeButton("취소", null);
    }

    @Override
    public CharSequence getSummary() {
        try {
            return SmingManager.getInstance().getSmingWdith() + "px";
        } catch (Throwable t) {
            return 0 + "px";
        }
    }
}

package com.monpub.textmaker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.monpub.textmaker.control.AlignControl;
import com.monpub.textmaker.control.FontControl;
import com.monpub.textmaker.control.OutlineControl;
import com.monpub.textmaker.control.ShadowControl;
import com.rarepebble.colorpicker.ColorPickerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TextMakeFragment extends Fragment {
    private static final String FONT_PATH = "font_path";
    private static final String TEXT_MAKING_DATA = "text_making_data";

    private int DEFAULT_HEIGHT_DIFF = -1;

    private String fontPath;
    private TextMakingInfo textMakingInfo;

    private TextPreviewView textPreviewView;

    private View frameFont;
    private View frameOutline;
    private View frameShadow;
    private View frameSkew;

    private View buttonFont;
    private View buttonOutline;
    private View buttonShadow;
    private View buttonSkew;

    private EditText editText;
    private View buttonPutDate;

    private FontControl fontControl;
    private OutlineControl outlineControl;
    private ShadowControl shadowControl;
    private AlignControl alignControl;


    private OnFragmentInteractionListener mListener;

    public TextMakeFragment() {
        // Required empty public constructor
    }

    public static TextMakeFragment newInstance(String fontPath, TextMakingInfo textMakingInfo) {
        TextMakeFragment fragment = new TextMakeFragment();
        Bundle args = new Bundle();
        args.putString(FONT_PATH, fontPath);
        args.putParcelable(TEXT_MAKING_DATA, textMakingInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fontPath = getArguments().getString(FONT_PATH);
            textMakingInfo = getArguments().getParcelable(TEXT_MAKING_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context contextWrapper = new ContextThemeWrapper(getActivity(), R.style.TextMakerTheme);
        inflater = inflater.cloneInContext(contextWrapper);

        final View root = inflater.inflate(R.layout.fragment_text_make, container, false);
        textPreviewView = (TextPreviewView) root.findViewById(R.id.text_preview);

        if (this.textMakingInfo != null) {
            textPreviewView.setTextMakingInfo(this.textMakingInfo);
        }

        TextMakingInfo textMakingInfo = textPreviewView.getTextMakingInfo();

        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = root.getRootView().getHeight() - root.getHeight();

                if (DEFAULT_HEIGHT_DIFF == -1) {
                    DEFAULT_HEIGHT_DIFF = heightDiff;
                } else if (heightDiff >= DEFAULT_HEIGHT_DIFF * 2) {
                    ViewGroup controlls = (ViewGroup) getView().findViewById(R.id.wrap_control);

                    ViewGroup.LayoutParams layoutParams = controlls.getLayoutParams();
                    layoutParams.height = heightDiff - DEFAULT_HEIGHT_DIFF;
                    controlls.setLayoutParams(layoutParams);

                    ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                }
            }
        });

        editText = (EditText) root.findViewById(R.id.input);
        editText.setText(textMakingInfo.getTextRaw());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textPreviewView.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        
        frameFont = root.findViewById(R.id.frame_font);
        frameOutline = root.findViewById(R.id.frame_outline);
        frameShadow = root.findViewById(R.id.frame_shadow);
        frameSkew = root.findViewById(R.id.frame_skew);

        buttonFont = root.findViewById(R.id.button_font);
        buttonOutline = root.findViewById(R.id.button_outline);
        buttonShadow = root.findViewById(R.id.button_shadow);
        buttonSkew = root.findViewById(R.id.button_skew);

        buttonFont.setOnClickListener(onEditTypeClickListener);
        buttonOutline.setOnClickListener(onEditTypeClickListener);
        buttonShadow.setOnClickListener(onEditTypeClickListener);
        buttonSkew.setOnClickListener(onEditTypeClickListener);

        ColorPickerView colorPickerView = new ColorPickerView(getContext());

        fontControl = new FontControl(textPreviewView, colorPickerView,  frameFont, fontPath);
        outlineControl = new OutlineControl(textPreviewView, colorPickerView, frameOutline);
        shadowControl = new ShadowControl(textPreviewView, colorPickerView, frameShadow);
        alignControl = new AlignControl(textPreviewView, colorPickerView, frameSkew);

        buttonPutDate = root.findViewById(R.id.put_date);
        buttonPutDate.setOnClickListener(onPutDateClickListener);

        onEditTypeClickListener.onClick(buttonFont);

        if (Build.VERSION.SDK_INT <= 19) {
            Util.fixBackgroundRepeat(root.findViewById(R.id.board));
        }
        return root;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private int requestedOrientationOld;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        requestedOrientationOld = ((Activity) context).getRequestedOrientation();
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        getActivity().setRequestedOrientation(requestedOrientationOld);
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private View.OnClickListener onEditTypeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (buttonFont == v) {
                frameFont.setVisibility(View.VISIBLE);
                frameOutline.setVisibility(View.GONE);
                frameShadow.setVisibility(View.GONE);
                frameSkew.setVisibility(View.GONE);

                buttonFont.setSelected(true);
                buttonOutline.setSelected(false);
                buttonShadow.setSelected(false);
                buttonSkew.setSelected(false);
            } else if (buttonOutline == v) {
                frameFont.setVisibility(View.GONE);
                frameOutline.setVisibility(View.VISIBLE);
                frameShadow.setVisibility(View.GONE);
                frameSkew.setVisibility(View.GONE);

                buttonFont.setSelected(false);
                buttonOutline.setSelected(true);
                buttonShadow.setSelected(false);
                buttonSkew.setSelected(false);
            } else if (buttonShadow == v) {
                frameFont.setVisibility(View.GONE);
                frameOutline.setVisibility(View.GONE);
                frameShadow.setVisibility(View.VISIBLE);
                frameSkew.setVisibility(View.GONE);

                buttonFont.setSelected(false);
                buttonOutline.setSelected(false);
                buttonShadow.setSelected(true);
                buttonSkew.setSelected(false);
            } else if (buttonSkew == v) {
                frameFont.setVisibility(View.GONE);
                frameOutline.setVisibility(View.GONE);
                frameShadow.setVisibility(View.GONE);
                frameSkew.setVisibility(View.VISIBLE);

                buttonFont.setSelected(false);
                buttonOutline.setSelected(false);
                buttonShadow.setSelected(false);
                buttonSkew.setSelected(true);
            }
        }
    };

    private View.OnClickListener onPutDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SimpleDateFormat simpleDateFormat;
            CharSequence[] formats = new CharSequence[TextMakingInfo.DATE_FORMAT.length];
            Date date = Calendar.getInstance().getTime();

            for (int i = 0; i < formats.length; i++) {
                simpleDateFormat = new SimpleDateFormat(TextMakingInfo.DATE_FORMAT[i]);
                formats[i] = simpleDateFormat.format(date);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setSingleChoiceItems(formats, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String dateReplace = TextMakingInfo.DATE_REPLACEMENTS[which];

                    int start = Math.max(editText.getSelectionStart(), 0);
                    int end = Math.max(editText.getSelectionEnd(), 0);
                    editText.getText().replace(Math.min(start, end), Math.max(start, end), dateReplace, 0, dateReplace.length());

                    dialog.dismiss();
                }
            });
            builder.show();
        }
    };

    public TextMakingInfo getTextMakingInfo() {
        return textPreviewView.getTextMakingInfo();
    }

}

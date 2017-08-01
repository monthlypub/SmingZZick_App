package com.monpub.sming.sticker;

import android.content.Context;
import android.view.View;

import com.monpub.sming.R;
import com.monpub.textmaker.TextMakingInfo;

/**
 * Created by small-lab on 2016-09-03.
 */
public  class TextStickerHolder extends StickerHolder<TextSticker> {
    public TextStickerStaticView textStickerHolderView;
    public TextStickerHolder(View itemView) {
        super(itemView);
        textStickerHolderView = (TextStickerStaticView) itemView.findViewById(R.id.text);
    }

    @Override
    public void setValue(TextSticker sticker) {
        Context context = textStickerHolderView.getContext();
        TextMakingInfo textMakingInfo = sticker.getTextMakeInfo(context);

        textStickerHolderView.setTextMakingInfo(textMakingInfo);
    }
}

package com.monpub.sming.youtube;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by small-lab on 2017-02-19.
 */

public class WebViewSwipeRefreshLayout extends SwipeRefreshLayout {
    private WebView webView;

    public WebViewSwipeRefreshLayout(Context context) {
        super(context);
    }

    public WebViewSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    @Override
    public boolean canChildScrollUp() {
        return webView.getScrollY() > 0;
    }
}

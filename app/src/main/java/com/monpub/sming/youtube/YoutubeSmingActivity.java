package com.monpub.sming.youtube;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.monpub.sming.MusicListenService;
import com.monpub.sming.R;
import com.monpub.sming.SmingApplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class YoutubeSmingActivity extends AppCompatActivity {
    private static final String PREF_LAST_PAGE = "pref_key_last_page";
    private static final String PREF_GUIDE_COMPLETE = "pref_key_guide_complete";

    private WebView webView;

    private DrawerLayout drawerLayout;
    private View menu;
    private View layoutLogin;
    private View loginView;

    private ProgressBar progressBar;
    private ProgressBar pageLoading;
    private TextView timeView;

    private View layoutProgress;

    private String rawJS;
    private String lastPlayTitle;

    private String currVideoId;
    private String playingVideoId;
    private String capturingVideoId;

    private long lastPlayTIme;

    static {
        if (Build.VERSION.SDK_INT >= 19) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_sming);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        menu = findViewById(R.id.menu);

        loginView = findViewById(R.id.login);
        layoutLogin = findViewById(R.id.layer_login);
        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("https://m.youtube.com/feed/account");
                drawerLayout.closeDrawer(menu);
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawer(menu);
            }
        });

        webView = (WebView) findViewById(R.id.webView);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        pageLoading = (ProgressBar) findViewById(R.id.page_loading);

        layoutProgress = findViewById(R.id.layout_progress);

        pageLoading.setMax(100);

        timeView = (TextView) findViewById(R.id.time);

        Uri uri;
        if (getIntent() != null && getIntent().getData() != null) {
            uri = getIntent().getData();
        } else {
//            String lastPageUrl  = SmingApplication.getPreference().getString(PREF_LAST_PAGE);
            String lastPageUrl = null;

            uri = Uri.parse(lastPageUrl == null || "about:blank".equalsIgnoreCase(lastPageUrl) ? "https://youtu.be" : lastPageUrl);
        }

//        initWebView(webView, "r9kJxGXUynI", "xRl8vLYSSP8", "YtgJJAxAwdQ", "A67_KZukgtk", "I_UgyfZinhw", "pG4seunapdQ", "8b-VlCAFMIQ", "weDFNBjeoJ8");
        initWebView(webView, uri);
        findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final WebViewSwipeRefreshLayout swipeRefreshLayout = (WebViewSwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setWebView(webView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        showGuide();
    }

    public void showGuide() {

        final View guide = findViewById(R.id.layout_guide);

        if (SmingApplication.getPreference().getBoolean(PREF_GUIDE_COMPLETE, false) == false) {
            guide.setVisibility(View.VISIBLE);

            webView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawerLayout.openDrawer(menu);
                    drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                        private boolean checkUserOpen = false;
                        @Override
                        public void onDrawerSlide(View drawerView, float slideOffset) {

                        }

                        @Override
                        public void onDrawerOpened(View drawerView) {
                            if (checkUserOpen == true) {
                                SmingApplication.getPreference().put(PREF_GUIDE_COMPLETE, true);
                                guide.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onDrawerClosed(View drawerView) {
                            checkUserOpen = true;
                        }

                        @Override
                        public void onDrawerStateChanged(int newState) {

                        }
                    });
                }
            }, 1000);
        } else {
            guide.setVisibility(View.GONE);
        }
    }

    private void initWebView(WebView webView, String... ids) {
        String listParam = TextUtils.join(",", ids);

        String url = "http://www.youtube.com/watch_videos?video_ids=" + listParam;

        initWebView(webView, Uri.parse(url));
    }

    private void initWebView(final WebView webView, Uri uri) {
        final WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSupportMultipleWindows(false);

        String url = uri.toString();

        webView.addJavascriptInterface(this, "app");
        webView.setWebViewClient(new WebViewClient() {
            private String lastPageUrl;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                Log.d("UUU_d", "[shouldOverrideUrlLoading][ url - " + url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                injection(webView);
                            }
                        },
                    100);

                SmingApplication.getPreference().put(PREF_LAST_PAGE, url);
                layoutProgress.setVisibility(View.GONE);

                findViewById(R.id.exit).setVisibility(View.VISIBLE);
                webView.removeCallbacks(exitHideRunnable);
                webView.postDelayed(exitHideRunnable, 3000);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                Log.d("UUU_d", "[onPageStarted][ url - " + url);

                lastPageUrl = url;
                layoutProgress.setVisibility(View.GONE);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);

//                Log.d("UUU_d", isReload + " ][-][ url - " + url);

                if ("about:blank".equalsIgnoreCase(url) == true) {
                    return;
                }

                layoutProgress.setVisibility(View.GONE);
                SmingApplication.getPreference().put(PREF_LAST_PAGE, url);

//                Log.d("UUU_d", isReload + " ][+][ url - " + url);

                if (url != null && url.startsWith("https://m.youtube.com/watch") == true) {
                    Uri uri = Uri.parse(url);
                    currVideoId = uri.getQueryParameter("v");
                } else {
                    currVideoId = null;
                }

                findViewById(R.id.exit).setVisibility(View.VISIBLE);
                webView.removeCallbacks(exitHideRunnable);
                webView.postDelayed(exitHideRunnable, 3000);

                if (TextUtils.isEmpty(playingVideoId) == false) {
                    captureFinish();
                }
                playingVideoId = null;

                checkLogin();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (newProgress == 100) {
                    pageLoading.setVisibility(View.INVISIBLE);
                } else {
                    pageLoading.setVisibility(View.VISIBLE);
                    pageLoading.setProgress(newProgress);
                }
            }
        });

        String js = loadJS();
        if (TextUtils.isEmpty(js) == false) {
            rawJS = js;
        }
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack() == true) {
            webView.goBack();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        captureFinish();
    }

    private void captureFinish() {
        Intent intent = new Intent(MusicListenService.ACTION_YOUTUBE_CAPTURE_FINISH);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        capturingVideoId = null;
        playingVideoId = null;
    }

    private void runOutTIme() {
        if (TextUtils.isEmpty(currVideoId) == true) {
            return;
        }

        if (TextUtils.isEmpty(currVideoId) == true && TextUtils.isEmpty(capturingVideoId) == false) {
            return;
        }

        if (currVideoId.equals(capturingVideoId) == false) {
            return;
        }

        Intent intent = new Intent(MusicListenService.ACTION_YOUTUBE_CAPTURE_ALMOST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @JavascriptInterface
    public void onProgress(final long curr, final long duration) {
        if (duration == 0) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutProgress.setVisibility(View.VISIBLE);

                progressBar.setMax((int) duration);
                progressBar.setProgress((int) curr);

                timeView.setText(String.format("%d:%02d / %d:%02d", curr / 60, curr % 60, duration / 60, duration % 60));
            }
        });

//        Log.d("TTT_d", String.format("%d / %d - %d", curr, duration, lastPlayTIme));
        if (lastPlayTIme <= duration - 3 && curr > duration - 3) {
            runOutTIme();
        }

        if (lastPlayTIme <= duration - 1 &&  curr > duration - 1) {
            captureFinish();
        }

        lastPlayTIme = curr;
    }

    @JavascriptInterface
    public void captureReady(String title) {
//        Log.d("TTT_d", "captureReady - " + title + " / " + currVideoId);
        if (TextUtils.isEmpty(currVideoId) == true) {
            return;
        }

        if (capturingVideoId != null && capturingVideoId.equals(currVideoId) == true) {
            return;
        }

        capturingVideoId = currVideoId;

        Intent intent = new Intent(MusicListenService.ACTION_YOUTUBE_CAPTURE_READY);
        intent.putExtra("title", title);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @JavascriptInterface
    public void onPlaying(String title, float duration) {
        if (TextUtils.isEmpty(title) == true || currVideoId == null || currVideoId.equals(playingVideoId) == true) {
            return;
        }

        Intent intent = new Intent(MusicListenService.ACTION_YOUTUBE_CAPTURE_START);
        intent.putExtra("title", title);

        Log.d("TTT_d", title);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        playingVideoId = currVideoId;
    }

    @JavascriptInterface
    public void onPaused(String title, float duration) {
    }


    @JavascriptInterface
    public void injectionFail() {
//        Log.d("TTT_d", "injectionFail");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        injection(webView);
                    }
                }, 1200);
            }
        });
    }

    @JavascriptInterface
    public boolean isLoggedIn() {
        return checkLogin();
    }

    private boolean checkLogin() {
        String cookie = CookieManager.getInstance().getCookie("https://m.youtube.com");
        final boolean loggedIn = cookie.contains("LOGIN_INFO") == true;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutLogin.setVisibility(loggedIn == true ? View.GONE : View.VISIBLE);
            }
        });

        return loggedIn;
    }


    private void injection(WebView webView) {
//        Log.d("TTT_d", "injection - " + currVideoId);
        if (TextUtils.isEmpty(currVideoId) == true) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            webView.evaluateJavascript(rawJS, null);
        } else {
            webView.loadUrl("javascript:" + rawJS);
        }
    }

    private String loadJS() {
        StringBuilder buf = new StringBuilder();
        try {
            InputStream is = getResources().getAssets().open("youtube_inject.js");
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;

            while ((str = br.readLine()) != null) {
                buf.append(str);
            }

            br.close();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return buf.toString();
    }

    Runnable exitHideRunnable = new Runnable() {
        @Override
        public void run() {
//            findViewById(R.id.exit).setVisibility(View.GONE);
        }
    };
}

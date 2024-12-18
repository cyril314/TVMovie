package com.movie.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.orhanobut.hawk.Hawk;
import com.movie.R;
import com.movie.api.ApiConfig;
import com.movie.base.BaseActivity;
import com.movie.bean.PraseBean;
import com.movie.ui.adapter.SettingPraseAdapter;
import com.movie.util.AdBlocker;
import com.movie.util.FastClickCheckUtil;
import com.movie.util.HawkConfig;
import com.movie.util.L;
import com.tv.leanback.VerticalGridView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析线路
 *
 * @author aim
 * @date :2021/1/6
 * @description:
 */
public class PraseActivity extends BaseActivity {

    private WebView mX5WebView;
    private VerticalGridView mGridView;
    private SettingPraseAdapter settingPraseAdapter;
    private String html;
    private String praseUrl;
    private int selected;
    private List<PraseBean> praseBeanList;
    private final Map<String, Boolean> loadedUrls = new HashMap<>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_prase;
    }

    @Override
    protected void init() {
        mGridView = findViewById(R.id.mGridView);
        mX5WebView = findViewById(R.id.mX5WebView);
        mX5WebView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        configWebView(mX5WebView);
        mX5WebView.setBackgroundColor(Color.BLACK);
        mX5WebView.setWebChromeClient(new WebChromeClient());
        mX5WebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                sslErrorHandler.proceed();
            }

            /**
             * 拦截请求
             */
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                boolean ad;
                if (!loadedUrls.containsKey(url)) {
                    ad = AdBlocker.isAd(url);
                    loadedUrls.put(url, ad);
                } else {
                    ad = loadedUrls.get(url);
                }
                return ad ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
            }

            /**
             * 加载解析源
             */
            @Override
            public void onLoadResource(WebView webView, String s) {
                super.onLoadResource(webView, s);
                try {
                    L.e("数据 = " + s);
                    if (s.contains(".m3u8?") || s.endsWith(".m3u8") || s.contains(".mp4?") || s.endsWith(".mp4")) {
                        String url = s;
                        if (s.contains("?url=")) {
                            String[] split = s.split("\\?url=");
                            for (String str : split) {
                                if (str.contains(".m3u8?") || str.endsWith(".m3u8") || str.contains(".mp4?") || str.endsWith(".mp4")) {
                                    url = str;
                                }
                            }
                        }
                        L.e("解析地址 = " + url);
                        Bundle bundle = new Bundle();
                        bundle.putString("playUrl", url);
                        jumpActivity(ProjectionPlayActivity.class, bundle);
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(mContext, "解析错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
        settingPraseAdapter = new SettingPraseAdapter();
        mGridView.setAdapter(settingPraseAdapter);
        mGridView.setNumColumns(6);
        settingPraseAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                if (selected != position) {
                    PraseBean praseBean = settingPraseAdapter.getData().get(position);
                    settingPraseAdapter.getData().get(selected).selected = false;
                    praseBean.selected = true;
                    settingPraseAdapter.notifyDataSetChanged();
                    selected = position;
                    Hawk.put(HawkConfig.DEFAULT_PRASE_ID, praseBean.getId());
                    mX5WebView.stopLoading();
                    praseUrl = praseBean.getPraseUrl();
                    mX5WebView.loadUrl(praseUrl + html);
                }
            }
        });
        settingPraseAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (view.getId() == R.id.tvPrase) {
                    if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).requestFocus();
                    }
                    if (selected != position) {
                        PraseBean praseBean = settingPraseAdapter.getData().get(position);
                        settingPraseAdapter.getData().get(selected).selected = false;
                        praseBean.selected = true;
                        settingPraseAdapter.notifyDataSetChanged();
                        selected = position;
                        Hawk.put(HawkConfig.DEFAULT_PRASE_ID, praseBean.getId());
                        mX5WebView.stopLoading();
                        praseUrl = praseBean.getPraseUrl();
                        mX5WebView.loadUrl(praseUrl + html);
                    }
                }
            }
        });
        initData();
    }

    private void initData() {
        praseBeanList = ApiConfig.get().getPraseBeanList();
        for (int i = 0; i < praseBeanList.size(); i++) {
            PraseBean praseBean = praseBeanList.get(i);
            if (praseBean.selected) {
                selected = i;
                praseUrl = praseBean.getPraseUrl();
                break;
            }
        }
        settingPraseAdapter.setNewData(praseBeanList);
        mGridView.setFocusPosition(selected);
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            html = bundle.getString("html");
            mX5WebView.loadUrl(praseUrl + html);
        }
//        html = "https://tv.sohu.com/v/MjAxNTA4MTcvbjQxOTA4MzkxNi5zaHRtbA==.html";
//        html = "https://m.iqiyi.com/v_irqqpk2p6c.html";
//        mX5WebView.loadUrl(praseUrl + html);
//        mX5WebView.loadUrl("https://www.kaniqiyi.com/v_irqqpk2p6c.html");
    }

    public void configWebView(WebView webView) {
        if (webView == null) {
            return;
        }
        /* 添加webView配置 */
        final WebSettings settings = webView.getSettings();
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        /* 添加webView配置 */
        //设置编码
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.getSettings().setUserAgentString(webView.getSettings().getUserAgentString());
    }

    @Override
    protected void onDestroy() {
        mX5WebView.stopLoading();
        mX5WebView.removeAllViews();
        mX5WebView.destroy();
        super.onDestroy();
    }
}
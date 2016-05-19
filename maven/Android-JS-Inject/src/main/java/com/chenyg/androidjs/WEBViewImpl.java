package com.chenyg.androidjs;


import android.content.Context;
import android.webkit.WebView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WEBViewImpl implements WEBView
{
    private static ExecutorService executorService;
    public static String urlForInjectJsError;

    public WebView webView;

    public WEBViewImpl(WebView webView)
    {
        this.webView = webView;
        if (executorService == null)
        {
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void loadUrl(String url)
    {
        webView.loadUrl(url);
    }

    @Override
    public Context getContext()
    {
        return webView.getContext();
    }

    @Override
    public void reloadForInjectJsFailed()
    {
        webView.reload();
    }

    @Override
    public void loadInjectJsFailedPage()
    {
        if (urlForInjectJsError != null)
        {
            loadUrl(urlForInjectJsError);
        }
    }

    static WEBView toWEBView(WebView webView)
    {
        if (webView instanceof WEBView)
        {
            return (WEBView) webView;
        } else
        {
            return new WEBViewImpl(webView);
        }
    }

    @Override
    public String getCurrentUrl()
    {
        return webView.getUrl();
    }

    @Override
    public ExecutorService getExecutor()
    {
        return executorService;
    }

    @Override
    public boolean isMainThread()
    {
        return getContext().getMainLooper().getThread().equals(Thread.currentThread());
    }
}

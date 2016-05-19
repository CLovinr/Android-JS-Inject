package com.chenyg.androidjs;

import android.content.Context;

import java.util.concurrent.ExecutorService;

/**
 * Created by 宇宙之灵 on 2016/5/16.
 */
public interface WEBView
{
    void loadUrl(String url);

    Context getContext();

    /**
     * 当注入失败时，尝试刷新重试。
     */
    void reloadForInjectJsFailed();

    /**
     * 最终注入失败后，会调用此函数，可以载入js注入失败的页面。
     */
    void loadInjectJsFailedPage();

    /**
     * 是否处于主线程
     *
     * @return
     */
    boolean isMainThread();

    String getCurrentUrl();

    ExecutorService getExecutor();
}

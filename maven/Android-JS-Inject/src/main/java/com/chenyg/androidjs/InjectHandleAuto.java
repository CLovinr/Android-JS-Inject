package com.chenyg.androidjs;

import android.util.Log;

/**
 * Created by 宇宙之灵 on 2016/5/28.
 */
 class InjectHandleAuto extends InjectHandleSelf
{

    private String injectJsStr;

    public InjectHandleAuto(JsCallJava jsCallJava)
    {
        super(jsCallJava);
        try
        {
            injectJsStr = "javascript:" + WEBUtil
                    .loadPackageJs(getClass(), "/safe-js/inject.js", jsCallJava.willPrintDebugInfo());
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Result dealCmd(WEBView webView, String cmd)
    {
        InjectState state = injectState(webView);
        if (INJECT_TEST.equals(cmd) && state != InjectState.OK && state != InjectState.FAILED)
        {
            if (jsCallJava.willPrintDebugInfo())
            {
                Log.w(getClass().getName(), cmd);
            }
            injectState(webView, InjectState.TEST);
            injectJs(webView);
            return Result.OK;
        } else
        {
            return super.dealCmd(webView, cmd);
        }
    }

    @Override
    public void onProgressChanged(WEBView webView, int newProgress)
    {
        super.onProgressChanged(webView, newProgress);
        InjectState state = injectState(webView);
        if (state != InjectState.OK && state != InjectState.FAILED)
        {
            if (jsCallJava.willPrintDebugInfo())
            {
                Log.w(getClass().getName(), "load:inject.js,progress=" + newProgress);
            }
            if (state == null)
            {
                injectJs(webView);
            }
            webView.loadUrl(injectJsStr);
        }
    }
}

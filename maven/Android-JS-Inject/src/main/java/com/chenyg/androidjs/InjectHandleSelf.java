package com.chenyg.androidjs;

import android.util.Log;

/**
 * 用于手动注入
 * Created by 宇宙之灵 on 2016/5/28.
 */
class InjectHandleSelf extends InjectHandleImpl
{
    public InjectHandleSelf(JsCallJava jsCallJava)
    {
        super(jsCallJava);
    }

    @Override
    public Result dealCmd(WEBView webView, String cmd)
    {
        if (jsCallJava.willPrintDebugInfo())
        {
            Log.w(getClass().getName(), cmd);
        }
        Result result = Result.OK;
        switch (cmd)
        {
            case INJECT_OK:
            {
                resetCurrentInject(webView);
                injectState(webView, InjectState.OK);
            }
            break;

            case INJECT_FAILED:
            {
                if (injectCount(webView) <= getMaxTryInjectCount())
                {
                    webView.reloadForInjectJsFailed();
                } else
                {
                    clear();
                    webView.loadInjectJsFailedPage();
                    injectState(webView, InjectState.FAILED);
                }
            }
            break;

            case INJECT_JS:
            {
                InjectState state = injectState(webView);
                if (state == null || state == InjectState.TEST)
                {
                    injectJs(webView);
                }
            }
            break;
            case INJECT_TEST:
                injectState(webView, InjectState.TEST);
                break;
            default:
                result = Result.FAILED;
        }


        return result;
    }

    @Override
    public void onProgressChanged(WEBView webView, int newProgress)
    {

    }
}

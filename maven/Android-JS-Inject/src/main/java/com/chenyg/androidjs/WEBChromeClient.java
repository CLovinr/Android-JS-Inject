package com.chenyg.androidjs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import com.chenyg.androidjs.my.MyJsPromptResult;
import com.chenyg.androidjs.my.MyJsResult;

import java.util.Arrays;

/**
 * 其中的onMy开头的方法是为了整合CrossWalk的XWalkView而准备的，目前已经有实现，不久有时间后整理出来。
 * Created by 宇宙之灵 on 2016/5/17.
 */
public class WEBChromeClient extends WebChromeClient
{
    private JsCallJava jsCallJava;
    private boolean isInjectedJS;
    private boolean autoInjectJS;

    private static final int MAX_TRY_INJECT_COUNT = 5;
    private int currentInjectCount = 0;
    private String lastUrl;
    private ProgressBar progressBar;
    private String alertTitle = "alert";

    private static final String[] FOR_INJECT;

    static
    {
        FOR_INJECT = new String[]{
                "<inject-failed>",
                "<inject-ok>",
                "<inject-js>",
                "<inject-test>"
        };
        Arrays.sort(FOR_INJECT);
    }


    void resetCurrentInject()
    {
        currentInjectCount = 0;
    }


    public WEBChromeClient(boolean autoInjectJS, boolean willPrintDebugInfo,
            JsCallJava.InjectObj... injectObjs)
    {
        this(autoInjectJS, new JsCallJava(willPrintDebugInfo, injectObjs));
    }


    public WEBChromeClient(boolean autoInjectJS, JsCallJava jsCallJava)
    {
        this.autoInjectJS = autoInjectJS;
        this.jsCallJava = jsCallJava;
    }

    public void setProgressBar(ProgressBar progressBar)
    {
        progressBar.setMax(100);
        this.progressBar = progressBar;
    }

    public void setAlertTitle(String alertTitle)
    {
        this.alertTitle = alertTitle;
    }

    private void injectJS(WEBView view, boolean isAuto)
    {
        if (!isInjectedJS || !isAuto)
        {
            if (jsCallJava.willPrintDebugInfo())
            {
                Log.w(getClass().getSimpleName(), "start inject js...");
            }
            view.loadUrl(jsCallJava.getPreloadInterfaceJS());
            isInjectedJS = true;
        }
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result)
    {
        return onMyJsAlert(WEBViewImpl.toWEBView(view), url, message, new MyJsResult()
        {

            @Override
            public void cancel()
            {
                result.cancel();
            }

            @Override
            public void confirm()
            {
                result.confirm();
            }


        });
    }

    /**
     * 判断alert的内容是否是用于注入的特殊内容。
     *
     * @param alertStr
     * @return
     */
    protected boolean isForInject(String alertStr)
    {
        return Arrays.binarySearch(FOR_INJECT, alertStr) >= 0;
    }

    public boolean onMyJsAlert(WEBView webView, String url, String message, MyJsResult result)
    {

        if ("<inject-failed>".equals(message))
        {
            if (url.equals(lastUrl))
            {
                currentInjectCount++;
            } else
            {
                currentInjectCount = 0;
                lastUrl = url;
            }
            if (currentInjectCount <= MAX_TRY_INJECT_COUNT)
            {
                webView.reloadForInjectJsFailed();
                result.confirm();
                return true;
            }

        } else if ("<inject-ok>".equals(message))
        {
            resetCurrentInject();
            result.confirm();
            return true;
        } else if ("<inject-test>".equals(message))
        {
            result.confirm();
            return true;
        }

        if ("<inject-failed>".equals(message))
        {
            resetCurrentInject();
            result.confirm();
            webView.loadInjectJsFailedPage();
            return true;
        }

        if (!autoInjectJS && "<inject-js>".equals(message))
        {
            injectJS(webView, false);
            result.confirm();
            return true;
        } else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
            builder.setTitle(alertTitle);
            builder.setMessage(message);
            builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });

            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
            result.confirm();
            return true;
        }
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress)
    {
        onMyProgressChanged(WEBViewImpl.toWEBView(view), newProgress);
    }

    public void onMyProgressChanged(WEBView webView, int newProgress)
    {
        //为什么要在这里注入JS
        //1 OnPageStarted中注入有可能全局注入不成功，导致页面脚本上所有接口任何时候都不可用
        //2 OnPageFinished中注入，虽然最后都会全局注入成功，但是完成时间有可能太晚，当页面在初始化调用接口函数时会等待时间过长
        //3 在进度变化时注入，刚好可以在上面两个问题中得到一个折中处理
        //为什么是进度大于25%才进行注入，因为从测试看来只有进度大于这个数字页面才真正得到框架刷新加载，保证100%注入成功

        if (newProgress <= 25)
        {
            isInjectedJS = false;
        } else if (!isInjectedJS)
        {
            if (autoInjectJS)
            {
                injectJS(webView, true);
                // Log.d(TAG, " inject js interface completely on progress " + newProgress);
            }
        }

        if (progressBar != null)
        {
            if (newProgress == 100)
            {
                progressBar.setVisibility(View.GONE);
            } else
            {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
            }
        }
    }


    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
            final JsPromptResult jsPromptResult)
    {
        return onMyJsPrompt(WEBViewImpl.toWEBView(view), url, message, defaultValue, new MyJsPromptResult()
        {
            @Override
            public void confirm(String result)
            {
                jsPromptResult.confirm(result);
            }

            @Override
            public void cancel()
            {
                jsPromptResult.cancel();
            }

            @Override
            public void confirm()
            {
                jsPromptResult.confirm();
            }
        });
    }


    public boolean onMyJsPrompt(WEBView webView, String url, String message, String defaultValue,
            MyJsPromptResult result)
    {
        result.confirm(jsCallJava.call(webView, message));
        return true;
    }

}

package com.chenyg.androidjs;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
 */
public class WEBChromeClient extends WebChromeClient
{
    private JsCallJava _jsCallJava;
    private InjectHandle injectHandle;

    protected ProgressBar progressBar;
    private String alertTitle = "alert";


    public WEBChromeClient(InjectType injectType, boolean willPrintDebugInfo,
            JsCallJava.InjectObj... injectObjs)
    {
        this(injectType, new JsCallJava(willPrintDebugInfo, injectObjs));
    }


    public WEBChromeClient(InjectType injectType, JsCallJava jsCallJava)
    {
        this._jsCallJava = jsCallJava;
        switch (injectType)
        {
            case Auto:
                injectHandle = new InjectHandleAuto(jsCallJava);
                break;
            case Self:
                injectHandle = new InjectHandleSelf(jsCallJava);
                break;
        }
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
    protected boolean isInjectCmd(String alertStr)
    {
        return injectHandle.isInjectCmd(alertStr);
    }

    public boolean onMyJsAlert(WEBView webView, String url, String message, MyJsResult result)
    {
        InjectHandle.Result res = null;
        if (injectHandle.isInjectCmd(message))
        {
            res = injectHandle.dealCmd(webView, message);
        }
        if (res == InjectHandle.Result.OK)
        {
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
        injectHandle.onProgressChanged(webView, newProgress);
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
        result.confirm(_jsCallJava.call(webView, message));
        return true;
    }

}

package com.chenyg.androidjs;

import android.webkit.WebSettings;
import android.webkit.WebView;

import java.lang.reflect.Method;

/**
 * Created by 宇宙之灵 on 2016/5/17.
 */
public class WEBUtil
{
    public static void defaultSettings(WebView webView)
    {
        WebSettings settings = webView.getSettings();


        //mWebView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setFocusable(true);
        settings.setJavaScriptEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setAppCacheEnabled(true);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);

        invoke(settings, "setAllowContentAccess", new Class[]{Boolean.class}, new Object[]{true});

        settings.setSupportMultipleWindows(false);
        settings.setLoadsImagesAutomatically(true);
    }


    public static Object invoke(Object obj, String method, Class<?>[] classes, Object[] vals)
    {
        try
        {
            Method m = obj.getClass().getDeclaredMethod(method, classes);
            return m.invoke(obj, vals);
        } catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
    }
}

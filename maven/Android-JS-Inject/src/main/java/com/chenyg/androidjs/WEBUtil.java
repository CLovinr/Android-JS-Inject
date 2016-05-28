package com.chenyg.androidjs;

import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;
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

    static String loadPackageJs(Class<?> c, String path, boolean isDebug) throws Exception
    {
        String content = read(c, path);
        //除去注释,否则在某些平台上会出现错误。
        content = content.replaceAll("//[^\\n]*", "");
        content = content.replaceAll("/\\*[^/]*\\*/", "");
        if (!isDebug)
        {
            content = content.replace("\n", "");
        }
        return content;
    }

    private static String read(Class<?> c, String path) throws Exception
    {
        InputStream in = null;
        try
        {
            in = c.getResourceAsStream(path);
            byte[] bs = new byte[in.available()];
            in.read(bs);
            return new String(bs, "utf-8");
        } catch (Exception e)
        {
            throw e;
        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}

package cn.pedant.SafeWebViewBridge.sample;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.chenyg.androidjs.JsCallJava;
import com.chenyg.androidjs.WEBChromeClient;

public class WebActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        WebView wv = new WebView(this);
        setContentView(wv);
        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        wv.setWebChromeClient(new CustomChromeClient());
        wv.loadUrl("file:///android_asset/default/test.html");
    }


    public class CustomChromeClient extends WEBChromeClient
    {

        public CustomChromeClient()
        {
            super(false, true, new JsCallJava.InjectObj("HostApp", HostJsScope.class));
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result)
        {
            // to do your work
            // ...
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress)
        {
            super.onProgressChanged(view, newProgress);
            // to do your work
            // ...
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result)
        {
            // to do your work
            // ...
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }
    }
}

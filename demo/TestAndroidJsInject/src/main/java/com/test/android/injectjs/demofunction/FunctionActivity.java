package com.test.android.injectjs.demofunction;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.webkit.WebView;
import android.widget.Toast;
import com.chenyg.androidjs.*;

/**
 * 测试java与js之间的函数的传递。
 * Created by 宇宙之灵 on 2016/5/28.
 */
public class FunctionActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        WebView wv = new WebView(this);
        setContentView(wv);
        WEBUtil.defaultSettings(wv);
        Java2JsCallback java2JsCallback = new Java2JsCallback(new WEBViewImpl(wv))
        {
            @Callback
            public void callback(String content)
            {
                Toast.makeText(FunctionActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        };
        java2JsCallback.setPermanent(true);

        wv.setWebChromeClient(
                new WEBChromeClient(false, true, new JsCallJava.InjectObj("demo.fun", new Js1(java2JsCallback))));
        wv.loadUrl("file:///android_asset/test-fun/test.html");
    }
}

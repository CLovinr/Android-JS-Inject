package com.test.android.injectjs.demofunction;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;
import com.chenyg.androidjs.*;

/**
 * 测试java与js之间的函数的传递。
 */
public class FunctionActivity extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        WebView wv = new WebView(this);
        setContentView(wv);
        WEBUtil.defaultSettings(wv);//会启用js


        Java2JsCallback java2JsCallback = new Java2JsCallback(new WEBViewImpl(wv))
        {
            @Callback//回调接口
            public void callback(String content)
            {
                Toast.makeText(FunctionActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        };
        java2JsCallback.setPermanent(true);
        String injectType = getIntent().getStringExtra("injectType");
        InjectType type = InjectType.valueOf(injectType);//自动：InjectType.Auto；手动：InjectType.Self

        wv.setWebChromeClient(
                new WEBChromeClient(type, true,
                        new JsCallJava.InjectObj("demo.fun", new Js1(java2JsCallback))));
        wv.loadUrl("file:///android_asset/test-fun/test" + (injectType == null ? "" : injectType) + ".html");
    }
}

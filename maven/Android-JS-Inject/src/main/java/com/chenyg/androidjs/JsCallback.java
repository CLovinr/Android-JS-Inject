/**
 * Summary: 异步回调页面JS函数管理对象
 * Version 1.0
 * Date: 13-11-26
 * Time: 下午7:55
 * Copyright: Copyright (c) 2013
 */

package com.chenyg.androidjs;


import java.lang.ref.WeakReference;

/**
 * 用于java端调用js。
 */
public class JsCallback
{

    private static final String CALLBACK_JS_FORMAT = "javascript:%s.callback(%s, %d %s);";
    private static final String DESTROY_JS_FORMAT = "javascript:%s.destroy(%s);";
    protected String id;
    private boolean couldGoOn;
    protected WeakReference<WEBView> webViewRef;
    private boolean isPermanent = false;
    private String namespace;


    public JsCallback(WEBView view, String namespace, String id)
    {
        couldGoOn = true;
        webViewRef = new WeakReference<>(view);
        this.namespace = namespace;
        this.id = id;
    }

    public boolean isPermanent()
    {
        return isPermanent;
    }

    public void setPermanent(boolean permanent)
    {
        isPermanent = permanent;
    }

    public void destroy() throws JsCallbackException
    {
        if (webViewRef.get() == null)
        {
            throw new JsCallbackException("the WebView related to the JsCallback has been recycled");
        }

        String execJs = String.format(DESTROY_JS_FORMAT, namespace, id);
        //Log.d("JsCallBack", execJs);
        webViewRef.get().loadUrl(execJs);
    }

    public void apply(Object... args) throws JsCallbackException
    {
        if (webViewRef.get() == null)
        {
            throw new JsCallbackException("the WebView related to the JsCallback has been recycled");
        }
        if (!couldGoOn)
        {
            throw new JsCallbackException("the JsCallback isn't permanent,cannot be called more than once");
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args)
        {
            sb.append(",");

            if(arg==null){
                sb.append("null");
            }else if(arg instanceof String){
                sb.append('"').append(arg).append('"');
            }else if(arg instanceof Java2JsCallback){
                sb.append('"').append(arg.toString()).append('"');
            }else{
                sb.append(String.valueOf(arg));
            }
        }
        String execJs = String.format(CALLBACK_JS_FORMAT, namespace, id, isPermanent() ? 1 : 0, sb.toString());
        //Log.d("JsCallBack", execJs);
        webViewRef.get().loadUrl(execJs);
        couldGoOn = isPermanent();
    }


    public static class JsCallbackException extends Exception
    {
        public JsCallbackException(String msg)
        {
            super(msg);
        }
    }

    public static void tryApply(JsCallback jsCallback, Object... args)
    {
        if (jsCallback != null)
        {
            try
            {
                jsCallback.apply(args);
            } catch (JsCallbackException e)
            {
                e.printStackTrace();
            }
        }
    }
}

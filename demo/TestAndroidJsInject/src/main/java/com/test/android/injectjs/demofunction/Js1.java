package com.test.android.injectjs.demofunction;

import android.widget.Toast;
import com.chenyg.androidjs.Java2JsCallback;
import com.chenyg.androidjs.JsCallback;
import com.chenyg.androidjs.WEBView;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by 宇宙之灵 on 2016/5/28.
 */
public class Js1
{

    private Java2JsCallback java2JsCallback;

    public void toast(WEBView webView, String message)
    {
        Toast.makeText(webView.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param java2JsCallback 用于动态传递到js端的java函数对象。
     */
    public Js1(Java2JsCallback java2JsCallback)
    {
        this.java2JsCallback = java2JsCallback;
    }

    /**
     * js函数直接作为java函数参数。
     *
     * @param view
     * @param callback
     */
    public void jsFun(WEBView view, JsCallback callback)
    {
        try
        {
            callback.apply();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * js函数作为json对象的内部变量。
     *
     * @param view
     * @param jsonObject
     */
    public void jsFun(WEBView view, JSONObject jsonObject)
    {
        try
        {
            JsCallback callback = (JsCallback) jsonObject.get("callback");
            callback.apply();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * js函数作为json数组的元素。
     *
     * @param view
     * @param jsonArray
     */
    public void jsFunArray(WEBView view, JSONArray jsonArray)
    {
        try
        {
            JsCallback callback = (JsCallback)jsonArray.get(0);
            callback.apply();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * js函数作为json对象的json数组的元素。
     *
     * @param view
     * @param jsonObject
     */
    public void jsFunArray(WEBView view, JSONObject jsonObject)
    {
        try
        {
            JsCallback callback = (JsCallback) jsonObject.getJSONArray("array").get(0);
            callback.apply();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 直接传递java函数对象。
     *
     * @param view
     * @param callback
     */
    public void sendJavaFun(WEBView view, JsCallback callback)
    {
        try
        {
            callback.apply(java2JsCallback);
        } catch (JsCallback.JsCallbackException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * java函数对象作为json对象的一个变量。
     *
     * @param view
     * @param callback
     */
    public void sendJavaFunInJson(WEBView view, JsCallback callback)
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("callback", java2JsCallback);
            callback.apply(jsonObject);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

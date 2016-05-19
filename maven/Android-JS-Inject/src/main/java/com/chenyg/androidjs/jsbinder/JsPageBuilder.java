package com.chenyg.androidjs.jsbinder;

import com.chenyg.androidjs.JsCallback;
import com.chenyg.androidjs.WEBView;
import com.chenyg.uibinder.*;
import com.chenyg.uibinder.js.JsBinder;
import com.chenyg.uibinder.js.JsPage;
import com.chenyg.wporter.WebPorter;
import com.chenyg.wporter.base.SimpleAppValues;
import com.chenyg.wporter.security.KeyUtil;
import com.chenyg.wporter.util.PackageUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by 宇宙之灵 on 2016/4/28.
 */
public class JsPageBuilder
{

    private static final long TIME_OUT = 300, SLEEP = 10;
    static Map<String, CallResult> forGetMap = Collections.synchronizedMap(new HashMap<String, CallResult>());


    public static JsPage build(final WEBView webView, final ExecutorService executorService,
            JSONObject data) throws JSONException,
            ClassNotFoundException
    {
        JsCallback setCall = (JsCallback) data.opt("setCall");
        final JsCallback getCall = (JsCallback) data.opt("getCall");
        setCall.setPermanent(true);
        getCall.setPermanent(true);
        JSONArray ids = data.optJSONArray("ids");
        Map<String, JsBinder> map = new HashMap<>();
        for (int i = 0; i < ids.length(); i++)
        {
            JSONObject jsonObject = ids.getJSONObject(i);
            String id = jsonObject.getString("id");
            JsBinder binder = new JsBridgeImpl(webView, id, setCall, getCall, executorService);
            map.put(id, binder);
        }

        Class<?> c = PackageUtil.newClass(data.optString("prefixClass"), null);

        Prefix prefix = Prefix.buildPrefix((Class<? extends WebPorter>) c);


        UIAttrGetter uiAttrGetter = new UIAttrGetter()
        {

            @Override
            public boolean supportSync()
            {
                return true;
            }

            @Override
            public void asynGetAttrs(Listener listener, Binder[] binders, AttrEnum... types)
            {
                String[] names = new String[binders.length];
                Object[] values = new Object[binders.length];
                JSONArray array = new JSONArray();
                for (int i = 0; i < names.length; i++)
                {
                    JSONObject jsonObject = new JSONObject();
                    array.put(jsonObject);
                    if (binders[i] == null)
                    {
                        continue;
                    }
                    JsBridgeImpl jsBridgeImpl = (JsBridgeImpl) binders[i];
                    try
                    {
                        jsonObject.put("id", jsBridgeImpl.getCallbackId());
                        jsonObject.put("name", (types.length == 1 ? types[0] : types[i]).name());
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    names[i] = binders[i].getIdDealResult().getVarName();
                }

                JSONObject returnData = getAttrs(webView, getCall, array, executorService);

                if (returnData != null)
                {
                    try
                    {
                        JSONArray valuesArr = returnData.getJSONArray("values");
                        for (int i = 0; i < valuesArr.length(); i++)
                        {
                            values[i] = valuesArr.get(i);
                        }
                        listener.onGet(new SimpleAppValues(names).values(values));
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        };


        JsPage jsPage = new JsPage(map, BaseUI.getBaseUI(), uiAttrGetter, prefix, webView.getCurrentUrl());

        return jsPage;
    }

    static JSONObject getAttr(WEBView webView, JsCallback getCall, String id, String attrName,
            ExecutorService executorService)
    {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonArray.put(jsonObject);
        try
        {
            jsonObject.put("id", id);
            jsonObject.put("name", attrName);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return getAttrs(webView, getCall, jsonArray, executorService);
    }

    static JSONObject getAttrs(WEBView webView, JsCallback getCall, Object args,
            ExecutorService executorService)
    {
        CallResult callResult = new CallResult(SLEEP, TIME_OUT);
        String cid = KeyUtil.secureRandomKey(20);
        try
        {
            JsPageBuilder.forGetMap.put(cid, callResult);
            getCall.apply(cid, args);
        } catch (JsCallback.JsCallbackException e)
        {
            e.printStackTrace();
        }

        JSONObject returnData = null;
        if (webView.isMainThread())
        {
            try
            {
                Future<JSONObject> future = executorService.submit(callResult);
                returnData = future.get(TIME_OUT, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e)
            {
                try
                {
                    throw new GetAttrException.GetAttrTimeoutException(e);
                } catch (GetAttrException.GetAttrTimeoutException e1)
                {
                    e1.printStackTrace();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } else
        {
            try
            {
                returnData = callResult.call();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        JsPageBuilder.forGetMap.remove(cid);
        return returnData;
    }

    public static void onGet(String cid, JSONObject returnData)
    {

        CallResult callResult = forGetMap.get(cid);
        if (callResult != null)
        {
            callResult.returnData = returnData;
        }
    }
}

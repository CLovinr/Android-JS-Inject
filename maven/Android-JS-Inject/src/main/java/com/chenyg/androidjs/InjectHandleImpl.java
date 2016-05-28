package com.chenyg.androidjs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 宇宙之灵 on 2016/5/28.
 */
abstract class InjectHandleImpl implements InjectHandle
{

    protected static final String INJECT_OK = "<inject-ok>";
    protected static final String INJECT_FAILED = "<inject-failed>";
    protected static final String INJECT_JS = "<inject-js>";
    protected static final String INJECT_TEST = "<inject-test>";

    private static class Temp
    {
        int injectCount = 0;
        int totalCount = 0;
        InjectState state = null;
    }

    enum InjectState
    {
        OK,
        FAILED,
        TEST
    }

    private static final String[] FOR_INJECT;

    static
    {
        FOR_INJECT = new String[]{
                INJECT_OK,
                INJECT_FAILED,
                INJECT_JS,
                INJECT_TEST
        };
        Arrays.sort(FOR_INJECT);
    }

    private int maxTryInjectCount;
    static final int JS_MAX_COUNT = 10;

    protected JsCallJava jsCallJava;
    private Map<String, Temp> tempMap = Collections.synchronizedMap(new HashMap<String, Temp>());

    public InjectHandleImpl(JsCallJava jsCallJava)
    {
        this.maxTryInjectCount = 1;
        this.jsCallJava = jsCallJava;
    }

    @Override
    public boolean isInjectCmd(String str)
    {
        return Arrays.binarySearch(FOR_INJECT, str) >= 0;
    }




    protected void injectJs(WEBView webView)
    {
        if (getTemp(webView).totalCount++ < maxTryInjectCount * JS_MAX_COUNT)
        {
            webView.loadUrl(jsCallJava.getPreloadInterfaceJS());
        }else{
            tempMap.clear();
        }
    }


    private Temp getTemp(WEBView webView)
    {
        Temp temp = tempMap.get(webView.getCurrentUrl());
        if (temp == null)
        {
            temp = new Temp();
            tempMap.put(webView.getCurrentUrl(), temp);
        }
        return temp;
    }

    protected InjectState injectState(WEBView webView)
    {
        return getTemp(webView).state;
    }

    protected void injectState(WEBView webView, InjectState state)
    {
        getTemp(webView).state = state;
    }

    protected void resetCurrentInject(WEBView webView)
    {
        Temp temp = getTemp(webView);
        temp.state = null;
        temp.totalCount = 0;
        temp.injectCount = 0;
    }

    protected void clear()
    {
        tempMap.clear();
    }

    protected int injectCount(WEBView webView)
    {
        return getTemp(webView).injectCount++;
    }


    public int getMaxTryInjectCount()
    {
        return maxTryInjectCount;
    }
}

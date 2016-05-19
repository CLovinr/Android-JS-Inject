package com.chenyg.androidjs;


import java.util.HashMap;
import java.util.UUID;

/**
 * 用于向js端动态传递java函数。
 */
public abstract class Java2JsCallback extends JsCallback
{
    static final String JAVA_CALLBACK = "JavaCallback:";

    private static HashMap<String, Java2JsCallback> javaCallbackHashMap = new HashMap<>();


    static void remove(String callbackId)
    {
        synchronized (javaCallbackHashMap)
        {
            javaCallbackHashMap.remove(callbackId);
        }
    }

    static Java2JsCallback get(String callbackId)
    {
        synchronized (javaCallbackHashMap)
        {
            return javaCallbackHashMap.get(callbackId);
        }
    }

    public Java2JsCallback(WEBView view)
    {
        super(view, "", UUID.randomUUID().toString());
        synchronized (javaCallbackHashMap)
        {
            javaCallbackHashMap.put(id, this);
        }
    }

    @Override
    public void destroy() throws JsCallbackException
    {
        remove(id);
    }

    @Override
    public String toString()
    {
        return "\"" + JAVA_CALLBACK + id + "\"";
    }

    @Override
    public void apply(Object... args) throws JsCallbackException
    {
        throw new JsCallbackException("it is a " + JAVA_CALLBACK);
    }

    /**
     * js端调用时执行该函数。
     *
     * @return
     */
    protected Object callback(Object... values)
    {
        if (!isPermanent())
        {
            try
            {
                destroy();
            } catch (JsCallbackException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }


}

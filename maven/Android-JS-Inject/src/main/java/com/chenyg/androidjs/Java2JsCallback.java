package com.chenyg.androidjs;


import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.UUID;

/**
 * 用于向js端动态传递java函数。使用注解{@linkplain Callback}标记要注入的函数.
 */
public abstract class Java2JsCallback extends JsCallback
{

    /**
     * 用于标记回调函数(public、非static)。
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Documented
    public @interface Callback
    {

    }

    static final String JAVA_CALLBACK = "JavaCallback:";

    private static HashMap<String, Java2JsCallback> javaCallbackHashMap = new HashMap<>();
    private HashMap<String, JsCallJava.MethodClass> methodsMap;

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
        methodsMap = new HashMap<>();

        try
        {
            Method[] methods = getClass().getMethods();
            for (Method method : methods)
            {
                String sign;

                if (!method.isAnnotationPresent(Callback.class) || method
                        .getModifiers() != Modifier.PUBLIC || Modifier
                        .isStatic(method.getModifiers()) || (sign = JsCallJava.genJavaMethodSign(
                        method, false, false)) == null)
                {
                    continue;
                }
                methodsMap.put(JAVA_CALLBACK + sign, new JsCallJava.MethodClass(method, this));
            }
        } catch (Exception e)
        {
            throw new RuntimeException(e);
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
        return JAVA_CALLBACK + id;
    }

    @Override
    public void apply(Object... args) throws JsCallbackException
    {
        throw new JsCallbackException("it is a " + JAVA_CALLBACK);
    }

    JsCallJava.MethodClass getMethodClass(String sign)
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
        return methodsMap.get(sign);
    }


}

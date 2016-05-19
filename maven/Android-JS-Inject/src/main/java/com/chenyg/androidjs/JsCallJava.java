package com.chenyg.androidjs;


import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class JsCallJava
{
    private final static String TAG = "JsCallJava";
    private final static String RETURN_RESULT_FORMAT = "{\"code\": %d, \"result\": %s}";
    private final static String JSON_FUNCTION_STARTS = "[function]=";
    private HashMap<String, MethodClass> methodsMap;

    private String preloadInterfaceJS;
    private Gson gson;
    private boolean willPrintDebugInfo, searchMoreForObjFun;



    private static class MethodClass
    {
        Method method;
        Object object;

        public MethodClass(Method method, Object object)
        {
            this.method = method;
            this.object = object;
        }
    }


    public static class InjectObj
    {
        String namespace;

        List<Class<?>> interfaceClasses = new ArrayList<>();
        List<Object> interfaceObjects = new ArrayList<>();

        /**
         * @param namespace        命名空间
         * @param interfaceClasses 必须含有无参构造函数.
         */
        public InjectObj(String namespace, Class<?>... interfaceClasses)
        {
            if (TextUtils.isEmpty(namespace))
            {
                throw new RuntimeException("namespace can not be null!");
            }
            this.namespace = namespace;
            this.add(interfaceClasses);
        }

        /**
         * @param namespace        命名空间
         * @param interfaceObjects
         */
        public InjectObj(String namespace, Object... interfaceObjects)
        {
            if (TextUtils.isEmpty(namespace))
            {
                throw new RuntimeException("namespace can not be null!");
            }
            this.namespace = namespace;
            this.add(interfaceObjects);
        }

        public InjectObj add(Object... interfaceObjects)
        {
            for (Object object : interfaceObjects)
            {
                this.interfaceObjects.add(object);
            }

            return this;
        }

        public InjectObj add(Class<?>... interfaceClasses)
        {
            for (Class<?> c : interfaceClasses)
            {
                this.interfaceClasses.add(c);
            }
            return this;
        }
    }


    public JsCallJava(boolean willPrintDebugInfo, boolean searchMoreForObjFun, InjectObj... injectObjs)
    {
        try
        {
            this.searchMoreForObjFun = searchMoreForObjFun;
            this.willPrintDebugInfo = willPrintDebugInfo;
            methodsMap = new HashMap<>();
            StringBuilder sbuilder = new StringBuilder("javascript:");

            String tml = readTemplate();

            //除去注释,否则在某些平台上会出现错误。

            tml = tml.replaceAll("//[^\\n]*", "");
            tml = tml.replaceAll("/\\*[^/]*\\*/", "");

            if (!willPrintDebugInfo)
            {
                tml = tml.replace("\n", "");
            }

            injectOne(sbuilder, new InjectObj("android_js_test", TestJs.class), tml, searchMoreForObjFun);

            for (InjectObj injectObj : injectObjs)
            {
                injectOne(sbuilder, injectObj, tml, searchMoreForObjFun);
            }


            preloadInterfaceJS = sbuilder.toString();
            if (willPrintDebugInfo)
            {
                Log.w(TAG, "the whole js:length=" + preloadInterfaceJS.length());
                Log.w(TAG, preloadInterfaceJS);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("init js error:" + e.getMessage());
        }
    }

    public boolean willPrintDebugInfo()
    {
        return willPrintDebugInfo;
    }

    private String readTemplate() throws Exception
    {
        InputStream in = null;
        try
        {
            in = getClass().getResourceAsStream("/safe-js/my-js-java-safe.js");
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

    private void injectOne(StringBuilder sbuilder, InjectObj injectObj, String tml,
            boolean searchMoreForObjFun) throws Exception
    {


        StringBuilder smethods = new StringBuilder();


        for (int i = 0; i < injectObj.interfaceClasses.size(); i++)
        {
            Class<?> c = injectObj.interfaceClasses.get(i);
            Constructor constructor = c.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object obj = constructor.newInstance();
            searchClassMethods(injectObj.namespace, smethods, obj);
        }


        for (int i = 0; i < injectObj.interfaceObjects.size(); i++)
        {
            Object object = injectObj.interfaceObjects.get(i);
            searchClassMethods(injectObj.namespace, smethods, object);
        }


        StringBuilder namespaces = new StringBuilder();

        {
            StringBuilder temp = new StringBuilder();
            String[] ss = injectObj.namespace.split("\\.");
            for (String s : ss)
            {
                if ("".equals(s))
                {
                    continue;
                } else
                {
                    temp.append(".").append(s);
                    namespaces.append("global").append(temp).append("=").append("global").append(temp).append("||{};");
                }
            }
        }


        tml = tml.replace("<SEARCH_MORE>", String.valueOf(searchMoreForObjFun));
        tml = tml.replace("<JAVA_CALLBACK>", "\"" + Java2JsCallback.JAVA_CALLBACK + "\"");
        tml = tml.replace("<NAMESPACE>", "\"" + injectObj.namespace + "\"");
        tml = tml.replace("<JSON_FUNCTION_STARTS>", JSON_FUNCTION_STARTS);
        tml = tml.replace("<LOG>", String.valueOf(willPrintDebugInfo));
        tml = tml.replace("<HOST_APP>", injectObj.namespace);
        tml = tml.replace("<HOST_APP_NAMESPACES>", namespaces);
        tml = tml.replace("<HOST_APP_FUN>", smethods);


        if (willPrintDebugInfo)
        {
            Log.w(TAG, injectObj.namespace);
            Log.w(TAG, namespaces.toString());
            Log.w(TAG, smethods.toString());
        }

        sbuilder.append(tml);
    }

    private void searchClassMethods(String namespace, StringBuilder sb, Object interfaceObj) throws Exception
    {
        /////个人建议还是用getMethods,这样可以不用把所有的static函数都挤在一个类里，而可以把一部分放在父类中.//////
        Method[] methods = interfaceObj.getClass().getMethods();

        for (Method method : methods)
        {
            String sign;

            if (method
                    .getModifiers() != Modifier.PUBLIC || Modifier
                    .isStatic(method.getModifiers()) || (sign = genJavaMethodSign(
                    method)) == null)
            {
                continue;
            }
            methodsMap.put(namespace + "." + sign, new MethodClass(method, interfaceObj));
            sb.append(namespace).append('.').append(method.getName()).append('=');
        }
    }

    private String genJavaMethodSign(Method method)
    {
        String sign = method.getName();
        Class[] argsTypes = method.getParameterTypes();
        int len = argsTypes.length;
        if (len < 1 || !argsTypes[0].isAssignableFrom(WEBView.class))
        {
            if (willPrintDebugInfo)
            {
                Log.w(TAG, "method(" + sign + ")'s first parameter is not " + WEBView.class + ",ignored!");
            }
            return null;
        }
        for (int k = 1; k < len; k++)
        {
            Class cls = argsTypes[k];
            if (cls == String.class)
            {
                sign += "_S";
            } else if (cls == int.class ||
                    cls == long.class ||
                    cls == float.class ||
                    cls == double.class)
            {
                sign += "_N";
            } else if (cls == boolean.class)
            {
                sign += "_B";
            } else if (cls == JSONObject.class)
            {
                sign += "_O";
            } else if (cls == JsCallback.class)
            {
                sign += "_F";
            } else
            {
                sign += "_P";
            }
        }
        return sign;
    }

    public String getPreloadInterfaceJS()
    {
        return preloadInterfaceJS;
    }


    private JSONObject parseObjFun(WEBView view, String namespace, JSONObject json) throws JSONException
    {

        Iterator<String> names = json.keys();
        JSONObject jobj = new JSONObject();
        while (names.hasNext())
        {
            String name = names.next();
            Object obj = json.get(name);
            jobj.put(name, obj);
            if (obj == null)
            {
                continue;
            }
            if ((obj instanceof String) && ((String) obj).startsWith(JSON_FUNCTION_STARTS))
            {
                jobj.put(name,
                        new JsCallback(view, namespace, ((String) obj).substring(JSON_FUNCTION_STARTS.length())));
            } else if ((obj instanceof JSONObject) && searchMoreForObjFun)
            {
                jobj.put(name, parseObjFun(view, namespace, (JSONObject) obj));
            }
        }

        return jobj;
    }

    public String call(WEBView webView, String jsonStr)
    {
        if (!TextUtils.isEmpty(jsonStr))
        {
            try
            {
                JSONObject callJson = new JSONObject(jsonStr);

                String namespace = callJson.getString("namespace");
                boolean isJavaCallback = callJson.getBoolean("isJavaCallback");

                String methodName = callJson.getString("method");
                JSONArray argsTypes = callJson.getJSONArray("types");
                JSONArray argsVals = callJson.getJSONArray("args");

                if (isJavaCallback)
                {


                    String callbackId = argsVals.getString(0);
                    String javaCallbackType = argsVals.getString(1);
                    int startIndex = 2;

                    if ("destroy".equals(javaCallbackType))
                    {
                        Java2JsCallback.remove(callbackId);
                        return getReturn(Java2JsCallback.JAVA_CALLBACK, methodName, 200, null);
                    } else
                    {

                        Java2JsCallback java2JsCallback = Java2JsCallback.get(callbackId);

                        if (java2JsCallback == null)
                        {
                            return getReturn(Java2JsCallback.JAVA_CALLBACK, methodName, 500,
                                    "not found java2JsCallback(id=" + callbackId + ")");
                        }


                        switch (javaCallbackType)
                        {
                            case "setPermanent":
                            {
                                boolean isPermanent = argsVals.getBoolean(startIndex);
                                java2JsCallback.setPermanent(isPermanent);
                                return getReturn(Java2JsCallback.JAVA_CALLBACK, methodName, 200, null);
                            }

                            case "callback":
                            {
                                Object[] args = new Object[argsVals.length() - startIndex + 1];
                                args[0] = webView;
                                for (int i = startIndex - 1; i < args.length; i++)
                                {
                                    args[i] = argsVals.get(i + 1);
                                }

                                return getReturn(Java2JsCallback.JAVA_CALLBACK, methodName, 200,
                                        java2JsCallback.callback(args));
                            }
                            default:

                                return getReturn(Java2JsCallback.JAVA_CALLBACK, methodName, 500,
                                        "unknown javaCallbackType(" + javaCallbackType + ")");
                        }

                    }
                }

                String sign = methodName;
                int len = argsTypes.length();
                Object[] values = new Object[len + 1];
                int numIndex = 0;
                String currType;

                values[0] = webView;

                for (int k = 0; k < len; k++)
                {
                    currType = argsTypes.optString(k);
                    if ("string".equals(currType))
                    {
                        sign += "_S";
                        values[k + 1] = argsVals.isNull(k) ? null : argsVals.getString(k);
                    } else if ("number".equals(currType))
                    {
                        sign += "_N";
                        numIndex = numIndex * 10 + k + 1;
                    } else if ("boolean".equals(currType))
                    {
                        sign += "_B";
                        values[k + 1] = argsVals.getBoolean(k);
                    } else if ("object".equals(currType))
                    {
                        sign += "_O";
                        JSONObject json = null;
                        if (!argsVals.isNull(k))
                        {
                            json = argsVals.getJSONObject(k);
                            json = parseObjFun(webView, namespace, json);
                        }
                        values[k + 1] = json;
                    } else if ("function".equals(currType))
                    {
                        sign += "_F";
                        values[k + 1] = new JsCallback(webView, namespace, argsVals.getString(k));
                    } else
                    {
                        sign += "_P";
                    }
                }

                MethodClass currMethod = methodsMap.get(namespace + "." + sign);

                // 方法匹配失败
                if (currMethod == null)
                {
                    return getReturn(jsonStr, null, 500,
                            "not found method(" + namespace + "." + sign + ") with valid parameters");
                }
                // 数字类型细分匹配
                if (numIndex > 0)
                {
                    Class[] methodTypes = currMethod.method.getParameterTypes();
                    int currIndex;
                    Class currCls;
                    while (numIndex > 0)
                    {
                        currIndex = numIndex - numIndex / 10 * 10;
                        currCls = methodTypes[currIndex];
                        if (currCls == int.class)
                        {
                            values[currIndex] = argsVals.getInt(currIndex - 1);
                        } else if (currCls == long.class)
                        {
                            //WARN: argsJson.getLong(k + defValue) will return a bigger incorrect number
                            values[currIndex] = Long.parseLong(argsVals.getString(currIndex - 1));
                        } else
                        {
                            values[currIndex] = argsVals.getDouble(currIndex - 1);
                        }
                        numIndex /= 10;
                    }
                }

                return getReturn(jsonStr, namespace + "." + sign, 200,
                        currMethod.method.invoke(currMethod.object, values));
            } catch (Exception e)
            {
                //优先返回详细的错误信息
                if (e.getCause() != null)
                {
                    return getReturn(jsonStr, null, 500, "method execute error:" + e.getCause().getMessage());
                }
                return getReturn(jsonStr, null, 500, "method execute error:" + e.getMessage());
            }
        } else
        {
            return getReturn(jsonStr, null, 500, "call data empty");
        }
    }

    private String getReturn(String reqJson, String callName, int stateCode, Object result)
    {
        String insertRes;
        if (result == null)
        {
            insertRes = "null";
        } else if (result instanceof String)
        {
            result = ((String) result).replace("\"", "\\\"");
            insertRes = "\"" + result + "\"";
        } else if (!(result instanceof Integer)
                && !(result instanceof Long)
                && !(result instanceof Boolean)
                && !(result instanceof Float)
                && !(result instanceof Double)
                && !(result instanceof JSONObject))
        {    // 非数字或者非字符串的构造对象类型都要序列化后再拼接
            if (gson == null)
            {
                gson = new Gson();
            }
            insertRes = gson.toJson(result);
        } else
        {  //数字直接转化
            insertRes = String.valueOf(result);
        }
        String resStr = String.format(RETURN_RESULT_FORMAT, stateCode, insertRes);
        if (willPrintDebugInfo)
        {
            Log.d(TAG, callName + " call json: " + reqJson + " result:" + resStr);
        }
        ////////
        return resStr;
    }
}

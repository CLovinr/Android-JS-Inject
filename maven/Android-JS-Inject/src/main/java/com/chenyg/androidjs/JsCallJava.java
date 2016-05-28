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

import static android.R.attr.name;

public class JsCallJava
{
    private final static String TAG = "JsCallJava";
    private final static String RETURN_RESULT_FORMAT = "{\"code\": %d, \"result\": %s}";
    public final static String JSON_FUNCTION_STARTS = "[function]=";
    private HashMap<String, MethodClass> methodsMap;

    private String preloadInterfaceJS;
    private Gson gson;
    private boolean willPrintDebugInfo, searchMoreForObjFun;


    static class MethodClass
    {
        Method method;
        Object object;

        public MethodClass(Method method, Object object)
        {
            this.method = method;
            this.object = object;
        }
    }


    /**
     * json对象中的字符串不能以{@linkplain #JSON_FUNCTION_STARTS}或{@linkplain Java2JsCallback#JAVA_CALLBACK}开头。
     */
    public static class InjectObj
    {
        String namespace;

        List<Class<?>> interfaceClasses = new ArrayList<>();
        List<Object> interfaceObjects = new ArrayList<>();

        /**
         * 对应的类必须含有无参构造函数。
         *
         * @param namespace        命名空间
         * @param interfaceClasses 要注入的类，必须含有无参构造函数.
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
         * @param interfaceObjects 要注入的对象
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

        /**
         * 添加注入对象。
         *
         * @param interfaceObjects 要注入的对象
         */
        public InjectObj add(Object... interfaceObjects)
        {
            for (Object object : interfaceObjects)
            {
                this.interfaceObjects.add(object);
            }

            return this;
        }

        /**
         * 添加要注入的类。
         *
         * @param interfaceClasses 必须含有无参构造函数。
         */
        public InjectObj add(Class<?>... interfaceClasses)
        {
            for (Class<?> c : interfaceClasses)
            {
                this.interfaceClasses.add(c);
            }
            return this;
        }
    }


    /**
     * @param willPrintDebugInfo 是否打印调试信息。
     * @param injectObjs         用于注入
     */
    public JsCallJava(boolean willPrintDebugInfo, InjectObj... injectObjs)
    {
        try
        {
            this.searchMoreForObjFun = true;
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

            injectOne(sbuilder, new InjectObj("android_js_test", TestJs.class), tml);

            for (InjectObj injectObj : injectObjs)
            {
                injectOne(sbuilder, injectObj, tml);
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

    private void injectOne(StringBuilder sbuilder, InjectObj injectObj, String tml) throws Exception
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
                    method, willPrintDebugInfo, true)) == null)
            {
                continue;
            }
            methodsMap.put(namespace + "." + sign, new MethodClass(method, interfaceObj));
            sb.append(namespace).append('.').append(method.getName()).append('=');
        }
    }

    static String genJavaMethodSign(Method method, boolean willPrintDebugInfo, boolean needWEBViewAndMethodName)
    {
        String sign = needWEBViewAndMethodName ? method.getName() : "";
        Class[] argsTypes = method.getParameterTypes();
        int len = argsTypes.length;
        if (needWEBViewAndMethodName && (len < 1 || !argsTypes[0].isAssignableFrom(WEBView.class)))
        {
            if (willPrintDebugInfo)
            {
                Log.w(TAG, "method(" + sign + ")'s first parameter is not " + WEBView.class + ",ignored!");
            }
            return null;
        }
        for (int k = needWEBViewAndMethodName ? 1 : 0; k < len; k++)
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
            } else if (cls == JSONArray.class)
            {
                sign += "_A";
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


    private Object parseObj(WEBView view, String namespace, Object obj) throws JSONException
    {
        if (obj != null)
        {
            if ((obj instanceof String) && ((String) obj).startsWith(JSON_FUNCTION_STARTS))
            {
                JsCallback jsCallback =
                        new JsCallback(view, namespace, (String) obj);
                jsCallback.isDebug = willPrintDebugInfo;
                obj = jsCallback;
            } else if ((obj instanceof JSONObject) && searchMoreForObjFun)
            {
                parseJSON(view, namespace, (JSONObject) obj);
            } else if ((obj instanceof JSONArray) && searchMoreForObjFun)
            {
                JSONArray array = (JSONArray) obj;
                for (int i = 0; i < array.length(); i++)
                {
                    Object object = array.get(i);
                    if (object != null)
                        array.put(i, parseObj(view, namespace, object));
                }
            }
        }
        return obj;
    }

    private void parseJSONArray(WEBView view, String namespace, JSONArray array) throws JSONException
    {
        for (int i = 0; i < array.length(); i++)
        {
            array.put(i, parseObj(view, namespace, array.get(i)));
        }
    }

    private void parseJSON(WEBView view, String namespace, JSONObject json) throws JSONException
    {
        Iterator<String> names = json.keys();
        while (names.hasNext())
        {
            String name = names.next();
            Object obj = json.get(name);
            json.put(name, parseObj(view, namespace, obj));
        }
    }

    public String call(WEBView webView, String jsonStr)
    {
        if (TextUtils.isEmpty(jsonStr))
        {
            return getReturn(jsonStr, null, 500, "call data empty");
        }
        try
        {
            JSONObject callJson = new JSONObject(jsonStr);

            if (willPrintDebugInfo)
            {
                Log.w(TAG, jsonStr);
            }

            String namespace = callJson.getString("namespace");
            boolean isJavaCallback = callJson.getBoolean("isJavaCallback");

            String methodName = callJson.getString("method");
            JSONArray argsTypes = callJson.getJSONArray("types");
            JSONArray argsVals = callJson.getJSONArray("args");

            Object[] values;
            DealArgsTemp dealArgsTemp;
            MethodClass currMethod;
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
                            values = new Object[argsVals.length() - startIndex];
                            dealArgsTemp = dealArgs(webView, values, 0, methodName, argsTypes, argsVals, startIndex,
                                    namespace);
                            currMethod = java2JsCallback.getMethodClass(dealArgsTemp.sign);
                        }
                        break;
                        default:
                            return getReturn(Java2JsCallback.JAVA_CALLBACK, methodName, 500,
                                    "unknown javaCallbackType(" + javaCallbackType + ")");
                    }

                }
            } else
            {
                values = new Object[argsTypes.length() + 1];
                values[0] = webView;
                dealArgsTemp = dealArgs(webView, values, 1, methodName, argsTypes, argsVals, 0, namespace);
                currMethod = methodsMap.get(namespace + "." + dealArgsTemp.sign);
            }


            String sign = dealArgsTemp.sign;
            int numIndex = dealArgsTemp.numIndex;


            // 方法匹配失败
            if (currMethod == null)
            {
                return getReturn(jsonStr, null, 500,
                        "not found method(" + (TextUtils
                                .isEmpty(namespace) ? "" : namespace + ".") + sign + ") with valid parameters");
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

            String rs = getReturn(jsonStr, (TextUtils.isEmpty(namespace) ? "" : namespace + ".") + sign, 200,
                    currMethod.method.invoke(currMethod.object, values));
            return rs;
        } catch (Exception e)
        {
            if (willPrintDebugInfo)
            {
                e.printStackTrace();
            }
            //优先返回详细的错误信息
            if (e.getCause() != null)
            {
                return getReturn(jsonStr, null, 500, "method execute error:" + e.getCause().getMessage());
            }
            return getReturn(jsonStr, null, 500, "method execute error:" + e.getMessage());
        }

    }

    static class DealArgsTemp
    {
        String sign;
        int numIndex = 0;

        public DealArgsTemp(String sign, int numIndex)
        {
            this.sign = sign;
            this.numIndex = numIndex;
        }
    }

    /**
     * 把js端传来的参数列表进行转换。
     *
     * @return sign
     * @throws JSONException
     */
    private DealArgsTemp dealArgs(WEBView webView, Object[] values, int offset, String methodName, JSONArray argsTypes,
            JSONArray argsVals, int offsetArgs, String namespace) throws JSONException

    {
        String sign = methodName;
        int numIndex = 0;
        String currType;
        int len = argsTypes.length();
        for (int m = offsetArgs; m < len; m++, offset++)
        {
            currType = argsTypes.optString(m);
            if ("string".equals(currType))
            {
                sign += "_S";
                values[offset] = argsVals.isNull(m) ? null : argsVals.getString(m);
            } else if ("number".equals(currType))
            {
                sign += "_N";
                numIndex = numIndex * 10 + m + 1;
            } else if ("boolean".equals(currType))
            {
                sign += "_B";
                values[offset] = argsVals.getBoolean(m);
            } else if ("object".equals(currType))
            {

                Object obj = argsVals.get(m);
                if (!argsVals.isNull(m))
                {
                    if (obj instanceof JSONArray)
                    {
                        sign += "_A";
                        parseJSONArray(webView, namespace, (JSONArray) obj);
                    } else if (obj instanceof JSONObject)
                    {
                        sign += "_O";
                        parseJSON(webView, namespace, (JSONObject) obj);
                    }
                } else
                {
                    sign += "_O";
                }
                values[offset] = obj;
            } else if ("function".equals(currType))
            {
                sign += "_F";
                JsCallback jsCallback = new JsCallback(webView, namespace, argsVals.getString(m));
                jsCallback.isDebug = willPrintDebugInfo;
                values[offset] = jsCallback;
            } else
            {
                sign += "_P";
            }
        }

        if (willPrintDebugInfo)
        {
            Log.d(TAG, "sign=" + sign + ",method=" + methodName);
        }

        return new DealArgsTemp(sign, numIndex);

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
            Log.w(TAG, "result:" + resStr);
        }
        ////////
        return resStr;
    }
}

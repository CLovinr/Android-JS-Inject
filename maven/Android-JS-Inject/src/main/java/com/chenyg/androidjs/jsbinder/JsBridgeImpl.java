package com.chenyg.androidjs.jsbinder;


import com.chenyg.androidjs.Java2JsCallback;
import com.chenyg.androidjs.JsCallback;
import com.chenyg.androidjs.WEBView;
import com.chenyg.uibinder.AttrEnum;
import com.chenyg.uibinder.OnValueChangedListener;
import com.chenyg.uibinder.js.JsBinder;
import com.chenyg.uibinder.js.JsBridge;
import com.chenyg.uibinder.js.JsView;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;


/**
 * Created by 宇宙之灵 on 2016/4/28.
 */
class JsBridgeImpl extends JsBinder implements JsBridge
{


    private String id;
    private JsCallback setCall, getCall;
    private ExecutorService executorService;
    private WeakReference<WEBView> webViewRef;
    private Java2JsCallback valueChangeJava2JsCallback, fireJava2JsCallback;


    public JsBridgeImpl(WEBView view, String id, JsCallback setCall, JsCallback getCall,
            ExecutorService executorService)
    {
        super(new JsView(null));
        viewType.setJsBridge(this);
        webViewRef = new WeakReference<>(view);
        this.id = id;
        this.setCall = setCall;
        this.getCall = getCall;
        this.executorService = executorService;

    }

    public String getCallbackId()
    {
        return id;
    }

    @Override
    protected void onInitOk()
    {
        super.onInitOk();
        if (isFireBinder())
        {
            try
            {
                fireJava2JsCallback = new Java2JsCallback(webViewRef.get())
                {
                    @Callback
                    public void callback()
                    {
                        executorService.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                onOccur();
                            }
                        });
                    }
                };
                fireJava2JsCallback.setPermanent(true);
                setCall.apply(id, "onclick", fireJava2JsCallback);
            } catch (JsCallback.JsCallbackException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void set(String name, Object value)
    {
        try
        {
            if (AttrEnum.ATTR_VALUE_CHANGE_LISTENER.name().equals(name))
            {
                onValueChangedListener = (OnValueChangedListener) value;
                if (valueChangeJava2JsCallback == null)
                {
                    valueChangeJava2JsCallback = new Java2JsCallback(webViewRef.get())
                    {
                        @Callback
                        public void callback(JSONObject value)
                        {
                            currentValue = value.opt("value");
                            doOnchange();
                        }
                    };
                    setCall.apply(id, name, valueChangeJava2JsCallback);
                    return;
                }
            }
            setCall.apply(id, name, value);
        } catch (JsCallback.JsCallbackException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Object get(String name)
    {
        JSONObject returnData = JsPageBuilder.getAttr(webViewRef.get(), getCall, id, name, executorService);
        if (returnData != null)
        {
            return returnData.opt("value");
        } else
        {
            return null;
        }
    }

    @Override
    public String parseAttrName(AttrEnum attrEnum)
    {
        String name = attrEnum.name();

        switch (attrEnum)
        {
            case METHOD_SET:
            case METHOD_ASYN_SET:
            case METHOD_GET:
                throw new RuntimeException("not allowed of " + attrEnum.name());
        }
        return name;
    }

    @Override
    public void release()
    {

        if (valueChangeJava2JsCallback != null)
        {
            try
            {
                valueChangeJava2JsCallback.destroy();
            } catch (JsCallback.JsCallbackException e)
            {
                e.printStackTrace();
            }
        }
        if (fireJava2JsCallback != null)
        {
            try
            {
                fireJava2JsCallback.destroy();
            } catch (JsCallback.JsCallbackException e)
            {
                e.printStackTrace();
            }
        }
    }

}
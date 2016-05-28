/**
 * Summary: js脚本所能执行的函数空间
 * Version 1.0
 * Date: 13-11-20
 * Time: 下午4:40
 * Copyright: Copyright (c) 2013
 */

package cn.pedant.SafeWebViewBridge.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import cn.pedant.SafeWebViewBridge.sample.util.TaskExecutor;
import com.chenyg.androidjs.JsCallback;
import com.chenyg.androidjs.WEBView;
import com.test.android.injectjs.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


//HostJsScope中需要被JS调用的函数，必须定义成public的非static函数，且必须包含WEBView这个参数
public class HostJsScope
{
    /**
     * 短暂气泡提醒
     *
     * @param webView 浏览器
     * @param message 提示信息
     */
    public void toast(WEBView webView, String message)
    {
        Toast.makeText(webView.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 可选择时间长短的气泡提醒
     *
     * @param webView    浏览器
     * @param message    提示信息
     * @param isShowLong 提醒时间方式
     */
    public void toast(WEBView webView, String message, int isShowLong)
    {
        Toast.makeText(webView.getContext(), message, isShowLong).show();
    }

    /**
     * 弹出记录的测试JS层到Java层代码执行损耗时间差
     *
     * @param webView   浏览器
     * @param timeStamp js层执行时的时间戳
     */
    public void testLossTime(WEBView webView, long timeStamp)
    {
        timeStamp = System.currentTimeMillis() - timeStamp;
        alert(webView, String.valueOf(timeStamp));
    }

    /**
     * 系统弹出提示框
     *
     * @param webView 浏览器
     * @param message 提示信息
     */
    public void alert(WEBView webView, String message)
    {
        // 构建一个Builder来显示网页中的alert对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
        builder.setTitle(webView.getContext().getString(R.string.dialog_title_system_msg));
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    public void alert(WEBView webView, int msg)
    {
        alert(webView, String.valueOf(msg));
    }

    public void alert(WEBView webView, boolean msg)
    {
        alert(webView, String.valueOf(msg));
    }

    /**
     * 获取设备IMSI
     *
     * @param webView 浏览器
     * @return 设备IMSI
     */
    public String getIMSI(WEBView webView)
    {
        return ((TelephonyManager) webView.getContext().getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
    }

    /**
     * 获取用户系统版本大小
     *
     * @param webView 浏览器
     * @return 安卓SDK版本
     */
    public int getOsSdk(WEBView webView)
    {
        return Build.VERSION.SDK_INT;
    }

    //---------------- 界面切换类 ------------------

    /**
     * 结束当前窗口
     *
     * @param view 浏览器
     */
    public void goBack(WEBView view)
    {
        if (view.getContext() instanceof Activity)
        {
            ((Activity) view.getContext()).finish();
        }
    }

    /**
     * 传入Json对象
     *
     * @param view 浏览器
     * @param jo   传入的JSON对象
     * @return 返回对象的第一个键值对
     */
    public String passJson2Java(WEBView view, JSONObject jo)
    {
        Iterator iterator = jo.keys();
        String res = null;
        if (iterator.hasNext())
        {
            try
            {
                String keyW = (String) iterator.next();
                res = keyW + ": " + jo.getString(keyW);
            } catch (JSONException je)
            {

            }
        }
        return res;
    }

    /**
     * 将传入Json对象直接返回
     *
     * @param view 浏览器
     * @param jo   传入的JSON对象
     * @return 返回对象的第一个键值对
     */
    public JSONObject retBackPassJson(WEBView view, JSONObject jo)
    {
        return jo;
    }

    public int overloadMethod(WEBView view, int val)
    {
        return val;
    }

    public String overloadMethod(WEBView view, String val)
    {
        return val;
    }

    public class RetJavaObj
    {
        public int intField;
        public String strField;
        public boolean boolField;
    }

    public List<RetJavaObj> retJavaObject(WEBView view)
    {
        RetJavaObj obj = new RetJavaObj();
        obj.intField = 1;
        obj.strField = "mine str";
        obj.boolField = true;
        List<RetJavaObj> rets = new ArrayList<RetJavaObj>();
        rets.add(obj);
        return rets;
    }

    public void delayJsCallBack(WEBView view, int ms, final String backMsg, final JsCallback jsCallback)
    {
        TaskExecutor.scheduleTaskOnUiThread(ms * 1000, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    jsCallback.apply(backMsg);
                } catch (JsCallback.JsCallbackException je)
                {
                    je.printStackTrace();
                }
            }
        });
    }

    public long passLongType(WEBView view, long i)
    {
        return i;
    }
}
package com.chenyg.androidjs.jsbinder;

import com.chenyg.uibinder.GetAttrException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

/**
 * Created by 宇宙之灵 on 2016/4/28.
 */
class CallResult implements Callable<JSONObject>
{

    JSONObject returnData;
    private long sleep;
    private long timeout;

    public CallResult(long sleep, long timeout)
    {
        this.sleep = sleep;
        this.timeout = timeout;
    }

    @Override
    public JSONObject call() throws Exception
    {
        long time = System.currentTimeMillis();
        while (returnData == null)
        {
            Thread.sleep(sleep);
            if (System.currentTimeMillis() - time > timeout)
            {
                throw new GetAttrException.GetAttrTimeoutException();
            }
        }
        return returnData;
    }
}

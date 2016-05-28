package com.chenyg.androidjs;

/**
 * Created by 宇宙之灵 on 2016/5/28.
 */
interface InjectHandle
{

    enum Result
    {
        OK,
        FAILED
    }

    /**
     * 是否是注入使用的命令字符串。
     */
    boolean isInjectCmd(String str);

    /**
     * 处理注入
     *
     * @param webView
     * @param cmd
     * @return
     */
    Result dealCmd(WEBView webView, String cmd);


    void onProgressChanged(WEBView webView, int newProgress);
}

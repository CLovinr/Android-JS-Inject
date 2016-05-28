package com.chenyg.androidjs;

/**
 * 待完善
 * Created by 宇宙之灵 on 2016/5/19.
 */
public enum InjectType
{
    /**
     * 自动注入.
     * <pre>
     *     1.首先判断window.autoInjectOk是否为true，若是则已经注入成功！
     *     2.若第1步没有成功，则通过window.autoInjectReady=function(){}，当注入成功时会调用该函数。
     * </pre>
     */
    Auto,
    /**
     * 手动注入,需要引入相关js。
     */
    Self
}

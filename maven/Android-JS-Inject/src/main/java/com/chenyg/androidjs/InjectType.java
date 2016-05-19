package com.chenyg.androidjs;

/**
 * 待完善
 * Created by 宇宙之灵 on 2016/5/19.
 */
 enum InjectType
{
    /**
     * 自动注入
     */
    Auto,
    /**
     * 手动注入,需要引入相关js。
     */
    Self,
    /**
     * 通过alert("&#60;inject-js&#62;")进行注入;需要引入相关js。
     */
    Alert
}

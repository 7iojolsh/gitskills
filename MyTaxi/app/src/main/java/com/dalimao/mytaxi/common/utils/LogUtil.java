package com.dalimao.mytaxi.common.utils;

import android.util.Log;

/**
 * 项目名：myTaxi
 * 包名：italker.yijia.net.mytaxi.common.utils
 * 文件名：LogUtil
 * 创建者：周成东
 * 创建时间:2017/10/14 15:34
 * 描述：ToDo
 */

public class LogUtil {

    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }
}

package com.dalimao.mytaxi.common.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 文件名：ToastUtil
 * 创建时间:2017/10/14 11:35
 * 描述：ToDo
 */

public class ToastUtil {
    public static void show(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
}

package com.hooaha.andr.im.zuzhii.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by haoliu on 2016/11/29.
 */
public class ToastUtil {
    public static void show(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
}

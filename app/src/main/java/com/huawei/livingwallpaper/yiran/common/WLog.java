package com.huawei.livingwallpaper.yiran.common;

import android.util.Log;

public class WLog {
    private static final String TAG = "_Wallpaper";
    private static final boolean DEBUG = true;
    private static final boolean INFO = true;

    public static void i(Object object, String msg) {
        if(INFO) {
            if(object instanceof String) {
                Log.i(object + TAG, msg);
            } else {
                Log.i(object.getClass().getSimpleName() + TAG, msg);
            }
        }
    }

    public static void d(Object object, String msg) {
        if(DEBUG)
            Log.i(object.getClass().getSimpleName() + TAG, msg);
    }

    public static void e(Object object, String msg) {
            Log.e(object.getClass().getSimpleName() + TAG, msg);
    }
}

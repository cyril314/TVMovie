package com.movie.util;

import android.util.Log;

/**
 * 日志
 *
 * @author aim
 * @date :2020/12/18
 * @description:
 */
public class L {

    private static final String TAG = "TVMovie";

    public static void e(String msg) {
        Log.e(TAG, "" + msg);
    }

    public static void i(String msg) {
        Log.i(TAG, "" + msg);
    }
}
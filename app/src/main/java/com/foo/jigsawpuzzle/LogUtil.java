package com.foo.jigsawpuzzle;

import android.util.Log;

/**
 * @desc: TODO
 * @author: Major
 * @since: 2017/1/4 0:59
 */

public class LogUtil {

    private static final String TAG = "ele_a";

    public static void printE(String msg) {
        Log.e(TAG, "" + msg);
    }
}

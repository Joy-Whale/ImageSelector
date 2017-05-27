package cn.joy.imageselector;

import android.util.Log;

/**
 * **********************
 * Author: yu
 * Date:   2015/7/8
 * Time:   14:52
 * **********************
 */
public class Logs {

    public static boolean isLog = true;

    public static void setLog(boolean isLog) {
        Logs.isLog = isLog;
    }


    public static void e(String tag, String msg) {
        if (isLog)
            Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isLog)
            Log.w(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isLog)
            Log.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (isLog)
            Log.v(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (isLog)
            Log.i(tag, msg);
    }
}

package cn.hadcn.davinci.base;

import android.util.Log;

/**
 * LogUtil
 * @author 90Chris
 */
public class VinciLog {
    public static String LOG_TAG = "DaVinci";
	public static boolean enableLog = false;//true:enable log display

	private static final int RETURN_NOLOG = 99;

    @SuppressWarnings("unused")
	public static int d(String msg) {
        return enableLog ? Log.d(LOG_TAG + ":", msg) : RETURN_NOLOG;
    }

    @SuppressWarnings("unused")
	public static int e(String msg) {
        return enableLog ? Log.e(LOG_TAG + ":", msg) : RETURN_NOLOG;
    }

    @SuppressWarnings("unused")
    public static int i(String msg) {
        return enableLog ? Log.i(LOG_TAG + ":", msg) : RETURN_NOLOG;
    }

    @SuppressWarnings("unused")
    public static int w(String msg) {
        return enableLog ? Log.w(LOG_TAG + ":", msg) : RETURN_NOLOG;
    }
}

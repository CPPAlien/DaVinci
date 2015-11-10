package cn.hadcn.davinci;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import cn.hadcn.davinci.base.VinciLog;
import cn.hadcn.davinci.cache.DaImageLoader;
import cn.hadcn.davinci.http.BaseRequest;


/**
 * DaVinci
 * Created by 90Chris on 2015/9/10.
 */
public class DaVinci {
    private static RequestQueue mRequestQueue;
    private static DaImageLoader mDaImageLoader;

    private static DaVinci mDaVinci = null;

    public static DaVinci with(Context context) {
        if ( mDaVinci == null ) {
            mDaVinci = new DaVinci(context);
        }
        return mDaVinci;
    }

    /**
     * if you want to use this way, you must call DaVinci.init() before using it
     * @return DaVinci instance
     */
    public static DaVinci with() {
        if ( mDaVinci == null ) {
            VinciLog.e("DaVinci instance has not been initialized yet, please use DaVinci.init() first");
        }
        return mDaVinci;
    }

    /**
     * init DaVinci instance, it's better to put it into application class
     * advantages
     * 1, you do not use pass context in request any more;
     * 2, application context is special in whole application life, so DaVinci do not need hook activity, optimize the memory management;
     * @param context context
     */
    public static void init(Context context){
        mDaVinci = new DaVinci(context);
    }

    /**
     * each http request are different instance
     * but for image loader, there is only one instance for whole application
     * @param context context
     */
    private DaVinci(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mDaImageLoader = new DaImageLoader(context, mRequestQueue);
    }

    /**
     * enable log display
     */
    public static void enableDebug(String tag) {
        VinciLog.enableLog = true;
        VinciLog.LOG_TAG = tag;
    }

    public BaseRequest getHttpRequest(){
        return new BaseRequest(mRequestQueue);
    }

    public DaImageLoader getImageLoader() {
        return mDaImageLoader;
    }
}

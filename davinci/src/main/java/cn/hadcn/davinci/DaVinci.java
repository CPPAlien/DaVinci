package cn.hadcn.davinci;

import android.content.Context;


import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;

import cn.hadcn.davinci.base.LogLevel;
import cn.hadcn.davinci.base.VinciLog;
import cn.hadcn.davinci.image.VinciImageLoader;
import cn.hadcn.davinci.http.impl.HttpRequest;
import cn.hadcn.davinci.http.impl.PersistentCookieStore;
import cn.hadcn.davinci.upload.impl.VinciUpload;
import cn.hadcn.davinci.volley.RequestQueue;
import cn.hadcn.davinci.volley.toolbox.Volley;


/**
 * DaVinci
 * Created by 90Chris on 2015/9/10.
 */
public class DaVinci {
    private static RequestQueue mRequestQueue;
    private static VinciImageLoader mDaImageLoader;
    private static DaVinci sDaVinci = null;
    private boolean isEnableCookie = false;
    private CookieManager mCookieManager = null;
    private Context mContext = null;

    public static DaVinci with(Context context) {
        if ( sDaVinci == null ) {
            sDaVinci = new DaVinci(context.getApplicationContext());
        }
        return sDaVinci;
    }

    /**
     * if you want to use this way, you must call DaVinci.init(Context) before using it
     * @return DaVinci instance
     */
    public static DaVinci with() {
        if ( sDaVinci == null ) {
            VinciLog.e("DaVinci instance has not been initialized yet, please use DaVinci.init() first");
        }
        return sDaVinci;
    }

    /**
     * init DaVinci instance, it's better to put it into application class
     * advantages
     * 1, you do not use pass context in request any more;
     * 2, application context is special in whole application life, so DaVinci do not need hook activity, optimize the memory management;
     * @param logLevel log level
     * @param debugTag log tag
     * @param context context
     */
    public static void init(LogLevel logLevel, String debugTag, Context context){
        sDaVinci = new DaVinci(context);
        VinciLog.init(logLevel, debugTag, context);
    }

    /**
     * each http request are different instance
     * but for image loader, there is only one instance for whole application
     * @param context context
     */
    private DaVinci(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
        mDaImageLoader = new VinciImageLoader(context, mRequestQueue);
    }

    /**
     * enable cookie, save cookie when response header contains Set-Cookie
     * add Cookie header when sending request
     */
    public void enableCookie() {
        isEnableCookie = true;
        if ( mCookieManager == null ) {
            mCookieManager = new CookieManager(new PersistentCookieStore(mContext), CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(mCookieManager);
        }
    }

    private int mMaxRetries = 0;
    private int mTimeOut = 0;
    /**
     * set http configures globally
     * @param maxRetires max retries, default is 1
     * @param timeout timeout, default is 2500ms
     */
    public void setHttpGlobal(int maxRetires, int timeout) {
        mMaxRetries = maxRetires;
        mTimeOut = timeout;
    }

    /**
     * different instances in different calls, and put them into a request queue
     * @return HttpRequest
     */
    public HttpRequest getHttpRequest(){
        String cookieString = null;
        if ( isEnableCookie ) {
            // cookie may be changed at any request, so get the cookie from memory first
            // if cookie is empty, get it from disk, else save it.
            // because the default CookieStoreImpl doesn't implement local save
            StringBuilder cookieBuilder = new StringBuilder();
            String divider = "";
            for (HttpCookie cookie : mCookieManager.getCookieStore().getCookies()) {
                cookieBuilder.append(divider);
                divider = ";";
                cookieBuilder.append(cookie.getName());
                cookieBuilder.append("=");
                cookieBuilder.append(cookie.getValue());
            }

            cookieString = cookieBuilder.toString();
        }

        HttpRequest request = new HttpRequest( mRequestQueue, isEnableCookie, cookieString );
        if ( mMaxRetries != 0 ) {
            request.maxRetries(mMaxRetries);
        }
        if ( mTimeOut != 0) {
            request.timeOut(mTimeOut);
        }
        return request;
    }

    public VinciImageLoader getImageLoader() {
        return mDaImageLoader;
    }

    /**
     * get uploader instance, default content type is binary
     */
    public VinciUpload getUploader() {
        return new VinciUpload(mRequestQueue);
    }
}

package cn.hadcn.davinci;

import android.content.Context;


import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

import cn.hadcn.davinci.base.VolleyManager;
import cn.hadcn.davinci.log.LogLevel;
import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.image.VinciImageLoader;
import cn.hadcn.davinci.http.impl.HttpRequest;
import cn.hadcn.davinci.http.impl.PersistentCookieStore;
import cn.hadcn.davinci.other.impl.VinciDownload;
import cn.hadcn.davinci.other.impl.VinciUpload;
import cn.hadcn.davinci.volley.RequestQueue;


/**
 * DaVinci
 * Created by 90Chris on 2015/9/10.
 */
public class DaVinci {
    /** Number of network request dispatcher threads to start. */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    private static RequestQueue mRequestQueue;
    private static VinciImageLoader mDaImageLoader;

    private static RequestQueue mDefaultRequestQueue;
    private static VinciImageLoader mDefaultDaImageLoader;

    private static DaVinci sDaVinci = null;
    private boolean isEnableCookie = false;
    private CookieManager mCookieManager = null;
    private static Context mContext = null;
    private Map<String, RequestQueue> queues = new HashMap<>();
    private Map<String, VinciImageLoader> loaders = new HashMap<>();

    public static DaVinci with(Context context) {
        mContext = context.getApplicationContext();
        if ( sDaVinci == null ) {
            sDaVinci = new DaVinci(0);
        }

        mRequestQueue = mDefaultRequestQueue;
        mDaImageLoader = mDefaultDaImageLoader;

        return sDaVinci;
    }

    /**
     * if you want to use this way, you must call DaVinci.init(Context) before using it
     * @return DaVinci instance
     */
    public static DaVinci with() {
        if ( sDaVinci == null ) {
            throw new RuntimeException("DaVinci instance has not been initialized yet, please use DaVinci.init() first");
        }
        mRequestQueue = mDefaultRequestQueue;
        mDaImageLoader = mDefaultDaImageLoader;

        return sDaVinci;
    }

    /**
     * use other thread pool
     * @param tag tag of thread pool
     * @return this
     */
    public DaVinci tag(String tag) {
        if ( !queues.containsKey(tag) ) {
            throw new RuntimeException("The pool has not been initialized");
        }
        mRequestQueue = queues.get(tag);
        mDaImageLoader = loaders.get(tag);
        return this;
    }

    /**
     * add other thread pool
     * @param tag tag
     * @param size size
     */
    public void addThreadPool(String tag, int size) {
        if ( size <= 0 ) {
            throw new RuntimeException("pool size at least one");
        }
        RequestQueue requestQueue = VolleyManager.newRequestQueue(mContext, size);
        VinciImageLoader imageLoader = new VinciImageLoader(mContext, requestQueue);
        queues.put(tag, requestQueue);
        loaders.put(tag, imageLoader);
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
        init(0, logLevel, debugTag, context);
    }


    /**
     * init DaVinci instance, it's better to put it into application class
     * advantages
     * 1, you do not use pass context in request any more;
     * 2, application context is special in whole application life, so DaVinci do not need hook activity, optimize the memory management;
     * @param poolSize thread pool size
     * @param logLevel log level
     * @param debugTag log tag
     * @param context context
     */
    public static void init(int poolSize, LogLevel logLevel, String debugTag, Context context) {
        mContext = context.getApplicationContext();
        sDaVinci = new DaVinci(poolSize);
        VinciLog.init(logLevel, debugTag, context);
    }

    /**
     * each http request are different instance
     * but for image loader, there is only one instance for whole application
     */
    private DaVinci(int poolSize) {
        if ( poolSize <= 0 ) {
            poolSize = DEFAULT_NETWORK_THREAD_POOL_SIZE;
        }
        mDefaultRequestQueue = VolleyManager.newRequestQueue(mContext, poolSize);
        mDefaultDaImageLoader = new VinciImageLoader(mContext, mDefaultRequestQueue);
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

    public VinciDownload getDownloader() {
        return new VinciDownload(mRequestQueue);
    }
}

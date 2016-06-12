package cn.hadcn.davinci.http.impl;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.hadcn.davinci.base.RequestWay;
import cn.hadcn.davinci.base.VinciLog;
import cn.hadcn.davinci.base.VolleyHttpBase;
import cn.hadcn.davinci.http.OnDaVinciRequestListener;
import cn.hadcn.davinci.volley.AuthFailureError;
import cn.hadcn.davinci.volley.DefaultRetryPolicy;
import cn.hadcn.davinci.volley.Request;
import cn.hadcn.davinci.volley.RequestQueue;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.VolleyError;

/**
 * BaseRequest
 * Created by 90Chris on 2015/1/26.
 * */
public class HttpRequest {
    private RequestQueue mRequestQueue;
    private Map<String, String> mHeadersMap = new HashMap<>();
    private String mContentType = null;
    private String mCharset = "utf-8";
    private int mTimeOutMs = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
    private int mMaxRetries = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;
    private boolean isEnableCookie = false;
    private String mCookie = null;
    private OnDaVinciRequestListener mRequestListener = null;

    public HttpRequest( RequestQueue requestQueue, boolean enableCookie, String cookie) {
        mRequestQueue = requestQueue;
        isEnableCookie = enableCookie;
        mCookie = cookie;
    }

    /**
     * timeout millisecond, default is 2500 ms
     * @param timeOutMs timeout
     * @return this
     */
    public HttpRequest timeOut(int timeOutMs) {
        mTimeOutMs = timeOutMs;
        return this;
    }

    /**
     * retry times, default is once
     * @param maxRetries time of retrying
     * @return this
     */
    public HttpRequest maxRetries(int maxRetries) {
        mMaxRetries = maxRetries;
        return this;
    }

    /**
     * add header in request
     * @param headersMap header
     * @return this
     */
    public HttpRequest headers(Map<String, String> headersMap) {
        mHeadersMap = headersMap;
        return this;
    }

    /**
     * set Content-Type field, default is application/json
     * @param contentType content-type
     * @return this
     */
    public HttpRequest contentType(String contentType) {
        mContentType = contentType;
        return this;
    }

    /**
     * set Content-Type field, default is application/json
     * @param contentType content-type
     * @param charset charset of request body, default is utf-8
     * @return this
     */
    public HttpRequest contentType(String contentType, String charset) {
        mContentType = contentType;
        mCharset = charset;
        return this;
    }

    /**
     * get method
     * @param requestUrl request url of get, must include http:// as head
     * @param params get parameters, will combine the params as a get request, like http://ninty.cc?a=1&b=2
     * @param requestListener listener
     */
    public void doGet(String requestUrl, Map<String, Object> params, OnDaVinciRequestListener requestListener) {
        doRequest(RequestWay.GET,
                requestUrl, params, null, requestListener);
    }

    /**
     * post method
     * @param requestUrl request url of post, must include http:// as head
     * @param postJsonData post contents, json format data
     * @param requestListener listener
     */
    public void doPost(String requestUrl, JSONObject postJsonData, OnDaVinciRequestListener requestListener) {
        doRequest(RequestWay.POST,
                requestUrl, null, postJsonData, requestListener);
    }

    /**
     * post method
     * @param requestUrl request url of post, must include http:// as head
     * @param postBodyString post body part, String type
     * @param requestListener listener
     */
    public void doPost(String requestUrl, String postBodyString, OnDaVinciRequestListener requestListener) {
        doRequest(RequestWay.POST,
                requestUrl, null, postBodyString, requestListener);
    }

    /**
     * post method
     * @param requestUrl request url of post, must include http:// as head
     * @param requestListener listener
     */
    public void doPost(String requestUrl, OnDaVinciRequestListener requestListener) {
        doRequest(RequestWay.POST,
                requestUrl, null, null, requestListener);
    }

    /**
     * do http request
     * @param way GET or POST
     * @param url request url
     * @param urlMap get method parameters  map
     * @param postBody post method parameters
     * @param requestListener listener
     */
    private void doRequest(RequestWay way, String url, Map<String, Object> urlMap, Object postBody, final OnDaVinciRequestListener requestListener) {
        mRequestListener = requestListener;
        String requestUrl = url;

        //construct url
        if ( null != urlMap ){
            requestUrl += "?";
            for ( String key : urlMap.keySet() ) {
                requestUrl = requestUrl + key + "=" + urlMap.get(key) + "&";
            }
        }

        VinciLog.d("Do " + way.getType() + " request, url = " + requestUrl);
        DaVinciHttp jsonObjectRequest = getRequest(way, requestUrl, postBody);

        if ( jsonObjectRequest == null ){
            VinciLog.e("post body type is error, it should be json or string");
            return;
        }

        if ( isEnableCookie ) {
            jsonObjectRequest.setCookie( mCookie );
        }
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(mTimeOutMs, mMaxRetries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(jsonObjectRequest);
    }

    private DaVinciHttp getRequest(RequestWay way, String requestUrl, Object postBody) {
        int volleyWay;

        //get volley method code, get or post
        switch (way) {
            case GET:
                volleyWay = Request.Method.GET;
                break;
            default:
                volleyWay = Request.Method.POST;
                if ( null != postBody ){
                    VinciLog.d("doPost data = " + postBody.toString());
                }
                break;
        }

        //inflate body part depends on type we get
        DaVinciHttp jsonObjectRequest = null;
        if ( postBody == null ) {
            jsonObjectRequest = new DaVinciHttp(volleyWay, requestUrl,
                    new ResponseListener(),
                    new ErrorListener());
        } else if ( postBody instanceof JSONObject ) {
            jsonObjectRequest = new DaVinciHttp(volleyWay, requestUrl, (JSONObject)postBody,
                    new ResponseListener(),
                    new ErrorListener());
        } else if (postBody instanceof String ) {
            jsonObjectRequest = new DaVinciHttp(volleyWay, requestUrl, (String)postBody,
                    new ResponseListener(),
                    new ErrorListener());
        }

        return jsonObjectRequest;
    }


    private class ResponseListener implements Response.Listener<String> {

        @Override
        public void onResponse(String response) {
            VinciLog.d("response:" + response);
            if ( mRequestListener != null ) {
                mRequestListener.onDaVinciRequestSuccess(response);
            }
        }
    }

    private class ErrorListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            String reason = null;
            if ( error.networkResponse != null ) {
                reason = "status code : " + String.valueOf(error.networkResponse.statusCode) + ";";
                byte[] data = error.networkResponse.data;
                reason += ( data == null ? null : new String(data) );
            }
            VinciLog.e("http failed: " + reason);
            if ( mRequestListener != null ) {
                mRequestListener.onDaVinciRequestFailed(reason);
            }
        }
    }

    private class DaVinciHttp extends VolleyHttpBase {

        /** Content type for request. */
        private final String PROTOCOL_CONTENT_TYPE = "application/json";

        public DaVinciHttp(int method, String url, String requestBody, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, requestBody, listener, errorListener);
        }

        @Override
        public String getBodyContentType() {
            String contentType = PROTOCOL_CONTENT_TYPE;
            if ( mContentType != null ) {
                contentType = mContentType;
            }
            return String.format("%s; charset=%s", contentType, mCharset);
        }

        public DaVinciHttp(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, null, listener, errorListener);
        }

        public DaVinciHttp(int method, String url, JSONObject jsonRequest, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, jsonRequest.toString(), listener, errorListener);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            VinciLog.d("Headers:" + mHeadersMap.toString());
            return mHeadersMap;
        }

        /**
         * set Cookie content
         * @param cookie cookie content
         */
        public void setCookie( String cookie ) {
            VinciLog.d("Put Cookie:" + cookie);
            mHeadersMap.put("Cookie", cookie);
        }
    }
}
package cn.hadcn.davinci.http.impl;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.Map;

import cn.hadcn.davinci.base.VinciLog;
import cn.hadcn.davinci.base.RequestMethod;
import cn.hadcn.davinci.http.OnDaVinciRequestListener;


/**
 * BaseRequest
 * Created by 90Chris on 2015/1/26.
 * */
public class HttpRequest {
    private RequestQueue mRequestQueue;
    private Map<String, String> mHeadersMap = null;
    private int mTimeOutMs = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
    private int mMaxRetries = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;
    private boolean isEnableCookie = false;
    private String mCookie = null;
    private OnDaVinciRequestListener mRequestListner = null;

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
    public HttpRequest setTimeOut(int timeOutMs) {
        mTimeOutMs = timeOutMs;
        return this;
    }

    /**
     * retry times, default is once
     * @param maxRetries time of retrying
     * @return this
     */
    public HttpRequest setMaxRetries(int maxRetries) {
        mMaxRetries = maxRetries;
        return this;
    }

    public HttpRequest addHeaders(Map<String, String> headersMap) {
        mHeadersMap = headersMap;
        return this;
    }

    /**
     * get method
     * @param requestUrl request url of get, must include http:// as head
     * @param params get parameters, will combine the params as a get request, like http://ninty.cc?a=1&b=2
     * @param requestListener listener
     */
    public void doGet(String requestUrl, Map<String, Object> params, OnDaVinciRequestListener requestListener) {
        doRequest(RequestMethod.Way.GET,
                requestUrl, params, null, requestListener);
    }

    /**
     * post method
     * @param requestUrl request url of post, must include http:// as head
     * @param postJsonData post contents, json format data
     * @param requestListener listener
     */
    public void doPost(String requestUrl, JSONObject postJsonData, OnDaVinciRequestListener requestListener) {
        doRequest(RequestMethod.Way.POST,
                requestUrl, null, postJsonData, requestListener);
    }

    /**
     * post method
     * @param requestUrl request url of post, must include http:// as head
     * @param postBodyString post body part, String type
     * @param requestListener listener
     */
    public void doPost(String requestUrl, String postBodyString, OnDaVinciRequestListener requestListener) {
        doRequest(RequestMethod.Way.POST,
                requestUrl, null, postBodyString, requestListener);
    }

    /**
     * do http request
     * @param way GET or POST
     * @param url request url
     * @param urlMap get method parameters  map
     * @param postBody post method parameters
     * @param requestListener listener
     */
    private void doRequest(RequestMethod.Way way, String url, Map<String, Object> urlMap, Object postBody, final OnDaVinciRequestListener requestListener) {
        mRequestListner = requestListener;
        String requestUrl = url;

        //construct url
        if ( null != urlMap ){
            requestUrl += "?";
            VinciLog.i("url map = " + urlMap.toString());
            for ( String key : urlMap.keySet() ) {
                requestUrl = requestUrl + key + "=" + urlMap.get(key) + "&";
            }
        } else {
            VinciLog.i("url map = null");
        }

        JsonVinciRequest jsonObjectRequest = getRequest(way, requestUrl, postBody);

        if ( isEnableCookie ) {
            jsonObjectRequest.setCookie( mCookie );
        }
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(mTimeOutMs, mMaxRetries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(jsonObjectRequest);
    }

    private JsonVinciRequest getRequest(RequestMethod.Way way, String requestUrl, Object postBody) {
        int volleyWay;

        //get volley method code, get or post
        switch (way) {
            case GET:
                volleyWay = Request.Method.GET;
                break;
            default:
                volleyWay = Request.Method.POST;
                if ( null != postBody ){
                    VinciLog.i("doPost data = " + postBody.toString());
                } else {
                    VinciLog.i("doPost map = null");
                }
                break;
        }

        //inflate body part depends on type we get
        JsonVinciRequest jsonObjectRequest = null;
        if ( postBody instanceof JSONObject ) {
            jsonObjectRequest = new DaVinciRequest(volleyWay, requestUrl, (JSONObject)postBody,
                    new ResponseListener(),
                    new ErrorListener());
        }else if (postBody instanceof String ) {
            jsonObjectRequest = new DaVinciRequest(volleyWay, requestUrl, (String)postBody,
                    new ResponseListener(),
                    new ErrorListener());
        }
        return jsonObjectRequest;
    }


    private class ResponseListener implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {
            VinciLog.i("http response:" + (response == null ? null : response.toString()));
            if ( mRequestListner != null ) {
                mRequestListner.onDaVinciRequestSuccess(response);
            }
        }
    }

    private class ErrorListener implements Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            String reason = error.networkResponse == null ? null : String.valueOf(error.networkResponse.statusCode);
            VinciLog.e("http failed: " + reason);
            if ( mRequestListner != null ) {
                mRequestListner.onDaVinciRequestFailed(reason);
            }
        }
    }

    private class DaVinciRequest extends JsonVinciRequest{

        public DaVinciRequest(int method, String url, String requestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, requestBody, listener, errorListener);
        }

        public DaVinciRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> mapHeaders = super.getHeaders();
            if (mHeadersMap != null) {
                for (String key : mHeadersMap.keySet()) {
                    mapHeaders.put(key, mHeadersMap.get(key));
                }
            }
            VinciLog.d("header:" + mapHeaders.toString());
            return mapHeaders;
        }
    }
}

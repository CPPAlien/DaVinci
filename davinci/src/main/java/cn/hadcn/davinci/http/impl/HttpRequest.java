package cn.hadcn.davinci.http.impl;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

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
    RequestQueue mRequestQueue;
    Map<String, String> mHeadersMap = null;
    int mTimeOutMs = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
    int mMaxRetries = DefaultRetryPolicy.DEFAULT_MAX_RETRIES;

    public HttpRequest(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
    }

    public HttpRequest setTimesOut(int timesOutMs) {
        mTimeOutMs = timesOutMs;
        return this;
    }

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
     * do http request
     * @param urlMap get method parameters  map
     * @param postJsonData post method parameters json format data
     * @param requestListener listener
     */
    private void doRequest(RequestMethod.Way way, String url, Map<String, Object> urlMap, JSONObject postJsonData, final OnDaVinciRequestListener requestListener) {
        int volleyWay;
        String requestUrl = url;

        if ( null != urlMap ){
            requestUrl += "?";
            VinciLog.i("url map = " + urlMap.toString());
            for ( String key : urlMap.keySet() ) {
                requestUrl = requestUrl + key + "=" + urlMap.get(key) + "&";
            }
        } else {
            VinciLog.i("url map = null");
        }

        switch (way) {
            case GET:
                volleyWay = Request.Method.GET;
                break;
            default:
                volleyWay = Request.Method.POST;
                if ( null != postJsonData ){
                    VinciLog.i("doPost data = " + postJsonData.toString());
                } else {
                    VinciLog.i("doPost map = null");
                }
                break;
        }

        VinciLog.i("send request:" + requestUrl);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(volleyWay, requestUrl, postJsonData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VinciLog.i("http response:" + (response == null ? null : response.toString()));
                        requestListener.onDaVinciRequestSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String reason = error.networkResponse == null ? null : String.valueOf(error.networkResponse.statusCode);
                        VinciLog.e("http failed: " + reason);
                        requestListener.onDaVinciRequestFailed(reason);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if ( mHeadersMap != null ) {
                    return mHeadersMap;
                }
                return super.getHeaders();
            }


        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(mTimeOutMs, mMaxRetries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(jsonObjectRequest);
    }
}

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.hadcn.davinci.http.impl;


import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hadcn.davinci.base.CookiePref;
import cn.hadcn.davinci.base.VinciLog;

/**
 * A request for retrieving a {@link JSONObject} response body at a given URL, allowing for an
 * optional {@link JSONObject} to be passed in as part of the request body.
 */
public class JsonVinciRequest extends JsonRequest<JSONObject> {
    private Map<String, String> mHeadersMap = new HashMap<>();
    private String mCookie = null;
    private boolean isCookieEnabled = false;
    private Context mContext;

    /**
     * Creates a new request.
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param requestBody A {@link String} to post with the request. Null is allowed and
     *   indicates no parameters will be posted along with request.
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonVinciRequest(int method, String url, String requestBody,
                            Listener<JSONObject> listener, ErrorListener errorListener) {
        super(method, url, requestBody, listener,
                errorListener);
    }

    /**
     * Creates a new request.
     * @param url URL to fetch the JSON from
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonVinciRequest(String url, Listener<JSONObject> listener, ErrorListener errorListener) {
        super(Method.GET, url, null, listener, errorListener);
    }

    /**
     * Creates a new request.
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonVinciRequest(int method, String url, Listener<JSONObject> listener, ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
    }

    /**
     * Creates a new request.
     * @param method the HTTP method to use
     * @param url URL to fetch the JSON from
     * @param jsonRequest A {@link JSONObject} to post with the request. Null is allowed and
     *   indicates no parameters will be posted along with request.
     * @param listener Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonVinciRequest(int method, String url, JSONObject jsonRequest,
                            Listener<JSONObject> listener, ErrorListener errorListener) {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                errorListener);
    }

    /**
     * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is
     * <code>null</code>, <code>POST</code> otherwise.
     *
     * @see #JsonVinciRequest(int, String, JSONObject, Listener, ErrorListener)
     */
    public JsonVinciRequest(String url, JSONObject jsonRequest, Listener<JSONObject> listener,
                            ErrorListener errorListener) {
        this(jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest,
                listener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

            if ( isCookieEnabled ) {
                // check is there a planting cookie tag of Set-Cookie
                String header = response.headers.toString();
                VinciLog.d("receive headers = " + header);
                Pattern pattern = Pattern.compile("Set-Cookie.*;");
                Matcher m = pattern.matcher(header);
                if ( m.find() ) {
                    String cookieFromResponse = m.group();
                    VinciLog.d("cookie from server " + cookieFromResponse);
                    //get cookie content, cut header "Set-Cookie:" and tail ";"
                    //and save cookie to memory cache and disk cache
                    mCookie = cookieFromResponse.substring(11, cookieFromResponse.length() - 1);
                    CookiePref.getInstance(mContext).saveCookie(mCookie);
                }
            }

            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    /**
     * enable cookie
     * @param enable enable cookie or not, true, enable; false, disable
     * @param context context, if null, cookie will not be enabled
     */
    public void enableCookie(boolean enable, Context context) {
        isCookieEnabled = enable;
        mContext = context;
        if ( context == null ) {
            isCookieEnabled = false;
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if ( isCookieEnabled ) {
            // if cookie isn't saved in memory cache, get it from disk cache
            if (mCookie == null) {
                mCookie = CookiePref.getInstance(mContext).getCookie();
            }
            if (mCookie != null) {
                mHeadersMap.put("Cookie", mCookie);
            }
        }
        return mHeadersMap;
    }
}

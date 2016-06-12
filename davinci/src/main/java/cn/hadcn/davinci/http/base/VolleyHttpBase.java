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

package cn.hadcn.davinci.http.base;


import java.io.UnsupportedEncodingException;
import java.util.Map;

import cn.hadcn.davinci.volley.AuthFailureError;
import cn.hadcn.davinci.volley.NetworkResponse;
import cn.hadcn.davinci.volley.ParseError;
import cn.hadcn.davinci.volley.Request;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.Response.*;
import cn.hadcn.davinci.volley.VolleyLog;
import cn.hadcn.davinci.volley.toolbox.HttpHeaderParser;

/**
 * A request for retrieving a T type response body at a given URL that also
 * optionally sends along a JSON body in the request specified.
 */
public abstract class VolleyHttpBase extends Request<String> {
    /** Default charset for JSON request. */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    private final Listener<String> mListener;
    private final String mRequestBody;


    public VolleyHttpBase(int method, String url, String requestBody, Listener<String> listener,
                          ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mRequestBody = requestBody;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    /**
     * @deprecated Use {@link #getBodyContentType()}.
     */
    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    /**
     * @deprecated Use {@link #getBody()}.
     */
    @Override
    public byte[] getPostBody() {
        return getBody();
    }

    @Override
    public abstract String getBodyContentType();

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

            return Response.success(jsonString,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    abstract public Map<String, String> getHeaders() throws AuthFailureError;
}

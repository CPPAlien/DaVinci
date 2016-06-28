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

package cn.hadcn.davinci.image.base;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.volley.DefaultRetryPolicy;
import cn.hadcn.davinci.volley.NetworkResponse;
import cn.hadcn.davinci.volley.ParseError;
import cn.hadcn.davinci.volley.Request;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.VolleyLog;
import cn.hadcn.davinci.volley.toolbox.HttpHeaderParser;

/**
 * A canned request for getting an image at a given URL and calling
 * back with a decoded Bitmap.
 */
public class ByteRequest extends Request<ByteBuffer> {
    protected static final String PROTOCOL_CHARSET = "utf-8";

    public static final int DEFAULT_IMAGE_TIMEOUT_MS = 1000;

    public static final int DEFAULT_IMAGE_MAX_RETRIES = 2;

    public static final float DEFAULT_IMAGE_BACKOFF_MULT = 2f;

    private final Response.Listener<ByteBuffer> mListener;

    /** Decoding lock so that we don't decode more than one image at a time (to avoid OOM's) */
    private static final Object sDecodeLock = new Object();

    private final String mRequestBody;

    /**
     * Creates a new image request, decoding to a maximum specified width and
     * height. If both width and height are zero, the image will be decoded to
     * its natural size. If one of the two is nonzero, that dimension will be
     * clamped and the other one will be set to preserve the image's aspect
     * ratio. If both width and height are nonzero, the image will be decoded to
     * be fit in the rectangle of dimensions width x height while keeping its
     * aspect ratio.
     *
     * @param url URL of the image
     * @param listener Listener to receive the decoded bitmap
     * @param errorListener Error listener, or null to ignore errors
     */
    public ByteRequest(int method, String url, String requestBody, Response.Listener<ByteBuffer> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        setRetryPolicy(new DefaultRetryPolicy(DEFAULT_IMAGE_TIMEOUT_MS, DEFAULT_IMAGE_MAX_RETRIES,
                DEFAULT_IMAGE_BACKOFF_MULT));
        mListener = listener;
        mRequestBody = requestBody;
    }

    @Override
    public Priority getPriority() {
        return Priority.LOW;
    }

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VinciLog.e("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    protected Response<ByteBuffer> parseNetworkResponse(NetworkResponse response) {
        // Serialize all decode on a global lock to reduce concurrent heap usage.
        synchronized (sDecodeLock) {
            try {
                byte[] data = response.data;
                ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                if (byteBuffer == null) {
                    return Response.error(new ParseError(response));
                } else {
                    return Response.success(byteBuffer, HttpHeaderParser.parseCacheHeaders(response));
                }
            } catch (OutOfMemoryError e) {
                VolleyLog.e("Caught OOM for %d byte image, url=%s", response.data.length, getUrl());
                return Response.error(new ParseError(e));
            }
        }
    }

    @Override
    protected void deliverResponse(ByteBuffer response) {
        mListener.onResponse(response);
    }
}

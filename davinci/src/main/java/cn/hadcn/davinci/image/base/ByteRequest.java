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

import android.os.Handler;
import android.os.Looper;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.volley.NetworkResponse;
import cn.hadcn.davinci.volley.ParseError;
import cn.hadcn.davinci.volley.Request;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.toolbox.HttpHeaderParser;

/**
 * A canned request for getting an image at a given URL and calling
 * back with a decoded Bitmap.
 */
public class ByteRequest extends Request<ByteBuffer> {
    protected static final String PROTOCOL_CHARSET = "utf-8";

    private final Response.Listener<ByteBuffer> mListener;
    private Response.ProgressListener mProgressListener;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    /** Decoding lock so that we don't decode more than one image at a time (to avoid OOM's) */
    private static final Object sDecodeLock = new Object();

    private final String mRequestBody;


    public ByteRequest(int method, String url, String requestBody, Response.Listener<ByteBuffer> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);

        mListener = listener;
        mRequestBody = requestBody;
    }

    public ByteRequest(int method, String url, String requestBody, Response.Listener<ByteBuffer> listener, Response.ErrorListener errorListener, Response.ProgressListener progressListener) {
        this(method, url, requestBody, listener, errorListener);
        mProgressListener = progressListener;
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
                VinciLog.e("Caught OOM for %d byte image, url=%s", e, response.data.length, getUrl());
                return Response.error(new ParseError(e));
            }
        }
    }

    @Override
    protected void deliverResponse(ByteBuffer response) {
        mListener.onResponse(response);
    }

    @Override
    public void progressUpdate(final int progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mProgressListener.onProgressUpdate(progress);
            }
        });
    }
}

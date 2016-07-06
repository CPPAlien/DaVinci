package cn.hadcn.davinci.other.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import cn.hadcn.davinci.image.base.ByteRequest;
import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.other.OnVinciDownloadListener;
import cn.hadcn.davinci.volley.DefaultRetryPolicy;
import cn.hadcn.davinci.volley.Request;
import cn.hadcn.davinci.volley.RequestQueue;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.VolleyError;

/**
 * VinciDownload
 * Created by 90Chris on 2016/7/5.
 */
public class VinciDownload {
    private RequestQueue mRequestQueue;
    private String mBody;

    public VinciDownload(RequestQueue mRequestQueue) {
        this.mRequestQueue = mRequestQueue;
    }

    public VinciDownload body(String body) {
        mBody = body;
        return this;
    }

    public void download(String url, final OutputStream out, final OnVinciDownloadListener listener) {
        ByteRequest request = new ByteRequest(Request.Method.POST, url, mBody, new Response.Listener<ByteBuffer>() {
            @Override
            public void onResponse(ByteBuffer response) {
                try {
                    out.write(response.array());
                    listener.onVinciDownloadSuccess();
                } catch (IOException e) {
                    VinciLog.e("write out error", e);
                    listener.onVinciDownloadFailed("write file failed");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onVinciDownloadFailed("net failed");
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(4 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(request);
    }

    public interface Listener {

    }
}

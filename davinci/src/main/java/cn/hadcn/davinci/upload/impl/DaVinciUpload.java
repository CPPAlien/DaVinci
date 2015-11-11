package cn.hadcn.davinci.upload.impl;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.File;

import cn.hadcn.davinci.base.VinciLog;
import cn.hadcn.davinci.upload.OnDaVinciUploadListener;

/**
 * DaVinciUpload
 * Created by 90Chris on 2015/11/11.
 */
public class DaVinciUpload {
    RequestQueue mRequestQueue;
    String mContentType;

    public DaVinciUpload(RequestQueue mRequestQueue, String contentType) {
        this.mRequestQueue = mRequestQueue;
        this.mContentType = contentType;
    }

    public void uploadMultiMedia(String uploadUrl, String mediaPath, final OnDaVinciUploadListener listener) {
        File file = new File(mediaPath);
        if ( !file.exists() ) {
            listener.onDaVinciUploadFailed("Upload file is not exists");
            return;
        }
        UploadRequest<JSONObject> uploadRequest = new UploadRequest<>(uploadUrl,file, mContentType,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VinciLog.i("upload response:" + response.toString());
                        listener.onDaVinciUploadSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VinciLog.e(error.toString());
                        listener.onDaVinciUploadFailed(String.valueOf(error.networkResponse.statusCode));
                    }
                });
        mRequestQueue.add(uploadRequest);
    }
}

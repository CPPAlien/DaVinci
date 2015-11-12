package cn.hadcn.davinci.upload.impl;

import com.android.volley.DefaultRetryPolicy;
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
public class VinciUpload {
    RequestQueue mRequestQueue;

    public VinciUpload(RequestQueue mRequestQueue) {
        this.mRequestQueue = mRequestQueue;
    }

    public void uploadMultiMedia(String uploadUrl, String mediaPath, final OnDaVinciUploadListener listener) {
        File file = new File(mediaPath);
        if ( !file.exists() ) {
            VinciLog.e("Upload file is not exists");
            listener.onDaVinciUploadFailed("Upload file is not exists");
            return;
        }
        UploadRequest uploadRequest = new UploadRequest(uploadUrl,file,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VinciLog.i("upload response:" + (response == null ? null : response.toString()));
                        listener.onDaVinciUploadSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String reason = error.networkResponse == null ? null : String.valueOf(error.networkResponse.statusCode);
                        VinciLog.e("upload failed: " + reason);
                        listener.onDaVinciUploadFailed(reason);
                    }
                });
        uploadRequest.setRetryPolicy(new DefaultRetryPolicy(4 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(uploadRequest);
    }
}

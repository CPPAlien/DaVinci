package cn.hadcn.davinci.upload.impl;


import org.json.JSONObject;

import java.io.File;

import cn.hadcn.davinci.base.VinciLog;
import cn.hadcn.davinci.upload.OnDaVinciUploadListener;
import cn.hadcn.davinci.volley.DefaultRetryPolicy;
import cn.hadcn.davinci.volley.RequestQueue;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.VolleyError;

/**
 * DaVinciUpload
 * Created by 90Chris on 2015/11/11.
 */
public class VinciUpload {
    RequestQueue mRequestQueue;
    private String mFilePartName = null;

    public VinciUpload(RequestQueue mRequestQueue) {
        this.mRequestQueue = mRequestQueue;
    }

    /**
     * name of file in form data
     * @param name default is 'file'
     */
    public void filePartName(String name) {
        mFilePartName = name;
    }

    /**
     * upload file to server
     * @param uploadUrl file server url
     * @param filePath  local file path
     * @param listener listener of uploading
     */
    public void uploadFile(String uploadUrl, String filePath, final OnDaVinciUploadListener listener) {
        File file = new File(filePath);
        if ( !file.exists() ) {
            VinciLog.w("Upload file is not exists");
            listener.onDaVinciUploadFailed("Upload file is not exists");
            return;
        }
        UploadRequest uploadRequest = new UploadRequest(uploadUrl,mFilePartName, file,

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

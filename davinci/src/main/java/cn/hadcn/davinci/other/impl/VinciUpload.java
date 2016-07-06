package cn.hadcn.davinci.other.impl;


import org.json.JSONObject;

import java.io.File;

import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.other.OnVinciUploadListener;
import cn.hadcn.davinci.other.request.UploadRequest;
import cn.hadcn.davinci.volley.DefaultRetryPolicy;
import cn.hadcn.davinci.volley.RequestQueue;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.VolleyError;

/**
 * DaVinciUpload
 * Created by 90Chris on 2015/11/11.
 */
public class VinciUpload {
    private RequestQueue mRequestQueue;
    private String mFilePartName = null;
    private String extraName;
    private JSONObject extraObject;

    public VinciUpload(RequestQueue mRequestQueue) {
        this.mRequestQueue = mRequestQueue;
    }

    /**
     * name of file in form data
     * @param name default is 'file'
     */
    public VinciUpload name(String name) {
        mFilePartName = name;
        return this;
    }

    public VinciUpload extra(String name, JSONObject object) {
        extraName = name;
        extraObject = object;
        return this;
    }

    /**
     * upload file to server
     * @param uploadUrl file server url
     * @param filePath  local file path
     * @param listener listener of uploading
     */
    public void upload(String uploadUrl, String filePath, final OnVinciUploadListener listener) {
        File file = new File(filePath);
        if ( !file.exists() ) {
            VinciLog.w("Upload file is not exists");
            listener.onVinciUploadFailed("Upload file is not exists");
            return;
        }
        UploadRequest uploadRequest = new UploadRequest(uploadUrl,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        VinciLog.d("upload response:" + (response == null ? null : response.toString()));
                        listener.onVinciUploadSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String reason = null;
                        if ( error.networkResponse != null ) {
                            reason = "status code : " + String.valueOf(error.networkResponse.statusCode) + ";";
                            byte[] data = error.networkResponse.data;
                            reason += ( data == null ? null : new String(data) );
                        }
                        VinciLog.e("http failed: " + reason);
                        if ( listener != null ) {
                            listener.onVinciUploadFailed(reason);
                        }
                    }
                });
        uploadRequest.setRetryPolicy(new DefaultRetryPolicy(4 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        if ( extraName != null && extraObject != null ) {
            uploadRequest.addExtra(extraName, extraObject);
        }

        uploadRequest.addFile(mFilePartName, file);

        mRequestQueue.add(uploadRequest);
    }
}

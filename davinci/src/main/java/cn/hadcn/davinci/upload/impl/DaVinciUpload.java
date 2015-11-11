package cn.hadcn.davinci.upload.impl;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.File;

import cn.hadcn.davinci.upload.OnDaVinciUploadListener;

/**
 * DaVinciUpload
 * Created by 90Chris on 2015/11/11.
 */
public class DaVinciUpload {
    RequestQueue mRequestQueue;

    public DaVinciUpload(RequestQueue mRequestQueue) {
        this.mRequestQueue = mRequestQueue;
    }

    public void uploadMultiMedia(String uploadUrl, String mediaPath, OnDaVinciUploadListener listener) {
        File file = new File(mediaPath);
        UploadRequest<JSONObject> uploadRequest = new UploadRequest<>(uploadUrl,file,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
    }
}

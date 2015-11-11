package cn.hadcn.davinci.upload;

import org.json.JSONObject;

/**
 * OnDaVinciUploadListener
 * Created by 90Chris on 2015/11/11.
 */
public interface OnDaVinciUploadListener {
    void onDaVinciUploadSuccess(JSONObject response);
    void onDaVinciUploadFailed(String reason);
}

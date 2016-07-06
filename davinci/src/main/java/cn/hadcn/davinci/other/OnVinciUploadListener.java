package cn.hadcn.davinci.other;

import org.json.JSONObject;

/**
 *
 * Created by 90Chris on 2016/7/6.
 */
public interface OnVinciUploadListener {
    void onVinciUploadSuccess(JSONObject response);
    void onVinciUploadFailed(String reason);
}

package cn.hadcn.davinci.http;

import org.json.JSONObject;

/**
 * OnRequestListener
 * Created by 90Chris on 2015/9/10.
 */
public interface OnRequestListener {
    void onSuccess(JSONObject jsonObject);
    void onFailed(String errorInfo);
}

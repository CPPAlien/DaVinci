package cn.hadcn.davinci.http;

import org.json.JSONObject;

/**
 * OnDaVinciRequestListener
 * Created by 90Chris on 2015/9/10.
 */
public interface OnDaVinciRequestListener {
    void onDaVinciRequestSucceed(JSONObject jsonObject);
    void onDaVinciRequestFailed(String errorInfo);
}

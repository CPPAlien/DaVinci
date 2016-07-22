package cn.hadcn.davinci.http;


/**
 * OnDaVinciRequestListener
 * Created by 90Chris on 2015/9/10.
 */
public interface OnDaVinciRequestListener {
    void onDaVinciRequestSuccess(String response);
    void onDaVinciRequestFailed(int code, String reason);
}

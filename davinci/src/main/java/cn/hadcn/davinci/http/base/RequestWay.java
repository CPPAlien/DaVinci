package cn.hadcn.davinci.http.base;

/**
 * RequestMethod
 * Created by 90Chris on 2015/9/10.
 */
public enum RequestWay {
    GET("get"),
    POST("post");

    private String mServerType;
    RequestWay(String type) {
        mServerType = type;
    }
    public String getType () {
        return mServerType;
    }
}

package cn.hadcn.davinci.base;

/**
 * RequestMethod
 * Created by 90Chris on 2015/9/10.
 */
public class RequestMethod {
    public enum Way {
        GET("get"),
        POST("post");

        private String mServerType;
        Way(String type) {
            mServerType = type;
        }
        public String getType () {
            return mServerType;
        }
    }
}

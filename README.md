# DaVinci
Full set solutions of network data transmission; 
The library based on Volley, includes image/gif loading, caching, http request with cookies and so on.

###1, How to Use

use maven to import dependency in gradle

```
repositories{
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.CPPAlien:DaVinci:1.0.9'
}
```

#### Release Notes

##### 1.0.9
1, Add Gif support
Enable Gif, you should add `compile 'pl.droidsonroids.gif:android-gif-drawable:1.1.15'` in your gradle,
If not, downloaded gif will be changed into a normal pic.

##### 1.0.8
1, Change response format from JsonObject to String

##### 1.0.7
1, You can reset your request Content-Type and charset of your request body now by using `request.contentType("xxxx")` and `request.charset("xxx")`
2, Change some expressions of setting, like `addHeaders` to `headers`
3, Get rid of v4 support lib dependency

##### 1.0.6
Use CookieHandler to manage cookies, 

Thanks for Fran Montiel who wrote the PersistentCookieStore(https://gist.github.com/franmontiel/ed12a2295566b7076161)

##### 1.0.5

Add Cookies Management in http Request

If you want to save cookie and use cookie in each requests, 
you can just call DaVinci.with(Context).enableCookie(); method before each requests.

Tips: The Cookies management isn't identical as a browser's. It can only deal with one Set-Cookie header of
a response, and will cover the old cookies. It's not like the browser can set cookies value by cookies' keys.

##### 1.0.4
Add Uploader Function implemented by data-form

###2, Implement GET or POST request
```
DaVinci.with(Context).getHttpRequest()
doGet(String requestUrl, Map<String, Object> params, OnDaVinciRequestListener requestListener)
doPost(String requestUrl, JSONObject postJsonData, OnDaVinciRequestListener requestListener)
doPost(String requestUrl, String postBodyString, OnDaVinciRequestListener requestListener)

public interface OnDaVinciRequestListener {
    void onDaVinciRequestSuccess(String response);
    void onDaVinciRequestFailed(String reason);
}
```

###3, Load images from internet
```
DaVinci.with(Context).getImageLoader().load(imageView, "image url put here");
```

###4, Upload file via form-data
```
DaVinci.with(Context).getUploader().uploadFile(String uploadUrl, String filePath, final OnDaVinciUploadListener listener)

public interface OnDaVinciUploadListener {
    void onDaVinciUploadSuccess(JSONObject response);
    void onDaVinciUploadFailed(String reason);
}
```


#Other
###1, If you don't want to pass Context every time

You can use init before each request, it's better put it in OnCreate method of Application
```
/**
 * @param isEnableDebug if open log print
 * @param debugTag log tag
 * @param context context
 */
DaVinci.init(boolean isEnableDebug, String debugTag, Context context)
```

enable Cookies
```
DaVinci.with(Context).enableCookie();
```
add http request headers
```
getHttpRequest().headers(Map<String, String> headersMap)
```
set http request timeout
```
getHttpRequest().timeOut(int timesOutMs)
```
set http request max retry times
```
getHttpRequest().maxRetries(int maxRetries)
```
set Content-Type(default is application/json) and charset(default is utf-8) of request, charset is optional
```
contentType(String contentType, String charset)
```

limit the max size of image loaded from internet
```
getImageLoader().resize(int maxPix).load(...)
```

### TODO
1, Use PtLog for log printing
2, Change aar to jar
3, Add json parse of response

Thanks for DiskLruCache which powered by Jake Wharton.

https://github.com/JakeWharton/DiskLruCache

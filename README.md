# DaVinci
Easy using network data transmission library based on Volley, includes image loading, memory/disk caching and http request

###1, How to Use

use maven to import dependency in gradle

```
repositories{
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.CPPAlien:DaVinci:1.0.4'
}
```

###2, Implement GET or POST request
```
DaVinci.with(Context).getHttpRequest()
doGet(String requestUrl, Map<String, Object> params, OnDaVinciRequestListener requestListener)
doPost(String requestUrl, JSONObject postJsonData, OnDaVinciRequestListener requestListener)

public interface OnDaVinciRequestListener {
    void onDaVinciRequestSucceed(JSONObject jsonObject);
    void onDaVinciRequestFailed(String errorInfo);
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
###1, If you don't want to pass Context in each calling

You can use
```
DaVinci.init(Context);
```
before each calling, usually you can put it in Application's onCreate method.

###2, Other settings
enable Debuging
```
DaVinci.enableDebug(String tag);
```
add http request headers
```
getHttpRequest().addHeaders(Map<String, String> headersMap)
```
set http request timeout
```
getHttpRequest().setTimesOut(int timesOutMs)
```
set http request max retry times
```
getHttpRequest().setMaxRetries(int maxRetries)
```
limit the max size of image loaded from internet
```
getImageLoader().resize(int maxPix).load(...)
```

Thanks for DiskLruCache which powered by Jake Wharton.

https://github.com/JakeWharton/DiskLruCache

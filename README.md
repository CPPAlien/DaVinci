# DaVinci
Full set of solutions to network data transmission; 
The library is based on Volley, including image loading, memory/disk caching, http request, uploading file, and Cookies

###1, How to Use

use maven to import dependency in gradle

```
repositories{
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.CPPAlien:DaVinci:1.0.5'
}
```

####Release Notes

#####1.0.6
Use CookieHandler to manage cookies, 

Thanks for Fran Montiel who wrote the PersistentCookieStore(https://gist.github.com/franmontiel/ed12a2295566b7076161)

#####1.0.5

Add Cookies Management in http Request

If you want to save cookie and use cookie in each requests, 
you can just call DaVinci.with(Context).enableCookie(); method before each requests.

Tips: The Cookies management isn't identical as a browser's. It can only deal with one Set-Cookie header of
a response, and will cover the old cookies. It's not like the browser can set cookies value by cookies' keys.

#####1.0.4
Add Uploader Function implemented by data-form

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
enable Debuging, tag is printed tag in LogCat.
```
DaVinci.with(Context).enableDebug(String tag);
```
enable Cookies
```
DaVinci.with(Context).enableCookie();
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

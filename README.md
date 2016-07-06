[![](https://jitpack.io/v/CPPAlien/DaVinci.svg)](https://jitpack.io/#CPPAlien/DaVinci)
# DaVinci
基于Volley实现的一款多功能网络库, 目前包括了普通图片和Gif图片的加载显示、图片的二级缓存机制、可以开启Cookie的http请求等功能。

**我们先来一张Glide与DaVinci加载同一张网络上Gif图的对比效果**

<div class='row'>
    <img src='http://7xq276.com2.z0.glb.qiniucdn.com/davinci.gif' width="250px"/>
</div>

[Demo Download](http://7xq276.com2.z0.glb.qiniucdn.com/davinci_demo.apk)

从上面我们可以明显看出，Glide加载一张Gif图比DaVinci明显花更久的时间。并且再看加载后的动画效果，DaVinci加载后的Gif图动画非常流畅，而Glide加载过后的Gif的动画有些显示问题。并且用DaVinci加载图片，你可以定制loading过程的图片，而Glide无法做到。

我们再来看下实现上述功能，两者需要的代码对比。

####DaVinci
```
DaVinci
	.with(this)
	.getImageLoader()
	.load("http://7xlkhg.com2.z0.glb.qiniucdn.com/qbi_cry.gif")
	.into(image1);
```

####Glide
```
Glide
	.with(this)
	.load("http://7xlkhg.com2.z0.glb.qiniucdn.com/qbi_cry.gif")
	.into(image2);
```

实现方式基本差不多，但你要知道本库可不单单只有图片加载功能哦。


###1，特色

1，支持Gif图片，并且做到Gif库可插拔；

2，实现客户端Http请求的Cookie机制，只要调用一个enable方法就搞定；

3，支持内存和本地的二级缓存，让图片加载更加流畅；

4, 支持使用POST方法获得图片；

5, 支持创建多线程池

6, 支持上传、下载功能

###2， 使用方法

用Gradle的方式导入DaVinci库

```
repositories{
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.CPPAlien:DaVinci:1.2.5'
}
```

###3， Get和Post请求
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

###4， 从网络上加载图片
```
DaVinci.with(Context).getImageLoader().load("image url put here").into(imageView);
```

你也可以在into是使用`into(ImageView imageView, int loadingImage, int errorImage)`来设置loading图片，和加载错误时的图片

本库Gif图片加载采用koral--实现的[android-gif-drawable](https://github.com/koral--/android-gif-drawable)，因为此库底层使用C库进行Gif的编解码，所以效率和显示效果方面都比Glide优秀。

开启本库Gif功能，你需要导入`compile 'pl.droidsonroids.gif:android-gif-drawable:1.1.15'`，导入后，加载的图片如果为Gif，则会自动以动图的方式在ImageView里面显示。如果你没有导入该android-gif-drawable库，则Gif图会被当做普通图片处理。

###5，其他用法

* 如果你不想每次在使用`DaVinci.with(Context)`时都传入`Context`，则你可以在所有调用前先`init`一下，以后只要使用`DaVinci.with()`即可。
```
/**
 * @param isEnableDebug if open log print
 * @param debugTag log tag
 * @param context context
 */
DaVinci.init(boolean isEnableDebug, String debugTag, Context context)
```

* 开启Cookie机制，Cookie机制开启后，每次的请求头中都会带有`Cookie`头信息。
```
DaVinci.with(Context).enableCookie();
```
* 设置默认的Content-Type (默认是 `application/json`) 和 charset(默认是 `utf-8`，此项可选)
```
contentType(String contentType, String charset)
```
* 加入请求头
```
getHttpRequest().headers(Map<String, String> headersMap)
```
* 设置请求超时时间
```
getHttpRequest().timeOut(int timesOutMs)
```

* 设置请求的错误尝试次数
```
getHttpRequest().maxRetries(int maxRetries)
```
* 设置加载图片大小，图片长宽按比例缩小为设定的maxpix大小
```
getImageLoader().resize(int maxPix).load(...)
```
**注：设置图片大小有如下限制：1，对Gif无效；2，只能按比例缩小图片，无法放大图片；3，只能在第一次加载时有效（因为图片加载成功后都会缓存到本地，为了效率考虑，后续显示该图片都从缓存中拿取，不再进行大小裁剪）。**

* 使用POST方法加载图片，body中为post方法体
```
getImageLoader().body(XXXX).load(...)
```

Thanks for DiskLruCache which powered by Jake Wharton.

https://github.com/JakeWharton/DiskLruCache

Thanks for Fran Montiel who wrote the PersistentCookieStore(https://gist.github.com/franmontiel/ed12a2295566b7076161)

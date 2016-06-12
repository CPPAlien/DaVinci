package cn.hadcn.davinci.image;

import android.content.Context;
import android.widget.ImageView;


import java.io.File;
import java.nio.ByteBuffer;

import cn.hadcn.davinci.image.base.ImageLoader;
import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.volley.RequestQueue;


/**
 * DaImageLoader
 * Created by 90Chris on 2015/9/11.
 */
public class VinciImageLoader {
    private String mCacheDir = null;
    private ImageLoader.ImageCache mImageCache;
    private ImageLoader mImageLoader;
    private Context mContext;
    private int mMaxSize = 0;
    private final static int CACHE_SIZE = 1024 * 1024 * 20;
    private ReadImageTask mReadImageTask;

    public VinciImageLoader(Context context, RequestQueue requestQueue) {
        mCacheDir = getDiskCacheDir(context);
        mContext = context;
        mImageCache = new DiskLruImageCache(mCacheDir, CACHE_SIZE);
        mImageLoader = new ImageLoader(requestQueue, mImageCache);
    }

    private String getDiskCacheDir(Context context) {
        final String CACHE_DIR_NAME = "imgCache";
        final String cachePath = context.getCacheDir().getPath();
        return cachePath + File.separator + CACHE_DIR_NAME;
    }

    public String getAbsolutePath( String fileName ) {
        return mCacheDir + File.separator + Util.generateKey(fileName) + ".0";
    }

    public ByteBuffer getImage(String name) {
        try {
            return mImageCache.getBitmap(name);
        } catch (NullPointerException e) {
            VinciLog.w("Get Image failed, name = " + name);
            return null;
        }
    }

    public void putImage(String name, ByteBuffer bitmap) {
        try {
            mImageCache.putBitmap(name, bitmap);
        } catch (NullPointerException e) {
            VinciLog.e("Put Image failed, name cannot be null", e);
        }
    }

    public VinciImageLoader load(String url) {
        mReadImageTask = new ReadImageTask(mContext, mImageCache, mImageLoader, url);
        return this;
    }

    public void into(ImageView imageView) {
        into(imageView, 0, 0);
    }

    public void into(ImageView imageView, int loadingImage, int errorImage) {
        mReadImageTask.setView(imageView, loadingImage, errorImage);
        mReadImageTask.setSize(mMaxSize);
        mReadImageTask.execute();
    }

    /**
     * limit the max size of an image will be displayed, height and width are both shorter than maxPix
     * @param maxPix max pixels of height and width
     * @return DaImageLoader instance
     */
    public VinciImageLoader resize(int maxPix) {
        mMaxSize = maxPix;
        return this;
    }
}

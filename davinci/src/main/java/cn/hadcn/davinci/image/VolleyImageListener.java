package cn.hadcn.davinci.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;


import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import cn.hadcn.davinci.image.base.ImageLoader;
import cn.hadcn.davinci.image.base.Util;
import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.volley.VolleyError;

/**
 *
 * Created by 90Chris on 2016/5/5.
 */
public class VolleyImageListener implements ImageLoader.ImageListener {
    private ImageView mImageView;
    private int mLoadingImage;
    private int mErrorImage;
    private int mMaxSize = 0;
    private int mKeyMode = 0;
    private Context mContext;
    private VinciImageLoader.ImageCache mImageCache;

    protected VolleyImageListener(Context context, ImageView imageView, VinciImageLoader.ImageCache imageCache) {
        this.mImageView = imageView;
        mContext = context;
        mImageCache = imageCache;
    }

    protected void setMaxSize(int size, int mode) {
        mMaxSize = size;
        mKeyMode = mode;
    }

    protected void setDefaultImage(int loadingImage, int errorImage) {
        mLoadingImage = loadingImage;
        mErrorImage = errorImage;
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        ByteBuffer byteBuffer = response.getBitmap();
        if ( null != byteBuffer ) {
            byte[] bytes = byteBuffer.array();
            VinciLog.d("Image loaded success, and saved in cache, url = " + response.getRequestUrl());

            // if it's gif, show as gif, and save in cache
            if ( Util.doGif(mImageView, bytes) ) {
                cacheImage(response.getRequestUrl(), bytes);
                return;
            }

            // deal with bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            int bHeight = bitmap.getHeight();
            int bWidth = bitmap.getWidth();
            if ( mMaxSize > 0 ) {
                if ( bWidth > bHeight ) {
                    int otherSize = (mMaxSize * bHeight) / bWidth;
                    bitmap = Bitmap.createScaledBitmap(bitmap, mMaxSize, otherSize, false);
                } else {
                    int otherSize = (mMaxSize * bWidth) / bHeight;
                    bitmap = Bitmap.createScaledBitmap(bitmap, otherSize, mMaxSize, false);
                }
            }
            mImageView.setImageBitmap(bitmap);

            // cache the image that was fetched.
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            cacheImage(response.getRequestUrl(), byteArray);
        } else {
            mImageView.setImageDrawable(mContext.getResources().getDrawable(mLoadingImage));
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mImageView.setImageDrawable(mContext.getResources().getDrawable(mErrorImage));
    }

    private void cacheImage(String url, byte[] data) {
        String rawKey = url;
        if ( mKeyMode != 0 && mMaxSize != 0 ) rawKey += mMaxSize;

        String key = Util.generateKey(rawKey);
        if ( key.isEmpty() ) return;
        mImageCache.putBitmap(key, data);
    }
}
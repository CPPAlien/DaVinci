package cn.hadcn.davinci.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;


import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import cn.hadcn.davinci.base.ImageLoader;
import cn.hadcn.davinci.base.VinciLog;
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
    private Context mContext;
    private ImageLoader.ImageCache mImageCache;

    protected VolleyImageListener(Context context, ImageView imageView, ImageLoader.ImageCache imageCache) {
        this.mImageView = imageView;
        mContext = context;
        mImageCache = imageCache;
    }

    protected void setMaxSize(int size) {
        mMaxSize = size;
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
                cacheImage(response.getRequestUrl(), ByteBuffer.wrap(bytes));
                return;
            }

            // deal with bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            int bHeight = bitmap.getHeight();
            int bWidth = bitmap.getWidth();
            int scaleWidth = 0;
            int scaleHeight = 0;
            if ( mMaxSize > 0 ) {
                if ( bWidth > mMaxSize) {
                    scaleWidth = mMaxSize;
                    scaleHeight = (scaleWidth * bHeight) / bWidth;
                    bitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, false);
                }
                if ( scaleHeight > mMaxSize) {
                    scaleHeight = mMaxSize;
                    scaleWidth = ( scaleHeight * bWidth ) / bHeight;
                    bitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, false);
                }
            }
            mImageView.setImageBitmap(bitmap);

            // cache the image that was fetched.
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            cacheImage(response.getRequestUrl(), ByteBuffer.wrap(byteArray));
        } else {
            mImageView.setImageDrawable(mContext.getResources().getDrawable(mLoadingImage));
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mImageView.setImageDrawable(mContext.getResources().getDrawable(mErrorImage));
    }

    private void cacheImage(String url, ByteBuffer byteBuffer) {
        mImageCache.putBitmap(url, byteBuffer);
    }
}

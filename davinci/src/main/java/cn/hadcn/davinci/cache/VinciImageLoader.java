package cn.hadcn.davinci.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import java.io.File;

import cn.hadcn.davinci.R;


/**
 * DaImageLoader
 * Created by 90Chris on 2015/9/11.
 */
public class VinciImageLoader {
    private final int DEFAULT_IMAGE_LOADING = R.drawable.image_loading;
    private final int DEFAULT_IMAGE_ERROR = R.drawable.image_load_error;
    private String mCacheDir = null;
    private ImageLoader.ImageCache mImageCache;
    private ImageLoader mImageLoader;
    private Context mContext;
    private int mMaxSize = 0;

    public VinciImageLoader(Context context, RequestQueue requestQueue) {
        mCacheDir = getDiskCacheDir(context);
        mContext = context;
        mImageCache= new DiskLruImageCache(mCacheDir,
                1024 * 1024 * 20, Bitmap.CompressFormat.PNG, 30);
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

    public Bitmap getBitmap(String name) {
        try {
            return mImageCache.getBitmap(name);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Disk Cache Not initialized");
        }
    }

    public void putBitmap(String name, Bitmap bitmap) {
        try {
            mImageCache.putBitmap(name, bitmap);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Disk Cache Not initialized");
        }
    }

    public void load(ImageView imageView, String url) {
        new ReadBitmapTask(imageView, url).execute();
    }

    public void load(ImageView imageView, String url, int loading_image, int error_image) {
        new ReadBitmapTask(imageView, url, loading_image, error_image).execute();
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

    private class ReadBitmapTask extends AsyncTask<String, Integer, Bitmap> {
        private ImageView mImageView;
        private String mImageUrl;
        private int mLoadingImage = DEFAULT_IMAGE_LOADING;
        private int mErrorImage = DEFAULT_IMAGE_ERROR;

        public ReadBitmapTask(ImageView imageView, String imageUrl, int image_loading, int image_error) {
            mImageView = imageView;
            mImageUrl = imageUrl;
            mLoadingImage = image_loading;
            mErrorImage = image_error;
        }

        public ReadBitmapTask( ImageView imageView, String imageUrl ) {
            mImageView = imageView;
            mImageUrl = imageUrl;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return mImageCache.getBitmap( mImageUrl );
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if ( bitmap != null ) {
                mImageView.setImageBitmap(bitmap);
            } else if ( mImageUrl == null || !mImageUrl.contains("http")) {
                mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, mErrorImage));
            } else {
                mImageLoader.get(mImageUrl, new VolleyImageListener(mImageView, mLoadingImage, mErrorImage));
            }
        }
    }

    private class VolleyImageListener implements ImageLoader.ImageListener {
        private ImageView mImageView;
        private int mLoadingImage = DEFAULT_IMAGE_LOADING;
        private int mErrorImage = DEFAULT_IMAGE_ERROR;

        public VolleyImageListener(ImageView mImageView, int loadingImage, int errorImage) {
            mLoadingImage = loadingImage;
            mErrorImage = errorImage;
            this.mImageView = mImageView;
            mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, mLoadingImage));
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            Bitmap bitmap = response.getBitmap();
            if ( null != bitmap ) {
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

                mImageCache.putBitmap(response.getRequestUrl(), bitmap);
                mImageView.setImageBitmap(bitmap);
            } else {
                mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, mLoadingImage));
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            mImageView.setImageDrawable(ContextCompat.getDrawable(mContext, mErrorImage));
        }
    }
}

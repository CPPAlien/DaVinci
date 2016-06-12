package cn.hadcn.davinci.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.nio.ByteBuffer;

import cn.hadcn.davinci.R;
import cn.hadcn.davinci.image.base.ImageLoader;
import cn.hadcn.davinci.log.VinciLog;

/**
 * read image from any where
 * Created by 90Chris on 2016/5/5.
 */
public class ReadImageTask {
    private final int DEFAULT_IMAGE_LOADING = R.drawable.image_loading;
    private final int DEFAULT_IMAGE_ERROR = R.drawable.image_load_error;

    private ImageView mImageView;
    private String mImageUrl;
    private int mLoadingImage = DEFAULT_IMAGE_LOADING;
    private int mErrorImage = DEFAULT_IMAGE_ERROR;
    private ImageLoader.ImageCache mImageCache;
    private ImageLoader mImageLoader;
    private Context mContext;
    private int mMaxSize;

    public ReadImageTask(Context context, ImageLoader.ImageCache imageCache, ImageLoader imageLoader, String imageUrl) {
        mImageUrl = imageUrl;
        mImageCache = imageCache;
        mImageLoader = imageLoader;
        mContext = context;
    }

    public final void execute() {
        if ( mImageUrl == null || mImageUrl.isEmpty() ) {
            mImageView.setImageDrawable(mContext.getResources().getDrawable(mErrorImage));
            return;
        }
        ByteBuffer byteBuffer = mImageCache.getBitmap(mImageUrl);
        if ( byteBuffer != null ) {
            byte[] bytes = byteBuffer.array();
            VinciLog.d("Load image from cache, url = " + mImageUrl);

            // if it's gif, show as gif
            if ( Util.doGif(mImageView, bytes) ) return;

            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            mImageView.setImageBitmap(image);
        } else if ( mImageUrl.startsWith("http") ) {
            VolleyImageListener listener = new VolleyImageListener(mContext, mImageView, mImageCache);
            listener.setDefaultImage(mLoadingImage, mErrorImage);
            listener.setMaxSize(mMaxSize);
            VinciLog.d("Load image from web, url = " + mImageUrl );
            mImageLoader.get(mImageUrl, listener);
        } else {
            mImageView.setImageDrawable(mContext.getResources().getDrawable(mErrorImage));
        }
    }

    protected void setView(ImageView imageView, int image_loading, int image_error) {
        mImageView = imageView;
        if ( image_loading != 0 ) mLoadingImage = image_loading;
        if ( image_error != 0 ) mErrorImage = image_error;
    }

    protected void setView(ImageView imageView) {
        mImageView = imageView;
    }

    protected void setSize(int size) {
        mMaxSize = size;
    }
}

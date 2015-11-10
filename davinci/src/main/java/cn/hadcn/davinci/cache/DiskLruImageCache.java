package cn.hadcn.davinci.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.android.volley.toolbox.ImageLoader.ImageCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.hadcn.davinci.base.VinciLog;


/**
 * Implementation of DiskLruCache by Jake Wharton
 * modified 90Chris
 */
public class DiskLruImageCache implements ImageCache {
    private DiskLruCache mDiskCache;
    private BitmapLruImageCache mMemoryCache;
    private CompressFormat mCompressFormat = CompressFormat.JPEG;
    private static int IO_BUFFER_SIZE = 8 * 1024;
    private int mCompressQuality = 70;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    public DiskLruImageCache(Context context, String uniqueName, int diskCacheSize,
                             CompressFormat compressFormat, int quality) {
        try {
                final File diskCacheDir = getDiskCacheDir(context, uniqueName);
                mMemoryCache = new BitmapLruImageCache(diskCacheSize);
                mDiskCache = DiskLruCache.open( diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize );
                mCompressFormat = compressFormat;
                mCompressQuality = quality;
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor ) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream( 0 ), IO_BUFFER_SIZE );
            return bitmap.compress( mCompressFormat, mCompressQuality, out );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    @Override
    public void putBitmap( String key, Bitmap data ) {
        mMemoryCache.putBitmap(createKey(key), data);
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( createKey(key) );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {               
                mDiskCache.flush();
                editor.commit();
                VinciLog.d("image put on disk cache " + key);
            } else {
                editor.abort();
                VinciLog.d("ERROR on: image put on disk cache " + key);
            }   
        } catch (IOException e) {
            VinciLog.d("ERROR on: image put on disk cache " + key);
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }           
        }
    }

    @Override
    public Bitmap getBitmap( String key ) {
        Bitmap bitmap = mMemoryCache.getBitmap(createKey(key));
        if ( bitmap != null ) {
            return bitmap;
        }

        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( createKey(key) );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn = new BufferedInputStream( in, IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream(buffIn);
                mMemoryCache.putBitmap(createKey(key), bitmap);
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }
        VinciLog.d(bitmap == null ? "bitmap is null" : "image read from disk " + key);

        return bitmap;
    }



    public boolean containsKey( String key ) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }
    /**
     * Creates a unique cache key based on a url value
     * @param url
     * 		url to be used in key creation
     * @return
     * 		cache key value
     */
    private String createKey(String url){
        String regEx = "[^a-z0-9_-]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(url);
        String key = m.replaceAll("").trim();
        int length = key.length();
        if ( length <= 120 ) {  //limited by DisLruCache
            return key;
        } else {
            return key.substring(0, 120);
        }
    }


    public void clearCache() {
        VinciLog.d("disk cache CLEARED");
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }
}

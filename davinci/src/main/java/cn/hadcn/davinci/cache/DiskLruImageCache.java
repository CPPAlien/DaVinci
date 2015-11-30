package cn.hadcn.davinci.cache;

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

import cn.hadcn.davinci.base.VinciLog;


/**
 * Implementation of DiskLruCache by Jake Wharton
 * modified by 90Chris
 */
public class DiskLruImageCache implements ImageCache {
    private DiskLruCache mDiskCache;
    private BitmapLruImageCache mMemoryCache;
    private CompressFormat mCompressFormat = CompressFormat.JPEG;
    private static int IO_BUFFER_SIZE = 8 * 1024;
    private int mCompressQuality = 70;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    public DiskLruImageCache( String cachePath, int diskCacheSize,
                             CompressFormat compressFormat, int quality) {
        try {
                final File diskCacheDir = new File(cachePath);;
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

    @Override
    public void putBitmap( String name, Bitmap data ) {
        String key = Util.generateKey(name);
        mMemoryCache.putBitmap(key, data);
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( key );
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
    public Bitmap getBitmap( String name ) {
        String key = Util.generateKey(name);
        Bitmap bitmap = mMemoryCache.getBitmap(key);
        if ( bitmap != null ) {
            return bitmap;
        }

        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream( 0 );
            if ( in != null ) {
                final BufferedInputStream buffIn = new BufferedInputStream( in, IO_BUFFER_SIZE );
                bitmap = BitmapFactory.decodeStream(buffIn);
                mMemoryCache.putBitmap(key, bitmap);
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

package cn.hadcn.davinci.image.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.hadcn.davinci.image.VinciImageLoader;
import cn.hadcn.davinci.image.base.ImageEntity;
import cn.hadcn.davinci.image.base.Util;
import cn.hadcn.davinci.log.VinciLog;


/**
 * Implementation of DiskLruCache by Jake Wharton
 * modified by 90Chris
 */
public class DiskLruImageCache implements VinciImageLoader.ImageCache {
    private DiskLruCache mDiskCache;
    private LruImageCache mMemoryCache;
    private static int IO_BUFFER_SIZE = 8 * 1024;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 8);

    public DiskLruImageCache( String cachePath, int diskCacheSize ) {
        try {
                final File diskCacheDir = new File(cachePath);
                mMemoryCache = new LruImageCache(maxMemory);
                mDiskCache = DiskLruCache.open( diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize );
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void writeBitmapToFile(byte[] data, DiskLruCache.Editor editor ) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream(0), IO_BUFFER_SIZE );
            out.write(data);
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    @Override
    public void putBitmap( String key, byte[] data ) {
        // save to memory cache first
        saveToMemory(key, data);

        // save to disk cache
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( key );
            if ( editor == null ) {
                return;
            }
            writeBitmapToFile( data, editor);
            mDiskCache.flush();
            editor.commit();
        } catch (IOException e) {
            VinciLog.e("Image put on disk cache failed, key = " + key, e);
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }           
        }
    }

    @Override
    public ImageEntity getBitmap(String key) {
        ImageEntity imageEntity = mMemoryCache.getMemCache(key);

        if ( imageEntity != null ) {
            return imageEntity;
        }

        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            if ( snapshot == null ) {
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if ( in != null ) {
                final BufferedInputStream buffIn = new BufferedInputStream(in, IO_BUFFER_SIZE);
                int size = buffIn.available();
                byte[] bytes = new byte[size];
                if ( buffIn.read(bytes) == -1) return null;

                imageEntity = saveToMemory(key, bytes);
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return imageEntity;
    }

    private ImageEntity saveToMemory(String key, byte[] data) {
        ImageEntity entity;

        if ( Util.isGif(data) ) {
            entity = new ImageEntity(data);
        } else {
            Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
            entity = new ImageEntity(image);
        }

        mMemoryCache.putMemCache(key, entity);
        return entity;
    }

    public void clearCache() {
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

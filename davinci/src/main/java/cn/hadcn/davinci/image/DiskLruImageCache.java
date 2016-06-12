package cn.hadcn.davinci.image;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import cn.hadcn.davinci.base.ImageLoader;
import cn.hadcn.davinci.base.VinciLog;


/**
 * Implementation of DiskLruCache by Jake Wharton
 * modified by 90Chris
 */
public class DiskLruImageCache implements ImageLoader.ImageCache {
    private DiskLruCache mDiskCache;
    private LruImageCache mMemoryCache;
    private static int IO_BUFFER_SIZE = 8 * 1024;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;

    public DiskLruImageCache( String cachePath, int diskCacheSize ) {
        try {
                final File diskCacheDir = new File(cachePath);
                mMemoryCache = new LruImageCache(diskCacheSize);
                mDiskCache = DiskLruCache.open( diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize );
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void writeBitmapToFile(ByteBuffer bitmap, DiskLruCache.Editor editor ) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream(0), IO_BUFFER_SIZE );
            out.write(bitmap.array());
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    @Override
    public void putBitmap( String name, ByteBuffer data ) {
        String key = Util.generateKey(name);
        if ( key.isEmpty() ) return;
        mMemoryCache.putMemCache(key, data);
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
    public ByteBuffer getBitmap(String name) {
        String key = Util.generateKey(name);
        if ( key.isEmpty() ) return null;
        ByteBuffer bitmap = mMemoryCache.getMemCache(key);
        if ( bitmap != null ) {
            return bitmap;
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
                bitmap = ByteBuffer.wrap(bytes);
                mMemoryCache.putMemCache(key, bitmap);
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return bitmap;
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

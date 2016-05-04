package cn.hadcn.davinci.cache;

import java.nio.ByteBuffer;

import cn.hadcn.davinci.base.ImageLoader;
import cn.hadcn.davinci.base.VinciLog;


/**
 * based on LruCache
 * @author 90Chris
 */
public class BitmapLruImageCache extends LruCache<String, ByteBuffer> implements ImageLoader.ImageCache {
	
	public BitmapLruImageCache(int maxSize) {
		super(maxSize);
	}
	
	@Override
	protected int sizeOf(String key, ByteBuffer value) {
		return value.capacity();
	}
	
	@Override
	public ByteBuffer getBitmap(String url) {
		VinciLog.d(url + " Retrieved item from Mem Cache");
		return get(url);
	}

	@Override
	public void putBitmap(String url, ByteBuffer bitmap) {
		VinciLog.d(url + " Added item to Mem Cache");
		put(url, bitmap);
	}
}

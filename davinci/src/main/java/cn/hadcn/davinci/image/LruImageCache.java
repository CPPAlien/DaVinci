package cn.hadcn.davinci.image;

import java.nio.ByteBuffer;

import cn.hadcn.davinci.base.ImageLoader;


/**
 * based on LruCache
 * @author 90Chris
 */
public class LruImageCache extends LruCache<String, ByteBuffer> implements ImageLoader.ImageCache {
	
	public LruImageCache(int maxSize) {
		super(maxSize);
	}
	
	@Override
	protected int sizeOf(String key, ByteBuffer value) {
		return value.capacity();
	}
	
	@Override
	public ByteBuffer getBitmap(String url) {
		return get(url);
	}

	@Override
	public void putBitmap(String url, ByteBuffer bitmap) {
		put(url, bitmap);
	}
}

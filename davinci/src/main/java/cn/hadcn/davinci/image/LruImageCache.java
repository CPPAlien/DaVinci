package cn.hadcn.davinci.image;

import java.nio.ByteBuffer;


/**
 * memory cache
 * @author 90Chris
 */
public class LruImageCache extends LruCache<String, ByteBuffer> {
	
	public LruImageCache(int maxSize) {
		super(maxSize);
	}
	
	@Override
	protected int sizeOf(String key, ByteBuffer value) {
		return value.capacity();
	}
	
	public ByteBuffer getMemCache(String url) {
		return get(url);
	}

	public void putMemCache(String url, ByteBuffer bitmap) {
		put(url, bitmap);
	}
}

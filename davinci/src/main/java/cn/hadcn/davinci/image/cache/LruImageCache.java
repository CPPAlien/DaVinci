package cn.hadcn.davinci.image.cache;

import cn.hadcn.davinci.image.base.ImageEntity;


/**
 * memory cache
 * @author 90Chris
 */
public class LruImageCache extends LruCache<String, ImageEntity> {
	
	public LruImageCache(int maxSize) {
		super(maxSize);
	}
	
	@Override
	protected int sizeOf(String key, ImageEntity value) {
		return value.getSize();
	}
	
	public ImageEntity getMemCache(String url) {
		return get(url);
	}

	public void putMemCache(String url, ImageEntity bitmap) {
		put(url, bitmap);
	}
}

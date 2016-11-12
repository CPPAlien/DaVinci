package cn.hadcn.davinci.image.cache;

import cn.hadcn.davinci.image.base.ImageEntity;


/**
 * memory cache
 * @author 90Chris
 */
class LruImageCache extends LruCache<String, ImageEntity> {
	
    LruImageCache(int maxSize) {
		super(maxSize);
	}
	
	@Override
	protected int sizeOf(String key, ImageEntity value) {
		return value.getSize() + 1024;  //多加1024 bytes，包入整个对象大小，宁多勿少
	}
	
    ImageEntity getMemCache(String url) {
		return get(url);
	}

    void putMemCache(String url, ImageEntity bitmap) {
		put(url, bitmap);
	}
}

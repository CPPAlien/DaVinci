package cn.hadcn.davinci.image.base;

import android.graphics.Bitmap;

/**
 *
 * Created by 90Chris on 2016/7/20.
 */
public class ImageEntity {
    private Bitmap bitmap;
    private byte[] bytes;
    private boolean isGif;
    private int size;

    public ImageEntity(byte[] bytes) {
        this.bytes = bytes;
        this.isGif = true;
        this.size = bytes.length;
    }

    public ImageEntity(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.isGif = false;
        this.size = bitmap.getRowBytes();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean isGif() {
        return isGif;
    }

    public int getSize() {
        return size;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        bitmap.recycle();
    }
}

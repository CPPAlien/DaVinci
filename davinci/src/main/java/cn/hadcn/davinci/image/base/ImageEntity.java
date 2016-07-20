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

    public ImageEntity(Builder builder) {
        bitmap = builder.bitmap;
        bytes = builder.bytes;
        isGif = builder.isGif;
        size = builder.size;
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

    public static class Builder {
        private Bitmap bitmap;
        private byte[] bytes;
        private boolean isGif;
        private int size;

        public Builder(int size) {
            this.size = size;
        }

        public Builder bitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public Builder bytes(byte[] bytes) {
            this.bytes = bytes;
            return this;
        }

        public Builder isGif(boolean isGif) {
            this.isGif = isGif;
            return this;
        }

        public ImageEntity build() {
            return new ImageEntity(this);
        }
    }


}

package com.df.entry;

import android.graphics.Bitmap;

/**
 * Created by 岩 on 13-10-31.
 */
public class OutsidePhotoEntity {
    private Bitmap bitmap;
    private String imageFileName;
    private int part;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }
}

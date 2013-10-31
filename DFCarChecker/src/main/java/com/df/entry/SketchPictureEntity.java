package com.df.entry;

import android.graphics.Bitmap;

/**
 * Created by 岩 on 13-10-31.
 */
public class SketchPictureEntity {
    private Bitmap bitmap;
    private String imageFileName;

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
}

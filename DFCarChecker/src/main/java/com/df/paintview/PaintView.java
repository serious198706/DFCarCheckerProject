package com.df.paintview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.df.entry.PhotoEntity;
import com.df.entry.PosEntity;

import java.util.List;

/**
 * Created by 岩 on 13-11-4.
 */
public abstract class PaintView extends ImageView {

    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public abstract void onDraw(Canvas canvas);

    public abstract void cancel();
    public abstract void undo();
    public abstract void redo();
    public abstract void clear();
    public abstract PosEntity getPosEntity();
    public abstract List<PhotoEntity> getPhotoEntities();
    public abstract List<PhotoEntity> getPhotoEntities(String sight);
    public abstract long getCurrentTimeMillis();
    public abstract String getGroup();
}

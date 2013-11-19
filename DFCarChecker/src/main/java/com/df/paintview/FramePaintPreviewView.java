package com.df.paintview;

/**
 * Created by 岩 on 13-9-26.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.df.dfcarchecker.CarCheck.CarCheckFrameFragment;
import com.df.dfcarchecker.R;
import com.df.entry.PosEntity;

import java.util.List;

public class FramePaintPreviewView extends ImageView {

    private int currentType;
    private boolean move;
    private List<PosEntity> data;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    public FramePaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public FramePaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public FramePaintPreviewView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<PosEntity> entities) {
        this.bitmap = bitmap;

        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.damage);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

    public void setType(int type) {
        this.currentType = type;
    }

    private void paint(Canvas canvas) {
        for (PosEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(PosEntity entity, Canvas canvas) {
        canvas.drawBitmap(colorDiffBitmap, entity.getStartX(), entity.getStartY(), null);
    }

    public PosEntity getPosEntity(){
        if(data.isEmpty()){
            return null;
        }
        return data.get(data.size()-1);
    }

    public void setPosEntities(List<PosEntity> entities) {
        data = entities;
    }
}


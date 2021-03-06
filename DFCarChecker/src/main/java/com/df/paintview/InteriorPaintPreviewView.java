package com.df.paintview;

/**
 * Created by 岩 on 13-9-26.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.df.dfcarchecker.CarCheck.CarCheckExteriorActivity;
import com.df.entry.PosEntity;
import com.df.service.Common;

import java.util.List;

public class InteriorPaintPreviewView extends ImageView {

    private int currentType;
    private boolean move;
    private List<PosEntity> data;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    public InteriorPaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public InteriorPaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public InteriorPaintPreviewView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<PosEntity> entities) {
        this.bitmap = bitmap;
        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //data = CarCheckInteriorActivity.posEntities;
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

    public void setType(int type) {
        this.currentType = type;
    }

    private Paint getPaint(int type) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setAlpha(0x80);//半透明

        // 根据当前类型决定笔触的颜色
        paint.setColor(type == Common.DIRTY ? Color.RED : Color.BLACK);
        paint.setAlpha(0x80);   //80%透明
        paint.setStyle(Paint.Style.STROKE); // 线类型填充
        paint.setStrokeWidth(4);  // 笔触粗细

        return paint;
    }

    private void paint(Canvas canvas) {
        for (PosEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(PosEntity entity, Canvas canvas) {
        RectF rectF = null;

        // 如果Rect的right < left，或者bottom < top，则会画不出矩形
        // 为了修正这个，需要做点处理

        // 右下
        if(entity.getStartX() < entity.getEndX() &&
                entity.getStartY() < entity.getEndY()) {
            rectF = new RectF(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY());
        }
        // 右上
        else if(entity.getStartX() < entity.getEndX() &&
                entity.getStartY() > entity.getEndY()) {
            rectF = new RectF(entity.getStartX(), entity.getEndY(), entity.getEndX(), entity.getStartY());
        }
        // 左下
        else if(entity.getStartX() > entity.getEndX() &&
                entity.getStartY() < entity.getEndY()) {
            rectF = new RectF(entity.getEndX(), entity.getStartY(), entity.getStartX(), entity.getEndY());
        }
        // 左上
        else if(entity.getStartX() > entity.getEndX() &&
                entity.getStartY() > entity.getEndY()) {
            rectF = new RectF(entity.getEndX(), entity.getEndY(), entity.getStartX(), entity.getStartY());
        }
        // 重合或者默认
        else {
            rectF = new RectF(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY());
        }

        canvas.drawRect(rectF, getPaint(entity.getType()));
    }
}


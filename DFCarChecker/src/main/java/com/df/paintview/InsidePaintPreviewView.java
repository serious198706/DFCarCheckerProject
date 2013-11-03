package com.df.paintview;

/**
 * Created by 岩 on 13-9-26.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.df.dfcarchecker.CarCheckInsideActivity;
import com.df.dfcarchecker.CarCheckOutsideActivity;
import com.df.entry.FaultPhotoEntity;
import com.df.service.Common;

import java.util.ArrayList;
import java.util.List;

public class InsidePaintPreviewView extends ImageView {

    private int currentType;
    private boolean move;
    private List<FaultPhotoEntity> data;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    public InsidePaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public InsidePaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public InsidePaintPreviewView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<FaultPhotoEntity> entities) {
        this.bitmap = bitmap;
        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        data = CarCheckInsideActivity.posEntities;
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
        for (FaultPhotoEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(FaultPhotoEntity entity, Canvas canvas) {
        canvas.drawLine(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY(), getPaint(entity.getType()));
    }

    public FaultPhotoEntity getPosEntity(){
        if(data.isEmpty()){
            return null;
        }
        return data.get(data.size()-1);
    }

    private void HandelPosEntitiesDueToDifferentResolution() {
        data.clear();

        for(FaultPhotoEntity faultPhotoEntity : CarCheckOutsideActivity.posEntities) {
            // 因为不能对原entities做修改，所以此处要做些特殊处理，采用值传递方式
            FaultPhotoEntity temp = new FaultPhotoEntity(faultPhotoEntity.getType());
            temp.setStart(faultPhotoEntity.getStartX(), faultPhotoEntity.getStartY());
            temp.setEnd(faultPhotoEntity.getEndX(), faultPhotoEntity.getEndY());

            // 这些max要乘以2的原因是，在后面的getStartX()等方法调用时，max是按照大图的max来计算的
            temp.setMaxX(max_x * 2);
            temp.setMaxY(max_y * 2);
            data.add(temp);
        }

        for(FaultPhotoEntity faultPhotoEntity : data) {
            faultPhotoEntity.setStart(faultPhotoEntity.getStartX() / 2, faultPhotoEntity.getStartY() / 2);
            faultPhotoEntity.setEnd(faultPhotoEntity.getEndX() / 2, faultPhotoEntity.getEndY() / 2);
        }
    }
}


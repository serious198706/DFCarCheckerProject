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
import android.util.AttributeSet;
import android.widget.ImageView;

import com.df.dfcarchecker.CarCheckOutsideActivity;
import com.df.dfcarchecker.R;
import com.df.service.Common;
import com.df.service.PosEntity;

import java.util.ArrayList;
import java.util.List;

public class InsidePaintPreviewView extends ImageView {

    private int currentType;
    private boolean move;
    private List<PosEntity> data;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    public InsidePaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public InsidePaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InsidePaintPreviewView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_base_image);
        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        data = new ArrayList<PosEntity>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        HandelPosEntitiesDueToDifferentResolution();
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
        canvas.drawLine(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY(), getPaint(entity.getType()));
    }

    public PosEntity getPosEntity(){
        if(data.isEmpty()){
            return null;
        }
        return data.get(data.size()-1);
    }

    private void HandelPosEntitiesDueToDifferentResolution() {
        data.clear();

        for(PosEntity posEntity : CarCheckOutsideActivity.posEntities) {
            // 因为不能对原entities做修改，所以此处要做些特殊处理，采用值传递方式
            PosEntity temp = new PosEntity(posEntity.getType());
            temp.setStart(posEntity.getStartX(), posEntity.getStartY());
            temp.setEnd(posEntity.getEndX(), posEntity.getEndY());

            // 这些max要乘以2的原因是，在后面的getStartX()等方法调用时，max是按照大图的max来计算的
            temp.setMaxX(max_x * 2);
            temp.setMaxY(max_y * 2);
            data.add(temp);
        }

        for(PosEntity posEntity : data) {
            posEntity.setStart(posEntity.getStartX() / 2, posEntity.getStartY() / 2);
            posEntity.setEnd(posEntity.getEndX() / 2, posEntity.getEndY() / 2);
        }
    }
}


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
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.df.dfcarchecker.CarCheckOutsideActivity;
import com.df.dfcarchecker.R;
import com.df.entry.FaultPhotoEntity;
import com.df.entry.StructurePhotoEntity;
import com.df.service.Common;

import java.util.ArrayList;
import java.util.List;

public class OutsidePaintPreviewView extends ImageView {

    private int currentType;
    private boolean move;
    private List<FaultPhotoEntity> data;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;
    private Bitmap otherBitmap;

    private int max_x, max_y;

    public OutsidePaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public OutsidePaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public OutsidePaintPreviewView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<FaultPhotoEntity> entities) {
        this.bitmap = bitmap;
        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_color_diff);
        otherBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_other);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        data = CarCheckOutsideActivity.posEntities;
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
        paint.setStyle(Paint.Style.STROKE); //加粗
        paint.setStrokeWidth(4); //宽度

        return paint;
    }

    private void paint(Canvas canvas) {
        for (FaultPhotoEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(FaultPhotoEntity entity, Canvas canvas) {
        int type = entity.getType();

        switch (type) {
            case Common.COLOR_DIFF:
                canvas.drawBitmap(colorDiffBitmap, entity.getStartX(), entity.getStartY(), null);
                return;
            case Common.SCRATCH:
                canvas.drawLine(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY(), getPaint(type));
                return ;
            case Common.TRANS:
                int dx = Math.abs(entity.getEndX() - entity.getStartX());
                int dy = Math.abs(entity.getEndY() - entity.getStartY());
                int dr = (int)Math.sqrt(dx * dx + dy * dy);
                int x0 = (entity.getStartX() + entity.getEndX()) / 2;
                int y0 = (entity.getStartY() + entity.getEndY()) / 2;
                canvas.drawCircle(x0, y0, dr / 2, getPaint(type));
                return;
            case Common.SCRAPE:
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

                canvas.drawRect(rectF, getPaint(type));

                return;
            case Common.OTHER:
                canvas.drawBitmap(otherBitmap, entity.getStartX(), entity.getStartY(), null);
                return;
        }
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
            temp.setMaxX((int)(max_x * 1.5));
            temp.setMaxY((int)(max_y * 1.5));
            data.add(temp);
        }

        for(FaultPhotoEntity faultPhotoEntity : data) {
            faultPhotoEntity.setStart((int)(faultPhotoEntity.getStartX() / 1.5), (int)(faultPhotoEntity.getStartY() / 1.5));
            faultPhotoEntity.setEnd((int)(faultPhotoEntity.getEndX() / 1.5), (int)(faultPhotoEntity.getEndY() / 1.5));
        }
    }
}


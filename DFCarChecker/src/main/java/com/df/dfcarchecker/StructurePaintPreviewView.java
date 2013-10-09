package com.df.dfcarchecker;

/**
 * Created by 岩 on 13-9-26.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.df.service.Common;
import com.df.service.PosEntity;

import java.util.ArrayList;
import java.util.List;

public class StructurePaintPreviewView extends ImageView {

    private int currentType;
    private boolean move;
    private List<PosEntity> data;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    public StructurePaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public StructurePaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StructurePaintPreviewView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_base_image);
        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        data = new ArrayList<PosEntity>();

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_color_diff);
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

    private void HandelPosEntitiesDueToDifferentResolution() {
        data.clear();

        for(PosEntity posEntity : CarCheckStructureFragment.posEntities) {
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


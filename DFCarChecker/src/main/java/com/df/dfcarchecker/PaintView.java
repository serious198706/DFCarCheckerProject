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

public class PaintView extends ImageView {

    private int currentType = Common.COLOR_DIFF;
    private boolean move;
    private List<PosEntity> data = CarCheckOutsideFragment.posEntities;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    public PaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaintView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_color_diff);
        this.setOnTouchListener(onTouchListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);

    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (currentType > 0 && currentType <= 4) {

                int x = (int) event.getX();
                int y = (int) event.getY();
                PosEntity entity = null;

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    entity = new PosEntity(currentType);
                    entity.setMaxX(max_x);
                    entity.setMaxY(max_y);
                    entity.setStart(x, y);
                    if(currentType!=1){
                        entity.setEnd(x, y);
                    }
                    data.add(entity);
                } else if(event.getAction()==MotionEvent.ACTION_MOVE){
                    if(currentType!=1){
                        entity = data.get(data.size()-1);
                        entity.setEnd(x, y);
                        move = true;
                    }
                } else if(event.getAction()==MotionEvent.ACTION_UP){
                    if(currentType==2&&move){
                        entity = data.get(data.size()-1);
                        entity.setEnd(x, y);
                        move = false;
                    }

                    showCamera();
                }
                invalidate();
            }
            return true;
        }
    };

    public void setType(int type) {
        this.currentType = type;
    }

    private Paint getPaint(int type) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setAlpha(0x80);//半透明

        switch (type) {
            case Common.COLOR_DIFF:
                paint.setStyle(Paint.Style.FILL_AND_STROKE);//填充并且填充
                paint.setStrokeWidth(4); //宽度
                break;
            case Common.SCRATCH:
                paint.setStyle(Paint.Style.STROKE); //加粗
                paint.setStrokeWidth(4); //宽度
                break;
            case Common.TRANS:
                paint.setStyle(Paint.Style.STROKE); //加粗
                paint.setStrokeWidth(4); //宽度
                break;
            case Common.SCRAPE:
                paint.setStyle(Paint.Style.STROKE); //加粗
                paint.setStrokeWidth(4); //宽度
                break;
        }

        return paint;
    }

    private void paint(Canvas canvas) {
        for (PosEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(PosEntity entity, Canvas canvas) {
        int type = entity.getType();

        switch (type) {
            case Common.COLOR_DIFF:
                canvas.drawBitmap(colorDiffBitmap, entity.getStartX(), entity.getStartY(), null);
                //canvas.drawCircle(entity.getStartX(), entity.getStartY(), 16, getPaint(type));
                return;
            case Common.SCRATCH:
                canvas.drawLine(entity.getStartX(), entity.getStartY(), entity.getEndX(), entity.getEndY(), getPaint(type));
                return ;
            case Common.TRANS:
                int dx = Math.abs(entity.getEndX()-entity.getStartX());
                int dy = Math.abs(entity.getEndY()-entity.getStartY());
                int dr = (int)Math.sqrt(dx*dx+dy*dy);
                int x0 = (entity.getStartX()+entity.getEndX())/2;
                int y0 = (entity.getStartY()+entity.getEndY())/2;
                canvas.drawCircle(x0, y0, dr/2, getPaint(type));
                return;
            case Common.SCRAPE:
                canvas.drawRect(
                        new RectF(entity.getStartX(), entity.getStartY(), entity.getEndX() , entity.getEndY()),
                        getPaint(type));
                return;
        }
    }

    private void showCamera(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("拍照");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ((Activity)getContext()).startActivityForResult(intent, 1);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    public PosEntity getPosEntity(){
        if(data.isEmpty()){
            return null;
        }
        return data.get(data.size()-1);
    }

    public void Clear() {
        if(!data.isEmpty()) {
            data.clear();
        }
    }
}


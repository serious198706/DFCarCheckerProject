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

public class InsidePaintView extends ImageView {

    private int currentType = Common.DIRTY;
    private boolean move;
    private List<PosEntity> data = CarCheckInsideFragment.posEntities;
    private List<PosEntity> undoData;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    public InsidePaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public InsidePaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InsidePaintView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        undoData = new ArrayList<PosEntity>();

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
            if (currentType > 4 && currentType <= 6) {

                int x = (int) event.getX();
                int y = (int) event.getY();
                PosEntity entity;

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    entity = new PosEntity(currentType);
                    entity.setMaxX(max_x);
                    entity.setMaxY(max_y);
                    entity.setStart(x, y);
                    entity.setEnd(x, y);
                    data.add(entity);
                } else if(event.getAction() == MotionEvent.ACTION_MOVE){
                    entity = data.get(data.size() - 1);
                    entity.setEnd(x, y);
                    move = true;
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    if(move){
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
            undoData.clear();
            invalidate();
        }
    }

    public void Undo() {
        if(!data.isEmpty()) {
            undoData.add(data.get(data.size() - 1));
            data.remove(data.size() - 1);
            invalidate();
        }
    }

    public void Redo() {
        if(!undoData.isEmpty()) {
            data.add(undoData.get(undoData.size() - 1));
            undoData.remove(undoData.size() - 1);
            invalidate();
        }
    }
}


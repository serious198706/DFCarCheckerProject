package com.df.paintview;

/**
 * Created by 岩 on 13-9-26.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.df.dfcarchecker.CarCheckInteriorActivity;
import com.df.entry.PhotoEntity;
import com.df.entry.PosEntity;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.ImageUploadQueue;

import java.util.ArrayList;
import java.util.List;

public class InteriorPaintView extends PaintView {

    private int currentType = Common.DIRTY;
    private boolean move;
    private List<PosEntity> data = CarCheckInteriorActivity.posEntities;
    private List<PhotoEntity> photoEntities = CarCheckInteriorActivity.photoEntities;

    // 本次更新的坐标点，如果用户点击取消，则不将thisTimeNewData中的坐标加入到data中
    private List<PosEntity> thisTimeNewData;
    private List<PosEntity> undoData;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    private long currentTimeMillis;
    public long getCurrentTimeMillis() {return currentTimeMillis;}

    private ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

    public InteriorPaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public InteriorPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public InteriorPaintView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<PosEntity> entities) {
        this.bitmap = bitmap;
        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        undoData = new ArrayList<PosEntity>();
        thisTimeNewData = new ArrayList<PosEntity>();

        this.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (currentType >= Common.DIRTY && currentType <= Common.BROKEN) {

                int x = (int) event.getX();
                int y = (int) event.getY();
                PosEntity entity = null;

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    entity = new PosEntity(currentType);
                    entity.setMaxX(max_x);
                    entity.setMaxY(max_y);
                    entity.setStart(x, y);
                    entity.setEnd(x, y);
                    data.add(entity);
                    thisTimeNewData.add(entity);
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

                    //showCamera();

                    // 如果手指在屏幕上移动范围非常小
                    if(entity == null) {
                        entity = data.get(data.size() - 1);
                    }

                    if(entity != null) {
                        if(Math.abs(entity.getEndX() - entity.getStartX()) < 10 &&
                                Math.abs(entity.getEndY() - entity.getStartY()) < 10) {
                            data.remove(entity);
                        } else {
                            showCamera();
                        }
                    }
                }
                invalidate();
            }
            return true;
        }
    };

    public void setType(int type) {
        this.currentType = type;
    }
    public int getType() {return this.currentType;}

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

                currentTimeMillis = System.currentTimeMillis();
                Uri fileUri = Helper.getOutputMediaFileUri(currentTimeMillis); // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                ((Activity)getContext()).startActivityForResult(intent, 1);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    public PosEntity getPosEntity(){
        if(data.isEmpty()){
            return null;
        }
        return data.get(data.size()-1);
    }

    public List<PhotoEntity> getPhotoEntities() { return photoEntities; }
    public List<PhotoEntity> getPhotoEntities(String sight) { return null; }

    public String getGroup() {
        return "interior";
    }

    public void clear() {
        if(!data.isEmpty()) {
            data.clear();
            undoData.clear();
            invalidate();
        }
    }

    public void undo() {
        if(!data.isEmpty()) {
            undoData.add(data.get(data.size() - 1));
            data.remove(data.size() - 1);
            invalidate();
        }
    }

    public void redo() {
        if(!undoData.isEmpty()) {
            data.add(undoData.get(undoData.size() - 1));
            undoData.remove(undoData.size() - 1);
            invalidate();
        }
    }

    public void cancel() {
        if(!thisTimeNewData.isEmpty()) {
            for(int i = 0; i < thisTimeNewData.size(); i++) {
                data.remove(thisTimeNewData.get(i));
            }
        }
    }
}


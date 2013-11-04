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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.df.dfcarchecker.CarCheckFrameFragment;
import com.df.dfcarchecker.R;
import com.df.entry.PhotoEntity;
import com.df.entry.PosEntity;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.ImageUploadQueue;

import java.util.ArrayList;
import java.util.List;

public class FramePaintView extends PaintView {

    private int currentType = Common.COLOR_DIFF;
    private boolean move;
    private List<PosEntity> data;

    // 本次更新的坐标点，如果用户点击取消，则不将thisTimeNewData中的坐标加入到data中
    private List<PosEntity> thisTimeNewData;
    private List<PosEntity> undoData;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    private long currentTimeMillis;
    private List<PhotoEntity> photoEntitiesFront = CarCheckFrameFragment.photoEntitiesFront;
    private List<PhotoEntity> photoEntitiesRear = CarCheckFrameFragment.photoEntitiesRear;

    public long getCurrentTimeMillis() {return currentTimeMillis;}

    private ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

    public FramePaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public FramePaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public FramePaintView(Context context) {
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

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_color_diff);
        this.setOnTouchListener(onTouchListener);
    }

    public void setBitmap(Bitmap bitmap) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (currentType > 0 && currentType <= 4) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                PosEntity entity;

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    entity = new PosEntity(currentType);
                    entity.setMaxX(max_x);
                    entity.setMaxY(max_y);
                    entity.setStart(x, y);

                    data.add(entity);
                    thisTimeNewData.add(entity);
                } else if(event.getAction() == MotionEvent.ACTION_MOVE){
                        entity = data.get(data.size() - 1);
                        entity.setStart(x, y);
                        move = true;
                        invalidate();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
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

    private void paint(Canvas canvas) {
        for (PosEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(PosEntity entity, Canvas canvas) {
        canvas.drawBitmap(colorDiffBitmap, entity.getStartX(), entity.getStartY(), null);
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
                undo();
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

    public List<PhotoEntity> getPhotoEntities() {
        return null;
    }

    public List<PhotoEntity> getPhotoEntities(String sight) {
        if(sight.equals("FRONT"))
            return photoEntitiesFront;
        else
            return photoEntitiesRear;
    }

    public String getGroup() {
        return "frame";
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


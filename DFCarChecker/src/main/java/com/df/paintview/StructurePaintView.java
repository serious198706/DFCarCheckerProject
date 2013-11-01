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
import android.widget.ImageView;

import com.df.dfcarchecker.LoginActivity;
import com.df.dfcarchecker.R;
import com.df.entry.FaultPhotoEntity;
import com.df.entry.PhotoEntity;
import com.df.entry.StructurePhotoEntity;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.ImageUploadQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StructurePaintView extends ImageView {

    private int currentType = Common.COLOR_DIFF;
    private boolean move;
    private List<StructurePhotoEntity> data;

    // 本次更新的坐标点，如果用户点击取消，则不将thisTimeNewData中的坐标加入到data中
    private List<StructurePhotoEntity> thisTimeNewData;
    private List<StructurePhotoEntity> undoData;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    private long currentTimeMillis;

    private ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();


    public StructurePaintView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public StructurePaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public StructurePaintView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<StructurePhotoEntity> entities) {
        this.bitmap = bitmap;
        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        undoData = new ArrayList<StructurePhotoEntity>();
        thisTimeNewData = new ArrayList<StructurePhotoEntity>();

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_color_diff);
        this.setOnTouchListener(onTouchListener);
    }

    public void setBitmap(Bitmap bitmap) {

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
                StructurePhotoEntity entity;

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    entity = new StructurePhotoEntity(currentType);
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
        for (StructurePhotoEntity entity : data) {
            paint(entity, canvas);
        }
    }

    private void paint(StructurePhotoEntity entity, Canvas canvas) {
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
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    public StructurePhotoEntity getPosEntity(){
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

    public void cancel() {
        if(!thisTimeNewData.isEmpty()) {
            for(int i = 0; i < thisTimeNewData.size(); i++) {
                data.remove(thisTimeNewData.get(i));
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if(resultCode == Activity.RESULT_OK) {
                    // 组织JsonString
                    JSONObject jsonObject = new JSONObject();

                    try {
                        jsonObject.put("PictureName", Long.toString(currentTimeMillis));
                        jsonObject.put("StartPoint", Integer.toString(getPosEntity().getStartX()) + "," +
                                Integer.toString(getPosEntity().getStartY()));
                        jsonObject.put("EndPoint", Integer.toString(getPosEntity().getEndX()) + "," +
                                Integer.toString(getPosEntity().getEndY()));
                        jsonObject.put("UniqueId", "199");
                        // 绘图类型 -
                        jsonObject.put("Type", currentType);
                        jsonObject.put("UserId", LoginActivity.userInfo.getId());
                        jsonObject.put("Key", LoginActivity.userInfo.getKey());
                    } catch (JSONException e) {

                    }

                    PhotoEntity photoEntity = new PhotoEntity();
                    photoEntity.setFileName(Helper.getOutputMediaFileUri(currentTimeMillis).getPath());
                    photoEntity.setJsonString(jsonObject.toString());

//                    imageUploadQueue.addImage(photoEntity);
                    // 暂时不加入照片池，等保存时才提交
                } else {
                }
                break;
        }
    }
}


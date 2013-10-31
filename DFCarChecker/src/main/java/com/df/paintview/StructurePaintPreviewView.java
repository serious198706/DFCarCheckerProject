package com.df.paintview;

/**
 * Created by 岩 on 13-9-26.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.df.dfcarchecker.CarCheckStructureFragment;
import com.df.dfcarchecker.R;
import com.df.entry.FaultPhotoEntity;
import com.df.entry.StructurePhotoEntity;

import java.util.List;

public class StructurePaintPreviewView extends ImageView {

    private int currentType;
    private boolean move;
    private List<StructurePhotoEntity> data;
    private Bitmap bitmap;
    private Bitmap colorDiffBitmap;

    private int max_x, max_y;

    public StructurePaintPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init();
    }

    public StructurePaintPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public StructurePaintPreviewView(Context context) {
        super(context);
        //init();
    }

    public void init(Bitmap bitmap, List<StructurePhotoEntity> entities) {
        this.bitmap = bitmap;

        data = entities;

        max_x = bitmap.getWidth();
        max_y = bitmap.getHeight();

        //data = new ArrayList<FaultPhotoEntity>();

        colorDiffBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.out_color_diff);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 因为此处并没有图片缩放，所以不需要进行坐标处理
        //HandelPosEntitiesDueToDifferentResolution();
        canvas.drawBitmap(bitmap, 0, 0, null);
        paint(canvas);
    }

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

    public StructurePhotoEntity getPosEntity(){
        if(data.isEmpty()){
            return null;
        }
        return data.get(data.size()-1);
    }

    public void setPosEntities(List<StructurePhotoEntity> entities) {
        data = entities;
    }

    private void HandelPosEntitiesDueToDifferentResolution() {
        data.clear();

        if(CarCheckStructureFragment.posEntitiesFront.isEmpty()) {
            return;
        }

        for(StructurePhotoEntity faultPhotoEntity : CarCheckStructureFragment.posEntitiesFront) {
            // 因为不能对原entities做修改，所以此处要做些特殊处理，采用值传递方式
            StructurePhotoEntity temp = new StructurePhotoEntity(faultPhotoEntity.getType());
            temp.setStart(faultPhotoEntity.getStartX(), faultPhotoEntity.getStartY());
            temp.setEnd(faultPhotoEntity.getEndX(), faultPhotoEntity.getEndY());

            // 这些max要乘以2的原因是，在后面的getStartX()等方法调用时，max是按照大图的max来计算的
            temp.setMaxX((int)(max_x * 1.5));
            temp.setMaxY((int)(max_y * 1.5));
            data.add(temp);
        }

        for(StructurePhotoEntity faultPhotoEntity : data) {
            faultPhotoEntity.setStart((int)(faultPhotoEntity.getStartX() / 1.5), (int)(faultPhotoEntity.getStartY() / 1.5));
            faultPhotoEntity.setEnd((int)(faultPhotoEntity.getEndX() / 1.5), (int)(faultPhotoEntity.getEndY() / 1.5));
        }
    }
}


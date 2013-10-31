package com.df.entry;

/**
 * Created by 岩 on 13-9-26.
 */

import android.graphics.Bitmap;

import com.df.dfcarchecker.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class FaultPhotoEntity implements Serializable {
    private int start_x, start_y;
    private int end_x, end_y;
    private int max_x, max_y;
    private int type ;  //1色差 2划痕 3变型 4刮蹭 5其他

    private String imageFileName;
    private Bitmap bitmap = null;

    public FaultPhotoEntity(int type) {
        this.type = type;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public void setStart(int x,int y){
        this.start_x = x;
        this.start_y = y;
    }

    public void setEnd(int x,int y){
        this.end_x = x;
        this.end_y = y;
    }

    public int getStartX() {
        return start_x;
    }

    public int getStartY() {
        return start_y;
    }

    public int getEndX() {
        return end_x > max_x ? max_x : end_x;
    }

    public int getEndY() {
        return end_y > max_y ? max_y : end_y;
    }

    public int getType() {
        return type;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public void setMaxX(int max_x) {
        this.max_x = max_x;
    }

    public void setMaxY(int max_y) {
        this.max_y = max_y;
    }

    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("PictureName", this.imageFileName);
            jsonObject.put("StartPoint", Integer.toString(getStartX()) + "," + Integer.toString(getStartY()));
            jsonObject.put("EndPoint", Integer.toString(getEndX()) + "," + Integer.toString(getEndY()));
            jsonObject.put("UniqueId", "199");
            // 绘图类型
            jsonObject.put("Type", getType());
            jsonObject.put("UserId", LoginActivity.userInfo.getId());
            jsonObject.put("Key", LoginActivity.userInfo.getKey());
        } catch (JSONException e) {

        }

        return jsonObject.toString();
    }

}

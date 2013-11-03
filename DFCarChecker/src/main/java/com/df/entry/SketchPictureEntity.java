package com.df.entry;

import android.graphics.Bitmap;

import com.df.dfcarchecker.CarCheckBasicInfoFragment;
import com.df.dfcarchecker.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 13-10-31.
 */
public class SketchPictureEntity {
    private Bitmap bitmap;
    private String imageFileName;
    private int part;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("PictureName", this.imageFileName);
            jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
            // 绘图类型
            jsonObject.put("Part", part);
            jsonObject.put("UserId", LoginActivity.userInfo.getId());
            jsonObject.put("Key", LoginActivity.userInfo.getKey());
        } catch (JSONException e) {

        }

        return jsonObject.toString();
    }
}
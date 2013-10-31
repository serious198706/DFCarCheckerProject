package com.df.entry;

import android.graphics.Bitmap;

import com.df.dfcarchecker.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 岩 on 13-10-31.
 */
public class EngineRoomPhotoEntity {
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

    public int getPart() {
        return part;
    }

    public void setPart(int part) {
        this.part = part;
    }

    public String getJsonString() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("PictureName", this.imageFileName);
            jsonObject.put("UniqueId", "199");
            //
            jsonObject.put("Part", part);
            jsonObject.put("UserId", LoginActivity.userInfo.getId());
            jsonObject.put("Key", LoginActivity.userInfo.getKey());
        } catch (JSONException e) {

        }

        return jsonObject.toString();
    }
}

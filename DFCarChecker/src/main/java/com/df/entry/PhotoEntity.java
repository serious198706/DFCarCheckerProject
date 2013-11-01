package com.df.entry;

/**
 * Created by 岩 on 13-11-1.
 */
// 照片实体类，拍摄完成的照片都以此种方式保存，并加入池中
public class PhotoEntity {
    private String fileName;
    private String jsonString;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}

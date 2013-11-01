package com.df.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.df.dfcarchecker.LoginActivity;
import com.df.entry.FaultPhotoEntity;
import com.df.entry.PhotoEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-30.
 */
public final class ImageUploadQueue {
    public static List<PhotoEntity> queue;

    private static final ImageUploadQueue instance = new ImageUploadQueue();
    private Context context;

    private ImageUploadQueue() {
        queue = new ArrayList<PhotoEntity>();
    }

    public static ImageUploadQueue getInstance() {
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public PhotoEntity getEntity() {
        return queue.size() > 0 ? queue.get(0) : null;
    }

    // 添加元素
    public void addImage(PhotoEntity photoEntity) {
        queue.add(photoEntity);
    }

    // 当上传成功后，删除队列中第一个元素
    public void removeImage() {
        if(queue.size() > 0) {
            // 先将文件从内存卡中删除
            if(deleteImageFromExternalStorage(queue.get(0).getFileName())) {
                queue.remove(0);
            }
        }
    }

    private boolean deleteImageFromExternalStorage(String fileName) {
        String path = Environment.getExternalStorageDirectory().getPath();
        path += "/Pictures/DFCarChecker/";

        File file = new File(fileName);
        boolean deleted = file.delete();

        return deleted;
    }
}

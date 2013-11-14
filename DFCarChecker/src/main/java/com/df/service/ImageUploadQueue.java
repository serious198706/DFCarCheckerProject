package com.df.service;

import android.content.Context;
import android.os.Environment;

import com.df.entry.PhotoEntity;

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
    public boolean removeImage() {
        if(queue.size() > 0) {
            // 先将文件从内存卡中删除
            if(deleteImageFromExternalStorage(queue.get(0).getFileName())) {
                queue.remove(0);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean deleteImageFromExternalStorage(String fileName) {
        if((fileName == null) || (fileName.equals("")))
            return true;

        String path = Environment.getExternalStorageDirectory().getPath();
        path += "/Pictures/DFCarChecker/";

        File file = new File(path + fileName);
        boolean deleted = file.delete();



        return deleted;
    }
}

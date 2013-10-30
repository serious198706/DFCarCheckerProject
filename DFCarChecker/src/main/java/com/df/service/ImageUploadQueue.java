package com.df.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.df.dfcarchecker.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-30.
 */
public final class ImageUploadQueue {
    private List<ImageEntity> queue;
    private static final ImageUploadQueue instance = new ImageUploadQueue();
    private static UploadPictureTask mUploadPictureTask;

    private ImageUploadQueue() {
        queue = new ArrayList<ImageEntity>();
        //mUploadPictureTask = new UploadPictureTask();
    }

    public static ImageUploadQueue getInstance() {
        return instance;
    }

    public int getSize() {
        return queue.size();
    }

    public ImageEntity getImage() {
        return queue.size() > 0 ? queue.get(0) : null;
    }

    // 添加元素
    public void addImage(ImageEntity imageEntity) {
        queue.add(imageEntity);
    }

    // 当上传成功后，删除队列中第一个元素
    public void removeImage() {
        if(queue.size() > 0) {
            // 先将文件从内存卡中删除
            if(deleteImageFromExternalStorage(queue.get(0).fileName)) {
                queue.remove(0);
            }
        }
    }

    private boolean deleteImageFromExternalStorage(String fileName) {
        String path = Environment.getExternalStorageDirectory().getPath();

        File file = new File(path, "/" + fileName);
        boolean deleted = file.delete();

        return deleted;
    }

//    public void checkExternalStorageForNewImage() {
//        String path = Environment.getExternalStorageDirectory().toString() + "/Pictures/DFCarChecker";
//        Log.d("Files", "Path: " + path);
//
//        File f = new File(path);
//        File file[] = f.listFiles();
//
//        Log.d("Files", "Size: "+ file.length);
//        for (int i=0; i < file.length; i++)
//        {
//            Log.d("Files", "FileName:" + file[i].getName());
//        }
//
//        File f = new File("/mnt/sdcard/Pictures/DFCarChecker/structure_f_2.jpg");
//        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
//    }

    public class ImageEntity
    {
        public Bitmap bitmap;
        public String fileName;
    }

    public void startUpload() {

    }

    private class UploadPictureTask extends AsyncTask<Void, Void, Boolean> {
        Context context;

        private UploadPictureTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean success = false;

            SoapService soapService = new SoapService();

            // 设置soap的配置
            soapService.setUtils("http://192.168.100.6:50/ReportService.svc",
                    "http://cheyiju/IReportService/SaveCarPictureTagKey",
                    "SaveCarPictureTagKey");

            JSONObject jsonObject = new JSONObject();
            try {
                // TODO: 更改命名方式
                jsonObject.put("PictureName", "structure_f_2.jpg");
                jsonObject.put("StartPoint", "187,90");
                jsonObject.put("EndPoint", "255, 103");
                jsonObject.put("UniqueId", "199");
                // 绘图类型 -
                jsonObject.put("Type", "0");
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());
            } catch (JSONException e) {

            }

            File f = new File("/mnt/sdcard/Pictures/DFCarChecker/structure_f_2.jpg");
            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

            //success = soapService.uploadPicture(root.getContext(), bitmap, jsonObject.toString());

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUploadPictureTask = null;

            if(success) {

            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mUploadPictureTask = null;
        }
    }
}

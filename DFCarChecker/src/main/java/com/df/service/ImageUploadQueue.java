package com.df.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.df.dfcarchecker.LoginActivity;
import com.df.entry.EngineRoomPhotoEntity;
import com.df.entry.FaultPhotoEntity;
import com.df.entry.InsidePhotoEntity;
import com.df.entry.OutsidePhotoEntity;
import com.df.entry.StructurePhotoEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-30.
 */
public final class ImageUploadQueue {
    private List<FaultPhotoEntity> faultQueue;
    private List<StructurePhotoEntity> structureQueue;
    private List<EngineRoomPhotoEntity> engineRoomQueue;
    private List<InsidePhotoEntity> insideQueue;
    private List<OutsidePhotoEntity> outsideQueue;

    private static final ImageUploadQueue instance = new ImageUploadQueue();
    private static UploadPictureTask mUploadPictureTask;
    private Context context;

    private ImageUploadQueue() {
        faultQueue = new ArrayList<FaultPhotoEntity>();
        structureQueue = new ArrayList<StructurePhotoEntity>();
        engineRoomQueue = new ArrayList<EngineRoomPhotoEntity>();
        insideQueue = new ArrayList<InsidePhotoEntity>();
        outsideQueue = new ArrayList<OutsidePhotoEntity>();
    }

    public static ImageUploadQueue getInstance() {
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public int getSize(FaultPhotoEntity entity) {
        return faultQueue.size();
    }

    public int getSize(StructurePhotoEntity entity) {
        return structureQueue.size();
    }

    public int getSize(EngineRoomPhotoEntity entity) {
        return engineRoomQueue.size();
    }

    public int getSize(OutsidePhotoEntity entity) {
        return outsideQueue.size();
    }

    public int getSize(InsidePhotoEntity entity) {
        return insideQueue.size();
    }

    public FaultPhotoEntity getImage() {
        return faultQueue.size() > 0 ? faultQueue.get(0) : null;
    }

    // 添加元素
    public void addImage(FaultPhotoEntity faultPhotoEntity) {
        faultQueue.add(faultPhotoEntity);
    }

    // 当上传成功后，删除队列中第一个元素
    public void removeImage() {
        if(faultQueue.size() > 0) {
            // 先将文件从内存卡中删除
            if(deleteImageFromExternalStorage(faultQueue.get(0).getImageFileName())) {
                faultQueue.remove(0);
            }
        }
    }

    private boolean deleteImageFromExternalStorage(String fileName) {
        String path = Environment.getExternalStorageDirectory().getPath();
        path += "/Pictures/DFCarChecker/";

        File file = new File(path, fileName);
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

    public void startUpload(final Context context) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    // 当照片池中还有照片，并且上传线程没有运行时，开启新的上传线程
                    if((getSize() != 0) && (mUploadPictureTask == null)) {
                        mUploadPictureTask = new UploadPictureTask(context);
                        mUploadPictureTask.execute((Void) null);
                    }
                }
            }
        });

        thread.run();
    }

    private class UploadPictureTask extends AsyncTask<Void, Void, Boolean> {
        Context context;

        public UploadPictureTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success;

            SoapService soapService = new SoapService();

            // 设置soap的配置
            soapService.setUtils("http://192.168.100.6:50/ReportService.svc",
                    "http://cheyiju/IReportService/SaveCarPictureTagKey",
                    "SaveCarPictureTagKey");

            FaultPhotoEntity faultPhotoEntity = getImage();

            // 如果照片池中还有照片
            if(faultPhotoEntity != null) {
                JSONObject jsonObject = new JSONObject();
                try {
                        jsonObject.put("PictureName", faultPhotoEntity.getImageFileName());
                        jsonObject.put("StartPoint", Integer.toString(faultPhotoEntity.getStartX()) + "," + Integer.toString(faultPhotoEntity.getStartY()));
                        jsonObject.put("EndPoint", Integer.toString(faultPhotoEntity.getEndX()) + "," + Integer.toString(faultPhotoEntity.getEndY()));
                        jsonObject.put("UniqueId", "199");
                        // 绘图类型
                        jsonObject.put("Type", faultPhotoEntity.getType());
                        jsonObject.put("UserId", LoginActivity.userInfo.getId());
                        jsonObject.put("Key", LoginActivity.userInfo.getKey());
                } catch (JSONException e) {

                }

                success = soapService.uploadPicture(this.context, faultPhotoEntity.getBitmap(), jsonObject.toString());
            } else {
                success = true;
            }

            //File f = new File("/mnt/sdcard/Pictures/DFCarChecker/structure_f_2.jpg");
            //Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());



            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUploadPictureTask = null;

            if(success) {
                removeImage();
            }
        }

        @Override
        protected void onCancelled() {
            mUploadPictureTask = null;
        }
    }
}

package com.df.service;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import com.df.entry.PhotoEntity;

import java.io.File;

/**
 * Created by 岩 on 13-11-1.
 */
public class QueueScanService extends Service {
    private UploadPictureTask mUploadPictureTask;
    private ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private Context context;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            Log.d(Common.TAG, "message start....................");
            while (true) {
                synchronized (this) {
                    try {
                        // 当照片池中还有照片，并且上传线程没有运行时，开启新的上传线程
                        if((imageUploadQueue.getQueueSize() != 0) && (mUploadPictureTask == null))  {
                            mUploadPictureTask = new UploadPictureTask();
                            mUploadPictureTask.execute();
                            Log.d(Common.TAG, "正在上传...");
                        }

                        wait(3000);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);


        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(android.content.Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        this.context = this;
    }

    @Override
    public void onDestroy() {

    }

    // 上传图片
    private class UploadPictureTask extends AsyncTask<Void, Void, Boolean> {
        Context context;

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = true;

//            SoapService soapService = new SoapService();
//
//            // 设置soap的配置
//            soapService.setUtils("http://192.168.100.6:50/ReportService.svc",
//                    "http://cheyiju/IReportService/SaveCarPictureTagKey",
//                    "SaveCarPictureTagKey");
//
//            PhotoEntity photoEntity = imageUploadQueue.getEntity();
//
//            // 如果照片池中还有照片
//            if(photoEntity != null) {
//                // 获取照片的物理路径
//                String path = Environment.getExternalStorageDirectory().getPath();
//                path += "/Pictures/DFCarChecker/";
//                File file = new File(path, photoEntity.getFileName());
//
//                // 并保存成bitmap用于上传
//                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
//
//                success = soapService.uploadPicture(this.context, bitmap, photoEntity.getJsonString());
//            } else {
//                success = true;
//            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUploadPictureTask = null;

            if(success) {
                Log.d(Common.TAG, "上传成功！");
                imageUploadQueue.removeImage();
            } else {
                Log.d(Common.TAG, "上传照片失败，重试");
            }
        }

        @Override
        protected void onCancelled() {
            mUploadPictureTask = null;
        }
    }
}

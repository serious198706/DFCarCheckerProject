package com.df.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import com.df.dfcarchecker.CarCheckViewPagerActivity;
import com.df.dfcarchecker.R;
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

    private int[] waitTime = {500, 1000, 2000, 5000, 10000, 300000};
    private int index = 0;

    private boolean canStartUpload = true;

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
                        if((imageUploadQueue.getQueueSize() != 0) && (mUploadPictureTask == null)
                         && canStartUpload)  {
                            mUploadPictureTask = new UploadPictureTask();
                            mUploadPictureTask.execute();
                            Log.d(Common.TAG, "正在上传...");
                            canStartUpload = false;
//                            // prepare intent which is triggered if the
//                            // notification is selected
//
//                            Intent intent = new Intent(this, CarCheckViewPagerActivity.class);
//                            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
//
//                            // build notification
//                            // the addAction re-use the same intent to keep the example short
//                            Notification n  = new Notification.Builder(context)
//                                    .setContentTitle("New mail from " + "test@gmail.com")
//                                    .setContentText("Subject")
//                                    .setSmallIcon(R.drawable.logo)
//                                    .setContentIntent(pIntent)
//                                    .setAutoCancel(true)
//                                    .addAction(R.drawable.logo, "Call", pIntent)
//                                    .addAction(R.drawable.logo, "More", pIntent)
//                                    .addAction(R.drawable.logo, "And more", pIntent).build();
//
//
//                            NotificationManager notificationManager =
//                                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//                            notificationManager.notify(0, n);
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
        SoapService soapService;

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            soapService = new SoapService();

            // 设置soap的配置
            soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                    "http://cheyiju/IReportService/SaveCarPictureTagKey",
                    "SaveCarPictureTagKey");

            PhotoEntity photoEntity = imageUploadQueue.getEntity();

            // 如果照片池中还有照片
            if(photoEntity != null) {
                // 获取照片的物理路径
                Bitmap bitmap = null;
                String path = Environment.getExternalStorageDirectory().getPath();
                path += "/Pictures/DFCarChecker/";
                File file = new File(path + photoEntity.getFileName());

                // 并保存成bitmap用于上传
                if(file != null)
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                success = soapService.uploadPicture(this.context, bitmap, photoEntity.getJsonString());
            } else {
                success = true;
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUploadPictureTask = null;

            if(success) {
                Log.d(Common.TAG, "上传成功！");
                imageUploadQueue.removeImage();
                index = 0;
            } else {
                Log.d(Common.TAG, "上传照片失败：" + soapService.getErrorMessage());
                Log.d(Common.TAG, "将在" + Integer.toString(waitTime[index]) + "毫秒后重试");

                try {
                    // 等待时间变长，避免给服务器压力
                    wait(waitTime[index]);
                    if(index <= 5)
                        index++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            canStartUpload = true;
        }

        @Override
        protected void onCancelled() {
            mUploadPictureTask = null;

            canStartUpload = true;
        }
    }
}

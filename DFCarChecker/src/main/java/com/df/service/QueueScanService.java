package com.df.service;

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
import android.os.Process;

import com.df.dfcarchecker.CarCheck.CarCheckBasicInfoFragment;
import com.df.dfcarchecker.LoginActivity;
import com.df.entry.PhotoEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by 岩 on 13-11-1.
 */
public class QueueScanService extends Service {
    public static final String BROADCAST_ACTION = "com.df.dfcarchecker.displayevent";
    private final Handler handler = new Handler();

    Intent intent;

    private UploadPictureTask mUploadPictureTask;
    private ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private Context context;

    private int[] waitTime = {500, 1000, 2000, 5000, 10000, 30000};
    private int index = 0;

    private boolean canStartUpload = true;
    public static boolean committed;
    private String jsonString;

    private CommitDataTask mCommitDataTask;

    private String action;
    private int carId;
    private ModifyDataTask mModifyDataTask;

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        this.context = this;

        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        committed = intent.getExtras().getBoolean("committed");
        jsonString = intent.getExtras().getString("JSONObject");
        action = intent.getExtras().getString("action");
        carId = intent.getExtras().getInt("carId");

        handler.removeCallbacks(sendUpdatesToUI);

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(android.content.Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }

    // 通知主界面进行更新的线程
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            Committed();
        }
    };

    private Runnable sendUpdatesToUIFail = new Runnable() {
        @Override
        public void run() {
            CommitFailed();
        }
    };

    //
    private void Committed() {
        intent.putExtra("result", "0");
        sendBroadcast(intent);
    }

    private void CommitFailed() {
        intent.putExtra("result", "-1");
        sendBroadcast(intent);
    }

    // 从线程接收消息的Handler
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

                        // 已提交，并且照片池为空
                        if(committed) {
                            if(action.equals("commit") && (imageUploadQueue.getQueueSize() == 0) &&
                                    (mCommitDataTask == null)) {
                                mCommitDataTask = new CommitDataTask(context);
                                mCommitDataTask.execute(jsonString);
                            }
                            // 修改车辆
                            else if(action.equals("modify") && (imageUploadQueue.getQueueSize() == 0) &&
                                    (mModifyDataTask == null)) {
                                mModifyDataTask = new ModifyDataTask(context);
                                mModifyDataTask.execute(jsonString);
                            }
                        }

                        wait(3000);
                    } catch (Exception e) {
                    }
                }
            }
        }
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
                    "http://cheyipai/IReportService/SaveCarPictureTagKey",
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
                    Thread.sleep(waitTime[index]);
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


    // 提交检测数据
    public class CommitDataTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        SoapService soapService;
        //private ProgressDialog progressDialog;

        private CommitDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;

            // 组织最终json
            soapService = new SoapService();

            // 设置soap的配置
            soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                    "http://cheyipai/IReportService/SaveCarCheckInfo",
                    "SaveCarCheckInfo");

            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());
                jsonObject.put("JsonString", new JSONObject(params[0]));
            } catch (JSONException e) {

            }

            success = soapService.communicateWithServer(context, jsonObject.toString());

            // 登录失败，获取错误信息并显示
            if(!success) {
                Log.d("DFCarChecker", "提交失败! " + soapService.getErrorMessage());
            } else {
                Log.d(Common.TAG, "提交成功！" + soapService.getErrorMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCommitDataTask = null;
            committed = false;

            if(success) {
                handler.post(sendUpdatesToUI); // 1 second
            } else {
                handler.post(sendUpdatesToUIFail);
            }
        }

        @Override
        protected void onCancelled() {
            mCommitDataTask = null;
            committed = false;
        }
    }

    // 提交修改数据
    public class ModifyDataTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        SoapService soapService;
        //private ProgressDialog progressDialog;

        private ModifyDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;

            // 组织最终json
            soapService = new SoapService();

            // 设置soap的配置
            soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                    "http://cheyipai/IReportService/UpdateCheckInfo",
                    "UpdateCheckInfo");

            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());
                jsonObject.put("CarId", carId);
                jsonObject.put("JsonString", new JSONObject(params[0]));
            } catch (JSONException e) {

            }

            success = soapService.communicateWithServer(context, jsonObject.toString());

            // 登录失败，获取错误信息并显示
            if(!success) {
                Log.d("DFCarChecker", "提交失败! " + soapService.getErrorMessage());
            } else {
                Log.d(Common.TAG, "提交成功！" + soapService.getErrorMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCommitDataTask = null;
            committed = false;

            if(success) {
                handler.post(sendUpdatesToUI); // 1 second
            } else {
                handler.post(sendUpdatesToUIFail);
            }
        }

        @Override
        protected void onCancelled() {
            mCommitDataTask = null;
            committed = false;
        }
    }
}

package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.df.dfcarchecker.CarCheck.CarCheckViewPagerActivity;
import com.df.dfcarchecker.CarReport.CarCheckedListActivity;
import com.df.entry.UserInfo;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.ImageUploadQueue;
import com.df.service.SoapService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends Activity {
    public static UserInfo userInfo;

    private ProgressDialog mProgressDialog;
    private LogoutTask mLogoutTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInfo = new UserInfo();

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            userInfo.setId(bundle.getString("UserId"));
            userInfo.setKey(bundle.getString("Key"));
        }

        Helper.showView(Common.innerVersion, getWindow().getDecorView(), R.id.innerTestVersion);
    }

    public void EnterCarCheck(View view) {
        Intent intent = new Intent(this, CarCheckViewPagerActivity.class);
        startActivity(intent);
    }

    public void EnterCarCheckedList(View view) {
        Intent intent = new Intent(this, CarCheckedListActivity.class);
        startActivity(intent);
    }

    public void Quit(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_title);
        builder.setMessage(R.string.quit);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 退出
                mLogoutTask = new LogoutTask(MainActivity.this);
                mLogoutTask.execute();
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    @Override
    public void onBackPressed() {
        Quit(null);
    }

    private class LogoutTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        SoapService soapService;

        private LogoutTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(context, null,
                    "正在注销...", false, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            // 删除产生的垃圾文件
            DeleteRecursive(new File(Environment.getExternalStorageDirectory().getPath() +
                    "/.cheyipai"));
            DeleteRecursive(new File(Environment.getExternalStorageDirectory().getPath() +
                    "/Pictures/DFCarChecker"));

            soapService = new SoapService();

            soapService.setUtils(Common.SERVER_ADDRESS + Common.USER_MANAGE_SERVICE,
                    "http://cheyipai/IUserManageService/LogOut",
                    "LogOut");

            JSONObject jsonObject = new JSONObject();

            try {
                if(userInfo != null) {
                    jsonObject.put("UserId", userInfo.getId());
                    jsonObject.put("Key", userInfo.getKey());
                }
            } catch (JSONException e) {

            }

            success = soapService.communicateWithServer(jsonObject.toString());

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mProgressDialog.dismiss();
            mLogoutTask = null;

            if(success) {
                Log.d(Common.TAG, "注销成功！");
                finish();
            } else {
                Log.d(Common.TAG, "注销失败！");
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
            mLogoutTask = null;
        }
    }
}

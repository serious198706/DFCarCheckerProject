package com.df.dfcarchecker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.df.service.Common;
import com.df.service.SoapService;
import com.df.entry.UserInfo;
import com.df.service.XmlHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    /**
     * 后台任务，用来进行登录
     */
    private UserLoginTask mAuthTask = null;

    // 储存用户名、密码
    private String mUserName;
    private String mPassword;

    // 组件们
    private EditText mUserNameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    // 用户信息：id、key
    public static UserInfo userInfo;

    private CheckUpdateTask mCheckUpdateTask;
    private DownloadTask mDownloadTask;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserNameView = (EditText) findViewById(R.id.userName);
        //mUserNameView.setText(mUserName);

        mPasswordView = (EditText) findViewById(R.id.password);

        // 当在密码填写时点击了回车，也视为进行登录操作
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckUpdateTask = new CheckUpdateTask(LoginActivity.this);
                mCheckUpdateTask.execute();
                //attemptLogin();
            }
        });

        //ImageUploadQueue queue = ImageUploadQueue.getInstance();
    }


    /**
     * 尝试进行登陆
     * 如果有错误（用户名、密码未填写）则不进行登陆
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // 检查网络
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        if (networkInfo == null || !networkInfo.isConnected()) {
//            Toast.makeText(this, "无法连接到网络", Toast.LENGTH_LONG).show();
//            return;
//        }

        mUserNameView.setError(null);
        mPasswordView.setError(null);

        mUserName = mUserNameView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 检查用户名
        if (TextUtils.isEmpty(mUserName)) {
            mUserNameView.setError(getString(R.string.error_username_required));
            focusView = mUserNameView;
            cancel = true;
        }

        // 检查密码
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_password_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // 有错误，让有错误的组件获取焦点
            focusView.requestFocus();
        } else {
            // 显示一个进度画面，并启动后台任务进行登录
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask(this);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * 显示进度动画，隐藏登录框
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * 一个异步的登录任务
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        SoapService soapService;

        private UserLoginTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            WifiManager wifiMan = (WifiManager) context.getSystemService(
                    Context.WIFI_SERVICE);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            String macAddr = wifiInf.getMacAddress();

            String serialNumber = null;

            try {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serialNumber = (String) get.invoke(c, "ro.serialno");
            } catch (Exception ignored) {
            }


            try {
                // 登录
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("UserName", mUserName);
                jsonObject.put("Password", mPassword);
                jsonObject.put("Key", macAddr);
                jsonObject.put("SerialNumber", serialNumber);

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils(Common.SERVER_ADDRESS + Common.USER_MANAGE_SERVICE,
                        "http://cheyipai/IUserManageService/UserLogin",
                        "UserLogin");

                success = soapService.login(context, jsonObject.toString());

                // 登录失败，获取错误信息并显示
                if(!success) {
                    Log.d("DFCarChecker", "Login error:" + soapService.getErrorMessage());
                } else {
                    userInfo = new UserInfo();

                    try {
                        JSONObject userJsonObject = new JSONObject(soapService.getResultMessage());

                        // 保存用户的UserId和此次登陆的Key
                        userInfo.setId(userJsonObject.getString("UserId"));
                        userInfo.setKey(userJsonObject.getString("Key"));
                    } catch (Exception e) {
                        Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                        return false;
                    }
                }
            } catch (JSONException e) {
                Log.d("DFCarChecker", "Json解析错误: " + e.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("UserId", userInfo.getId());
                intent.putExtra("Key", userInfo.getKey());
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError(soapService.getErrorMessage());
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
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
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Quit(null);
    }


    private class CheckUpdateTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private SoapService soapService;

        public CheckUpdateTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(context, null, "正在检测最新版本...", false, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = true;

            soapService = new SoapService();
            soapService.setUtils(Common.SERVER_ADDRESS + Common.USER_MANAGE_SERVICE,
                    "http://cheyipai/IUserManageService/GetAppNewVersionInfo", "GetAppNewVersionInfo");

            success = soapService.checkUpdate(context);

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mProgressDialog.dismiss();

            if(success) {
                try {
                    // {
                    //   "VersionNumber":"numberValue",
                    //   "DownloadAddress":""addressValue",
                    //   "Description":"descValue"
                    // }

                    JSONObject jsonObject = new JSONObject(soapService.getResultMessage());

                    final String version = jsonObject.getString("VersionNumber");
                    final String appAddress = jsonObject.getString("DownloadAddress");

                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                    // 版本不同，升级
                    if(compareVersion(pInfo.versionName, version)) {
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle(R.string.newUpdate)
                                .setMessage("检测到新版本，点击确定进行更新")
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mProgressDialog = new ProgressDialog(context);
                                        mProgressDialog.setMessage("正在下载");
                                        mProgressDialog.setIndeterminate(true);
                                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                        mProgressDialog.setCancelable(true);

                                        mDownloadTask = new DownloadTask(context);
                                        mDownloadTask.execute(appAddress);
                                        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                mDownloadTask.cancel(true);
                                            }
                                        });
                                        mProgressDialog.setCanceledOnTouchOutside(false);

                                    }
                                })
                                .create();

                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    } else {
                        attemptLogin();
                    }
                } catch (Exception e) {

                }
            } else {
                Toast.makeText(context, "获取版本号失败！", Toast.LENGTH_SHORT).show();
                attemptLogin();
            }
        }
    }

    // 比较版本号
    private boolean compareVersion(String localVersion, String serverVersion) {
        if(localVersion.charAt(0) < serverVersion.charAt(0))
            return true;

        if(localVersion.charAt(2) < serverVersion.charAt(2))
            return true;

        if(localVersion.charAt(4) < serverVersion.charAt(4))
            return true;

        return false;
    }

    // 下载新版本apk
    private class DownloadTask extends AsyncTask<String, Integer, Boolean> {

        private Context context;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... sUrl) {
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            wl.acquire();

            try {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(sUrl[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                        return false;

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    // download the file
                    input = connection.getInputStream();

                    File downloadPath = new File(Environment.getExternalStorageDirectory().getPath() +
                            "/Download");

                    downloadPath.mkdirs();

                    output = new FileOutputStream(downloadPath.getPath() + "/DFCarChecker.apk");

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled())
                            return false;
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    return false;
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    }
                    catch (IOException ignored) { }

                    if (connection != null)
                        connection.disconnect();
                }
            } finally {
                wl.release();
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mProgressDialog.dismiss();

            if(success) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory() + "/Download/" + "DFCarChecker.apk")),
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(context, "下载失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.df.dfcarchecker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.df.service.Common;
import com.df.service.SoapService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class StartupActivity extends Activity {
    private String extStorageDirectory;

    private CheckUpdateTask mCheckUpdateTask;
    private DownloadTask mDownloadTask;

    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirstRun();
    }

    private void FirstRun() {
        SharedPreferences settings = this.getSharedPreferences("DFCarChecker", 0);
        boolean firstrun = settings.getBoolean("firstrun", true);

        // 如果为第一次运行，则要释放所需要的文件
        if (firstrun) {
            Log.d("DFCarChecker", "firstrun");

            SharedPreferences.Editor e = settings.edit();
            e.putBoolean("firstrun", false);
            e.commit();

            SetDirectory();
            Intent home = new Intent(StartupActivity.this, LoginActivity.class);
            startActivity(home);
            finish();

        }
        // 如果不是第一次运行，则直接检查更新
        else {
            mCheckUpdateTask = new CheckUpdateTask(this);
            mCheckUpdateTask.execute();
        }
    }

    /**
     * -- Check to see if the sdCard is mounted and create a directory w/in it
     * ========================================================================
     **/
    private void SetDirectory() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {

            extStorageDirectory = Environment.getExternalStorageDirectory().toString();

            File txtDirectory = new File(extStorageDirectory + "/.cheyipai/");
            // Create a File object for the parent directory
            txtDirectory.mkdirs();// Have the object build the directory structure, if needed.
            CopyAssets(); // Then run the method to copy the file.

            Log.d("DFCarChecker", "/.cheyipai created.");

        } else if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Log.d("tag", "sdcard missing");
        }

    }

    /**
     * -- Copy the file from the assets folder to the sdCard
     * ===========================================================
     **/
    private void CopyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }
        for (int i = 0; i < files.length; i++) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(files[i]);
                out = new FileOutputStream(extStorageDirectory + "/.cheyipai/" + files[i]);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                Log.e("tag", e.getMessage());
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
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
                        Intent home = new Intent(StartupActivity.this, LoginActivity.class);
                        startActivity(home);
                        finish();
                    }
                } catch (Exception e) {

                }
            } else {
                Toast.makeText(context, "获取版本号失败！", Toast.LENGTH_SHORT).show();
                Intent home = new Intent(StartupActivity.this, LoginActivity.class);
                startActivity(home);
                finish();
            }
        }
    }

    private boolean compareVersion(String localVersion, String serverVersion) {
        if(localVersion.charAt(0) < serverVersion.charAt(0))
            return true;

        if(localVersion.charAt(2) < serverVersion.charAt(2))
            return true;

        if(localVersion.charAt(4) < serverVersion.charAt(4))
            return true;

        return false;
    }


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
                    output = new FileOutputStream(Environment.getExternalStorageDirectory()
                            .getPath() + "/Download/DFCarChecker.apk");

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
                startActivity(intent);
            } else {
                Toast.makeText(context, "下载失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

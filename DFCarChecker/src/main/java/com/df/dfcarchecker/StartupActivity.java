package com.df.dfcarchecker;

import android.app.AlertDialog;
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
import java.util.logging.Logger;

public class StartupActivity extends Activity {
    private String extStorageDirectory;

    private CheckUpdateTask mCheckUpdateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirstRun();
    }

    private void FirstRun() {
        SharedPreferences settings = this.getSharedPreferences("DFCarChecker", 0);
        boolean firstrun = settings.getBoolean("firstrun", true);
        if (firstrun) { // Checks to see if we've ran the application b4
            Log.d("DFCarChecker", "firstrun");

            SharedPreferences.Editor e = settings.edit();
            e.putBoolean("firstrun", false);
            e.commit();
            // If not, run these methods:
            SetDirectory();
            Intent home = new Intent(StartupActivity.this, LoginActivity.class);
            startActivity(home);
            finish();

        } else { // Otherwise start the application here:
            Intent home = new Intent(StartupActivity.this, LoginActivity.class);
            startActivity(home);
            finish();
//            mCheckUpdateTask = new CheckUpdateTask(this);
//            mCheckUpdateTask.execute();
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

        protected Boolean doInBackground(Void... params) {
            boolean success = true;

            soapService = new SoapService();
            soapService.setUtils(Common.SERVER_ADDRESS + Common.USER_MANAGE_SERVICE,
                    "http://cheyipai/IUserManageService/GetAppNewVersionInfo", "GetAppNewVersionInfo");

            //success = soapService.checkUpdate(context);

            return success;
        }

        protected void onPostExecute(boolean success) {
            if(success) {
                try {
                    // {
                    //   "VersionNumber":"numberValue",
                    //   "DownloadAddress":""addressValue",
                    //   "Description":"descValue"
                    // }

                    //JSONObject jsonObject = new JSONObject(soapService.getResultMessage());
                    //String version = jsonObject.getString("versionName");

                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                    // 版本不同，升级
                    //if(!version.equals(pInfo.versionName)) {
                    if(true) {
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle(R.string.newUpdate)
                                .setMessage("检测到新版本，点击确定进行更新")
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                  Uri.parse("http://cy198706" +
                                                          ".com/kanmeizhi/recommend/1.jpg")));
//                                                Uri.parse("http://i.268v.com/pub/app/DFCarChecker" +
//                                                        ".apk")));
                                    }
                                })
                                .create();

                        dialog.show();
                    }

                    Intent home = new Intent(StartupActivity.this, LoginActivity.class);
                    startActivity(home);
                    finish();
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
}

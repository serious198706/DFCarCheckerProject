package com.df.dfcarchecker;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StartupActivity extends Activity {
    private String extStorageDirectory;

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
            Intent intent = new Intent(StartupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
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
}

package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.df.service.ImageUploadQueue;
import com.df.service.QueueScanService;

public class MainActivity extends Activity {
    public static ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //imageUploadQueue.startUpload(this);

        Intent intent = new Intent(this, QueueScanService.class);
        startService(intent);
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
                Intent intent = new Intent(MainActivity.this, QueueScanService.class);
                stopService(intent);
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
}

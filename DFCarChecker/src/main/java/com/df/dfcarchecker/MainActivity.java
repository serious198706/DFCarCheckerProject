package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.df.dfcarchecker.CarCheck.CarCheckViewPagerActivity;
import com.df.dfcarchecker.CarReport.CarCheckedListActivity;
import com.df.entry.UserInfo;
import com.df.service.ImageUploadQueue;

public class MainActivity extends Activity {
    public static ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

    public static UserInfo userInfo;

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

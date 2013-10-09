package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        }

    public void EnterProcedureInput(View view) {
        Intent intent = new Intent(this, ProcedureInputFrameActivity.class);
        startActivity(intent);
    }

    public void EnterCarCheck(View view) {
        Intent intent = new Intent(this, CarCheckFrameActivity.class);
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

}

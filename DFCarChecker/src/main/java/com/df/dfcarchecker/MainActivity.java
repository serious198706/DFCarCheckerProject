package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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

    public void EnterCarWaitingList(View view) {
        Intent intent = new Intent(this, CarWaitingListActivity.class);
        startActivity(intent);
    }

    public void EnterCarCheckedList(View view) {
        Intent intent = new Intent(this, CarCheckedListActivity.class);
        startActivity(intent);
    }

    public void Quit(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.quit_title);
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

package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.Activity;
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

    public void EnterCarWaitingList(View view) {
        Intent intent = new Intent(this, CarWaitingListActivity.class);
        startActivity(intent);
    }

}

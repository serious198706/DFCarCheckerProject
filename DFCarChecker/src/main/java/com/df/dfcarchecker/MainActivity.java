package com.df.dfcarchecker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        }

    public void EnterProcedureInput(View view) {
        Intent intent = new Intent(this, ProcedureCollectionActivity.class);
        startActivity(intent);
    }

    public void EnterCarWaitingList(View view) {
        Intent intent = new Intent(this, CarWaitingListActivity.class);
        startActivity(intent);
    }

}

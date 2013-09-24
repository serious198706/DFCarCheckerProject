package com.df.dfcarchecker;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class CarWaitingListActivity extends Activity {
    private ArrayList<HashMap<String, String>> mylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_waiting_list);

        ListView list = (ListView) findViewById(R.id.car_waiting_list);

        mylist = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;

        for(int i = 0; i < 20; i++)
        {
            map = new HashMap<String, String>();

            map.put("car_number", "车牌号码：京A2013" + String.format("%s", i));
            map.put("car_type", "行驶本车型：QQ");
            map.put("car_color", "车身颜色：红");
            map.put("car_date", "提交日期：2013-02-23");

            mylist.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                mylist,
                R.layout.car_waiting_list_row,
                new String[] {"car_number", "car_type", "car_color", "car_date"},
                new int[] {R.id.car_number, R.id.car_type, R.id.car_color, R.id.car_date});
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(CarWaitingListActivity.this, mylist.get(i).get("car_number"), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CarWaitingListActivity.this, CarCheckFrameActivity.class);
                startActivity(intent);
            }
        });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car_waiting_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.action_settings:
                Toast.makeText(CarWaitingListActivity.this, "refreshing..", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

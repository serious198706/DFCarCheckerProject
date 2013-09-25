package com.df.dfcarchecker;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CarCheckedListActivity extends Activity {
    private ArrayList<HashMap<String, String>> mylist;
    private ActionMode mActionMode = null;
    private ListView list;
    private int lastPos = 0;
    private SimpleAdapter adapter;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.car_checked_list_context, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_platform:
                    //import();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_modify:
                    mode.finish();
                    return true;
                case R.id.action_print:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            list.clearFocus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_checked_list);

        list = (ListView) findViewById(R.id.car_checked_list);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mylist = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;

        for(int i = 0; i < 20; i++)
        {
            map = new HashMap<String, String>();

            int randomCarNumber = 1000 + (int)(Math.random() * ((9999 - 1000) + 1));

            map.put("car_number", "车牌号码：京A" + String.format("%s", randomCarNumber));
            map.put("car_type", "型号：QQ");
            map.put("car_color", "颜色：红");
            map.put("car_level", "鉴定等级：80B");

            Random r = new Random();
            if(r.nextInt(10) % 2 == 0) {
                map.put("car_photo", "照片：√");
            }
            else {
                map.put("car_photo", "照片：×");
            }

            map.put("car_status", "状态：已竞价");
            map.put("car_date", "提交日期：2013-02-23");

            mylist.add(map);
        }

        adapter = new SimpleAdapter(
                this,
                mylist,
                R.layout.car_checked_list_row,
                new String[] {"car_number", "car_type", "car_color", "car_level", "car_photo", "car_status", "car_date"},
                new int[] {R.id.car_number, R.id.car_type, R.id.car_color, R.id.car_level, R.id.car_photo, R.id.car_status, R.id.car_date});
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(CarCheckedListActivity.this, mylist.get(i).get("car_number"), Toast.LENGTH_SHORT).show();

                lastPos = i;

                view.clearFocus();
                list.clearFocus();
                Intent intent = new Intent(CarCheckedListActivity.this, CarReportFrameActivity.class);
                startActivity(intent);
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           int pos, long id) {
                Toast.makeText(CarCheckedListActivity.this, mylist.get(pos).get("car_number"), Toast.LENGTH_SHORT).show();
                mActionMode = CarCheckedListActivity.this.startActionMode(mActionModeCallback);
                view.setSelected(true);

                return true;
            }
        });

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car_checked_list, menu);
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
            case R.id.action_refresh:
                // 提交数据
                list.setSelection(lastPos);
                list.clearChoices();
                list.clearFocus();
                list.invalidate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

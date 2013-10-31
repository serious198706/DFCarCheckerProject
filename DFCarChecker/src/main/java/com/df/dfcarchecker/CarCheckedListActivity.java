package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.df.entry.Brand;
import com.df.entry.Country;
import com.df.entry.Manufacturer;
import com.df.entry.Series;
import com.df.service.SoapService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private ProgressDialog progressDialog;
    private SoapService soapService;
    private String result;

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
    private RefreshCheckedCarListTask mRefreshCheckedCarListTask;


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
                // 刷新车辆
                list.setSelection(lastPos);
                list.clearChoices();
                list.clearFocus();
                list.invalidate();

                refresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        mRefreshCheckedCarListTask = new RefreshCheckedCarListTask(this);
        mRefreshCheckedCarListTask.execute((Void) null);
    }

    private class RefreshCheckedCarListTask extends AsyncTask<Void, Void, Boolean> {
        Context context;

        private RefreshCheckedCarListTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(context, "提醒",
                    "正在获取车辆信息，请稍候。。", false, false);
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean success = false;

            try {
                JSONObject jsonObject = new JSONObject();

                // SeriesId + userID + key
                jsonObject.put("StartNumber", 1);
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils("http://192.168.100.6:50/ReportService.svc",
                        "http://cheyiju/IReportService/ListCheckedCarsInfoByUserid",
                        "ListCheckedCarsInfoByUserid");

                success = soapService.communicateWithServer(context, jsonObject.toString());

                // TODO: 加入用户是否登录的状态改变

                // 传输失败，获取错误信息并显示
                if(!success) {
                    Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());
                } else {
                    result = soapService.getResultMessage();
                }
            } catch (JSONException e) {
                Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                return false;
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRefreshCheckedCarListTask = null;

            progressDialog.dismiss();

            if (success) {

                try {
                    // 开始位为[，表示传输的是全部信息
                    if(result.startsWith("[")) {
                        JSONArray jsonArray = new JSONArray(result);

                        // 用来存储车辆配置信息的jsonobject list
                        List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
                    }


                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                }
            } else {
                Log.d("DFCarChecker", "连接错误！");
            }
        }

        @Override
        protected void onCancelled() {
            mRefreshCheckedCarListTask = null;
        }
    }
}

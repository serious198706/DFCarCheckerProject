package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
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

import com.df.service.Common;
import com.df.service.SoapService;

import org.apache.http.conn.BasicEofSensorWatcher;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CarCheckedListActivity extends Activity {
    private HashMap<String, String> map;
    private ArrayList<HashMap<String, String>> mylist;
    private ActionMode mActionMode = null;
    private ListView list;
    private int lastPos = 0;
    private SimpleAdapter adapter;

    private ProgressDialog progressDialog;
    private SoapService soapService;
    private String result;

    // 已检测列表的开始位
    private int startNumber = 1;

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
    private GetCarDetailTask mGetCarDetailTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_checked_list);

        list = (ListView) findViewById(R.id.car_checked_list);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mylist = new ArrayList<HashMap<String, String>>();
        map = new HashMap<String, String>();

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        refresh();
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

    private void setList(String jsonString) {
        if(jsonString.startsWith("["))
        {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);

                for(int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    map = new HashMap<String, String>();

                    map.put("id",  jsonObject.getString("id"));
                    map.put("plateNumber", "车牌号码：" + jsonObject.getString("plateNumber"));
                    map.put("brand", "型号：" + jsonObject.getString("brand"));
                    map.put("exteriorColor", "颜色：" + jsonObject.getString("exteriorColor"));
                    map.put("score", "分数：" + jsonObject.getString("score"));

                    String status = jsonObject.getString("status");
                    if(status.equals("1")) {
                        status = "已上传";
                    } else if(status.equals("2")) {
                        status = "已参拍";
                    } else {
                        status = "打回";
                    }

                    map.put("status", "状态：" + status);
                    map.put("created", "创建时间：" + jsonObject.getString("created"));

                    mylist.add(map);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

        }

//        for(int i = 0; i < 20; i++)
//        {
//            map = new HashMap<String, String>();
//
//            int randomCarNumber = 1000 + (int)(Math.random() * ((9999 - 1000) + 1));
//
//            map.put("car_number", "车牌号码：京A" + String.format("%s", randomCarNumber));
//            map.put("car_type", "型号：QQ");
//            map.put("car_color", "颜色：红");
//            map.put("car_level", "鉴定等级：80B");
//
//            Random r = new Random();
//            if(r.nextInt(10) % 2 == 0) {
//                map.put("car_photo", "照片：√");
//            }
//            else {
//                map.put("car_photo", "照片：×");
//            }
//
//            map.put("car_status", "状态：已竞价");
//            map.put("car_date", "提交日期：2013-02-23");
//
//            mylist.add(map);
//        }

        adapter = new SimpleAdapter(
                this,
                mylist,
                R.layout.car_checked_list_row,
                new String[] {"plateNumber", "brand", "exteriorColor", "score", "status", "created"},
                new int[] {R.id.car_number, R.id.car_type, R.id.car_color, R.id.car_level,
                        R.id.car_status, R.id.car_date});
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(CarCheckedListActivity.this, mylist.get(i).get("car_number"), Toast.LENGTH_SHORT).show();

                lastPos = i;

                view.clearFocus();
                list.clearFocus();

                mGetCarDetailTask = new GetCarDetailTask(view.getContext());
                mGetCarDetailTask.execute(Integer.parseInt(mylist.get(i).get("id")));
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
    }



    private void refresh() {
        mRefreshCheckedCarListTask = new RefreshCheckedCarListTask(this);
        mRefreshCheckedCarListTask.execute((Void) null);
    }

    //GetCheckedCarDetailOptionByCarId
    private class RefreshCheckedCarListTask extends AsyncTask<Void, Void, Boolean> {
        Context context;

        private RefreshCheckedCarListTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(context, null,
                    "正在获取已检车辆信息，请稍候。。", false, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            try {
                JSONObject jsonObject = new JSONObject();

                // SeriesId + userID + key
                jsonObject.put("StartNumber", startNumber);
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                        "http://cheyiju/IReportService/ListCheckedCarsInfoByUserid",
                        "ListCheckedCarsInfoByUserid");

                success = soapService.communicateWithServer(context, jsonObject.toString());

                // 传输失败，获取错误信息并显示
                if(!success) {
                    Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());
                } else {
                    result = soapService.getResultMessage();
                    startNumber += 10;
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
                setList(result);
            } else {
                Log.d("DFCarChecker", "连接错误: " + soapService.getErrorMessage());

                if(soapService.getErrorMessage().equals("用户名或Key解析错误，请输入正确的用户Id和Key")) {
                    Toast.makeText(CarCheckedListActivity.this, "连接错误，请重新登陆！", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(CarCheckedListActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        }

        @Override
        protected void onCancelled() {
            mRefreshCheckedCarListTask = null;
        }
    }

    // 获取详细信息
    private class GetCarDetailTask extends AsyncTask<Integer, Void, Boolean> {
        private Context context;

        private SoapService soapService;
        public GetCarDetailTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            boolean success = false;

            soapService = new SoapService();

            soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                    "http://cheyiju/IReportService/GetCheckedCarDetailOptionByCarId",
                    "GetCheckedCarDetailOptionByCarId");

            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("Id", params[0]);
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());
            } catch (JSONException e) {

            }

            success = soapService.communicateWithServer(context, jsonObject.toString());

            // 传输失败，获取错误信息并显示
            if(!success) {
                Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());
            } else {
                result = soapService.getResultMessage();
                startNumber += 10;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Intent intent = new Intent(CarCheckedListActivity.this, CarReportViewPagerActivity.class);
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(getWindow().getDecorView(),
                    getWindow().getDecorView().getWidth() / 2,
                    getWindow().getDecorView().getHeight() / 2,
                    getWindow().getDecorView().getWidth(),
                    getWindow().getDecorView().getHeight());

            intent.putExtra("jsonData", result);
            startActivity(intent, options.toBundle());
        }
    }

    // dummy
    private JSONArray generateDummyJsonObject() {
        JSONArray jsonArray = new JSONArray();

        try {
            for(int i = 0; i < 10; i++) {
                JSONObject jsonObject = new JSONObject();

                int randomCarNumber = 1000 + (int)(Math.random() * ((9999 - 1000) + 1));

                jsonObject.put("plateNumber", "车牌号码: 京A" + Integer.toString(randomCarNumber));
                jsonObject.put("brand", "车辆型号: QQ");

                int randomCarColor = (int)(Math.random() * 12);
                String colorArray[] = getResources().getStringArray(R.array.ci_car_color_arrays);
                jsonObject.put("exteriorColor", "颜色: " + colorArray[randomCarColor]);

                randomCarNumber = (int)(Math.random() * 100);
                jsonObject.put("score", "最终得分: " + Integer.toString(randomCarNumber / 100));
                jsonObject.put("status", "状态: 已竞价");

                int randomMonth = 1 + (int)(Math.random() * ((12 - 1) + 1));
                int randomDay = 1 + (int)(Math.random() * (30 - 1) + 1);

                jsonObject.put("created", "提交日期: 2013-" + Integer.toString(randomMonth) + "-" +
                        Integer.toString(randomDay));

                jsonArray.put(i, jsonObject);
            }
        } catch (JSONException e) {
            return null;
        }

        return jsonArray;
    }
}

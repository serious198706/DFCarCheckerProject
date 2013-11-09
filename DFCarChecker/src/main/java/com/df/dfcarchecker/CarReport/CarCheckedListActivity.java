package com.df.dfcarchecker.CarReport;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.df.dfcarchecker.CarCheck.CarCheckFrameFragment;
import com.df.dfcarchecker.CarCheck.CarCheckViewPagerActivity;
import com.df.dfcarchecker.LoginActivity;
import com.df.dfcarchecker.MainActivity;
import com.df.dfcarchecker.R;
import com.df.service.Common;
import com.df.service.SoapService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private ImportPlatformTask mImportPlatformTask;
    private RefreshCheckedCarListTask mRefreshCheckedCarListTask;
    private GetCarDetailTask mGetCarDetailTask;
    private CheckSellerNameTask mCheckSellerNameTask;
    private ModifyCarTask mModifyCarTask;

    private EditText sellerNameEdit;
    private String sellerId;
    private String sellerName = "";

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
                    checkSellerName();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_modify:
                    editThisCar();
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

    private void editThisCar() {
        mModifyCarTask = new ModifyCarTask(this);
        mModifyCarTask.execute(Integer.parseInt(mylist.get(lastPos).get("id")));
    }


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

    // 设置列表显示内容
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
                lastPos = i;

                view.clearFocus();
                list.clearFocus();

                progressDialog = ProgressDialog.show(CarCheckedListActivity.this, null,
                        "正在获取车辆信息，请稍候。。", false, false);

                mGetCarDetailTask = new GetCarDetailTask(view.getContext());
                mGetCarDetailTask.execute(Integer.parseInt(mylist.get(i).get("id")));
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           int pos, long id) {
                lastPos = pos;

                mActionMode = CarCheckedListActivity.this.startActionMode(mActionModeCallback);
                view.setSelected(true);

                return true;
            }
        });
    }

    // 刷新列表
    private void refresh() {
        mRefreshCheckedCarListTask = new RefreshCheckedCarListTask(this);
        mRefreshCheckedCarListTask.execute((Void) null);
    }

    // 检查卖家姓名
    private void checkSellerName() {
        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.import_platform_dialog, null);

        sellerNameEdit = (EditText)view.findViewById(R.id.sellerName);
        sellerNameEdit.setText(sellerName);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("输入信息")
            .setView(view)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

            sellerName = sellerNameEdit.getText().toString();
            mCheckSellerNameTask = new CheckSellerNameTask
                    (CarCheckedListActivity.this,
                            Integer.parseInt(mylist.get(lastPos).get("id")),
                            sellerName);
            mCheckSellerNameTask.execute();
            }
        })
        .setNegativeButton(R.string.cancel, null)
        .create();

        dialog.show();
    }

    // 导入平台
    private void importPlatform(String sellerId) {
        mImportPlatformTask = new ImportPlatformTask(this, Integer.parseInt(mylist.get(lastPos).get("id")), sellerId);
        mImportPlatformTask.execute();
    }

    // 获取已检车辆线程
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
                        "http://cheyipai/IReportService/ListCheckedCarsInfoByUserid",
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

    // 获取详细信息线程
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
                    "http://cheyipai/IReportService/GetCheckedCarDetailOptionByCarId",
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
//            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(getWindow().getDecorView(),
//                    getWindow().getDecorView().getWidth() / 2,
//                    getWindow().getDecorView().getHeight() / 2,
//                    getWindow().getDecorView().getWidth(),
//                    getWindow().getDecorView().getHeight());

            intent.putExtra("jsonData", result);

            progressDialog.dismiss();

            startActivity(intent/*, options.toBundle()*/);
        }
    }

    // 检查卖家姓名线程
    private class CheckSellerNameTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        int carId;
        String sellerNameValue;

        private CheckSellerNameTask(Context context, int carId, String sellerNameValue) {
            this.context = context;
            this.carId = carId;
            this.sellerNameValue = sellerNameValue;
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
                jsonObject.put("CarId", this.carId);
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());
                jsonObject.put("SellerName", this.sellerNameValue);

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                        "http://cheyipai/IReportService/CheckSellerName",
                        "CheckSellerName");

                success = soapService.communicateWithServer(context, jsonObject.toString());
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

            // 成功获取
            if (success) {
                result = soapService.getResultMessage();

                inputSellerName(result);
            } else {
                Log.d("DFCarChecker", "连接错误: " + soapService.getErrorMessage());

                showErrorDialog();

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

    private void inputSellerName(String result) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(result);
            String companyName = jsonObject.getString("CompanyName");
            String realName = jsonObject.getString("RealName");
            sellerId = jsonObject.getString("SellerId");

            String message = getResources().getString(R.string.importPlatformConfirm) +
                    "\n" +
                    "公司：" + companyName + "\n" +
                    "真实姓名：" + realName;

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            importPlatform(sellerId);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();

            dialog.show();

        } catch (JSONException e) {

        }
    }

    private void showErrorDialog() {
        String message = "未找到卖家，请检查用户名！";

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkSellerName();
                    }
                })
                .create();

        dialog.show();
    }

    // 确认导入平台线程
    private class ImportPlatformTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        int carId;
        String sellerId;

        private ImportPlatformTask(Context context, int carId, String sellerId) {
            this.context = context;
            this.carId = carId;
            this.sellerId = sellerId;
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(context, null,
                    "正在导入平台，请稍候。。", false, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            try {
                JSONObject jsonObject = new JSONObject();

                // SeriesId + userID + key
                jsonObject.put("CarId", this.carId);
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());
                jsonObject.put("SellerId", this.sellerId);

                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                        "http://cheyipai/IReportService/ImportPlatform",
                        "ImportPlatform");

                success = soapService.communicateWithServer(context, jsonObject.toString());

                result = soapService.getResultMessage();
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

            sellerName = "";

            // 成功获取
            if (success) {
                Toast.makeText(context, "导入成功！", Toast.LENGTH_LONG).show();
            } else {
                Log.d("DFCarChecker", "连接错误: " + soapService.getErrorMessage());

                Toast.makeText(context, "导入失败！" + soapService.getErrorMessage(), Toast.LENGTH_LONG).show();

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


    // 获取详细信息线程
    private class ModifyCarTask extends AsyncTask<Integer, Void, Boolean> {
        private Context context;

        private SoapService soapService;
        public ModifyCarTask(Context context) {
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
                    "http://cheyipai/IReportService/GetCheckedCarDetailOptionByCarId",
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

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(!success) {
                Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());
            } else {
                result = soapService.getResultMessage();

                Intent intent = new Intent(CarCheckedListActivity.this,
                        CarCheckViewPagerActivity.class);

                intent.putExtra("edit", true);
                intent.putExtra("JSONData", result);

                progressDialog.dismiss();

                startActivity(intent/*, options.toBundle()*/);
            }


        }
    }
}

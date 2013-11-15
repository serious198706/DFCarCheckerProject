package com.df.dfcarchecker.CarCheck;

import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.df.dfcarchecker.CarReport.CarCheckedListActivity;
import com.df.dfcarchecker.R;
import com.df.service.Common;
import com.df.service.CustomViewPager;
import com.df.service.QueueScanService;

import org.json.JSONException;
import org.json.JSONObject;

public class CarCheckViewPagerActivity extends FragmentActivity implements ActionBar
        .TabListener, CarCheckBasicInfoFragment.OnHeadlineSelectedListener{
    private CarCheckBasicInfoFragment carCheckBasicInfoFragment;
    private CarCheckFrameFragment carCheckFrameFragment;
    private CarCheckIntegratedFragment carCheckIntegratedFragment;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    CustomViewPager mViewPager;

    public static ProgressDialog mCommitProgressDialog;

    private static boolean canCommit = false;

    private Intent serviceIntent;

    private boolean integratedUpdated = false;
    private boolean frameUpdated = false;

    // 用于修改
    private String jsonData = "";
    private int carId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check_viewpager);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            if(bundle.containsKey("edit")) {
                jsonData = bundle.getString("JSONData");
                carId = bundle.getInt("carId");
            } else {
                jsonData = "";
            }
        }

        carCheckBasicInfoFragment = new CarCheckBasicInfoFragment(jsonData);
        carCheckFrameFragment = new CarCheckFrameFragment(jsonData);
        carCheckIntegratedFragment = new CarCheckIntegratedFragment(jsonData);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (CustomViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(false);

        // 让viewPager一次性缓存3个页面，可以提高viewPager的流畅性，也可以保存fragment的数据，在
        // fragment来回切换时数据不会丢失
        mViewPager.setOffscreenPageLimit(3);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        serviceIntent = new Intent(this, QueueScanService.class);
        serviceIntent.putExtra("committed", canCommit);
        serviceIntent.putExtra("JSONObject", "");
        serviceIntent.putExtra("action", "");
        serviceIntent.putExtra("carId", carId);
        startService(serviceIntent);
        registerReceiver(broadcastReceiver, new IntentFilter(QueueScanService.BROADCAST_ACTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_commit_cancle, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        destroyEntities();

        unregisterReceiver(broadcastReceiver);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                quitCarCheck();
                return true;
            case R.id.action_commit:
                // 提交数据
                if(runOverAllCheck())
                    commit();
                break;
            case R.id.action_cancel:
                quitCarCheck();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        if(tab.getPosition() == 2 && !jsonData.equals("") && !integratedUpdated) {
            carCheckIntegratedFragment.letsEnterModifyMode();
            integratedUpdated = true;
        }

        if(tab.getPosition() == 1 && !jsonData.equals("") && !frameUpdated) {
            carCheckFrameFragment.letsEnterModifyMode();
            frameUpdated = true;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new Fragment();
            switch (position)
            {
                case 0:
                    fragment = carCheckBasicInfoFragment;
                    break;
                case 1:
                    fragment = carCheckFrameFragment;
                    break;
                case 2:
                    fragment = carCheckIntegratedFragment;
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    public void quitCarCheck() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_title);
        if(!jsonData.equals("")) {
            builder.setMessage(R.string.quitCarModify);
        } else {
            builder.setMessage(R.string.quitCarCheck);
        }

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 退出
                destroyEntities();
                Intent intent = new Intent(CarCheckViewPagerActivity.this, QueueScanService.class);
                stopService(intent);
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void destroyEntities() {
        if(CarCheckFrameFragment.photoEntitiesFront != null) {
            CarCheckFrameFragment.photoEntitiesFront.clear();
        }
        if(CarCheckFrameFragment.photoEntitiesRear != null) {
            CarCheckFrameFragment.photoEntitiesRear.clear();
        }
        if(CarCheckFrameFragment.posEntitiesFront != null) {
            CarCheckFrameFragment.posEntitiesFront.clear();
        }
        if(CarCheckFrameFragment.posEntitiesRear != null) {
            CarCheckFrameFragment.posEntitiesRear.clear();
        }
        if(CarCheckIntegratedFragment.exteriorPosEntities != null) {
            CarCheckIntegratedFragment.exteriorPosEntities.clear();
        }
        if(CarCheckIntegratedFragment.exteriorPhotoEntities != null) {
            CarCheckIntegratedFragment.exteriorPhotoEntities.clear();
        }
        if(CarCheckIntegratedFragment.interiorPosEntities != null) {
            CarCheckIntegratedFragment.interiorPosEntities.clear();
        }
        if(CarCheckIntegratedFragment.interiorPhotoEntities != null) {
            CarCheckIntegratedFragment.interiorPhotoEntities.clear();
        }
        if(CarCheckExteriorActivity.posEntities != null) {
            CarCheckExteriorActivity.posEntities.clear();
        }
        if(CarCheckInteriorActivity.posEntities != null) {
            CarCheckInteriorActivity.posEntities.clear();
        }
    }


    public void commit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_title);

        if(!jsonData.equals("")) {
            builder.setMessage(R.string.commitCarModify);
        } else {
            builder.setMessage(R.string.commitCarCheck);
        }

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // 提交

                // 将结构检查的图片加入到照片池
                mCommitProgressDialog = new ProgressDialog(CarCheckViewPagerActivity.this);
                mCommitProgressDialog.setMessage("正在提交");
                mCommitProgressDialog.setIndeterminate(true);
                mCommitProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mCommitProgressDialog.setCanceledOnTouchOutside(false);
                mCommitProgressDialog.show();

                // 修改车辆信息
                if(!jsonData.equals("")) {
                    canCommit = true;

                    JSONObject finalJsonObject = new JSONObject();

                    // 单独提取出photo串，photo暂时不修改
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONObject photos = jsonObject.getJSONObject("photos");

                        String tradeCode;
                        String totalScore;
                        String checkUserId;
                        String createdDate;
                        String sellerId;

                        finalJsonObject = generateCommitJsonObject().put("photos", photos);

                        if(jsonObject.has("tradeCode")) {
                            tradeCode = jsonObject.getString("tradeCode");
                            finalJsonObject.put("tradeCode", tradeCode);
                        }
                        if(jsonObject.has("totalScore")) {
                            totalScore = jsonObject.getString("totalScore");
                            finalJsonObject.put("totalScore", totalScore);
                        }

                        if(jsonObject.has("checkUserId")) {
                            checkUserId = jsonObject.getString("checkUserId");
                            finalJsonObject.put("checkUserId", checkUserId);
                        }

                        if(jsonObject.has("createdDate")) {
                            createdDate = jsonObject.getString("createdDate");
                            finalJsonObject.put("createdDate", createdDate);
                        }

                        if(jsonObject.has("sellerId")) {
                            sellerId = jsonObject.getString("sellerId");
                            finalJsonObject.put("sellerId", sellerId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // 修改车辆信息要放入carId
                    serviceIntent.putExtra("committed", canCommit);
                    serviceIntent.putExtra("action", "modify");
                    serviceIntent.putExtra("carId", carId);
                    serviceIntent.putExtra("JSONObject", finalJsonObject.toString());
                    startService(serviceIntent);
                } else {
                    carCheckFrameFragment.addPhotosToQueue();
                    canCommit = true;

                    serviceIntent.putExtra("committed", canCommit);
                    serviceIntent.putExtra("action", "commit");
                    serviceIntent.putExtra("carId", 0);
                    serviceIntent.putExtra("JSONObject", generateCommitJsonObject().toString());
                    startService(serviceIntent);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        quitCarCheck();
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mCommitProgressDialog != null) {
                mCommitProgressDialog.dismiss();
                String result = intent.getExtras().getString("result");

                // 提交成功
                if(result.equals("0")) {
                    Toast.makeText(CarCheckViewPagerActivity.this, "提交成功！", Toast.LENGTH_LONG).show();
                    // 停止服务
                    if(!jsonData.equals("")) {
                        Intent serviceIntent = new Intent(CarCheckViewPagerActivity.this, QueueScanService.class);
                        stopService(serviceIntent);
                        finish();
                    } else {
                        String score = intent.getExtras().getString("score");

                        String exterior = "外观检查得分：";
                        String interior = "内饰检查得分：";
                        String engine = "发动机检查得分：";
                        String gearbox = "变速箱检查得分：";
                        String function = "功能检查得分：";
                        String total = "总分：";

                        try {
                            JSONObject jsonObject = new JSONObject(score);
                            exterior += jsonObject.getString("exterior");
                            interior += jsonObject.getString("interior");
                            engine += jsonObject.getString("engine");
                            gearbox += jsonObject.getString("gearbox");
                            function += jsonObject.getString("function");

                            Float totalScore = Float.parseFloat(jsonObject.getString("exterior"))
                                    + Float.parseFloat(jsonObject.getString("interior"))
                                    + Float.parseFloat(jsonObject.getString("engine"))
                                    + Float.parseFloat(jsonObject.getString("gearbox"))
                                    + Float.parseFloat(jsonObject.getString("function"));

                            total += Float.toString(totalScore);
                        } catch (JSONException e) {

                        }

                        String msg = exterior + "\n" + interior + "\n" + engine + "\n" + gearbox +
                                "\n" + function + "\n\n" + total;

                        // 显示得分，关闭界面
                        AlertDialog dialog = new AlertDialog.Builder(CarCheckViewPagerActivity.this)
                                .setTitle("车辆得分")
                                //.setView(getLayoutInflater().inflate(R.layout.score_dialog, null))
                                .setMessage(msg)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent1 = new Intent(CarCheckViewPagerActivity.this,
                                                CarCheckedListActivity.class);
                                        startActivity(intent1);

                                        finish();
                                    }
                                })
                                .create();

                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                } else {
                    String error = intent.getExtras().getString("errorMsg");
                    Toast.makeText(CarCheckViewPagerActivity.this, "提交失败！" + error,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private boolean runOverAllCheck() {
        boolean checkThrough = false;

        checkThrough = carCheckBasicInfoFragment.runOverAllCheck();
        if(!checkThrough) {
            mViewPager.setCurrentItem(0);
            return checkThrough;
        }

        if(!jsonData.equals("")) {
            return true;
        }

        checkThrough = carCheckFrameFragment.runOverAllCheck();
        if(!checkThrough) {
            mViewPager.setCurrentItem(1);
            return checkThrough;
        }

        checkThrough = carCheckIntegratedFragment.runOverAllCheck();
        if(!checkThrough) {
            mViewPager.setCurrentItem(2);
            return checkThrough;
        }

        return checkThrough;
    }

    private JSONObject generateCommitJsonObject() {
        try {
            // root节点
            JSONObject root = new JSONObject();

            // 基本信息
            JSONObject features = new JSONObject();

            // 基本信息 - 配置信息
            features.put("options", carCheckBasicInfoFragment.generateOptionsJsonObject());

            // 基本信息 - 手续信息
            features.put("procedures", carCheckBasicInfoFragment.generateProceduresJsonObject());

            // 综合检查
            JSONObject conditions = new JSONObject();

            // 综合检查 - 外观检查
            if(CarCheckExteriorActivity.generateExteriorJsonObject() == null) {
                JSONObject exterior = new JSONObject(jsonData).getJSONObject("conditions")
                        .getJSONObject("exterior");
                conditions.put("exterior", exterior);
            } else {
                if(!jsonData.equals("")) {
                    JSONObject exterior = new JSONObject(jsonData).getJSONObject("conditions")
                            .getJSONObject("exterior");

                    JSONObject temp = CarCheckExteriorActivity.generateExteriorJsonObject();
                    temp.put("score", exterior.getString("score"));
                    conditions.put("exterior", temp);
                } else {
                    conditions.put("exterior", CarCheckExteriorActivity
                            .generateExteriorJsonObject());
                }
            }

            // 综合检查 - 内饰检查
            if(CarCheckInteriorActivity.generateInteriorJsonObject() == null) {
                JSONObject interior = new JSONObject(jsonData).getJSONObject("conditions")
                        .getJSONObject("interior");
                conditions.put("interior", interior);
            } else {
                if(!jsonData.equals("")) {
                    JSONObject interior = new JSONObject(jsonData).getJSONObject("conditions")
                            .getJSONObject("interior");

                    JSONObject temp = CarCheckInteriorActivity.generateInteriorJsonObject();
                    temp.put("score", interior.getString("score"));
                    conditions.put("interior", temp);
                } else {
                    conditions.put("interior", CarCheckInteriorActivity
                            .generateInteriorJsonObject());
                }
            }

            // 综合检查 - 发动机检查
            conditions.put("engine", CarCheckIntegratedFragment.generateEngineJsonObject());

            // 综合检查 - 变速箱检查
            conditions.put("gearbox", CarCheckIntegratedFragment.generateGearboxJsonObject());

            // 综合检查 - 功能检查
            conditions.put("function", CarCheckIntegratedFragment.generateFunctionJsonObject());

            // 综合检查 - 底盘检查
            //conditions.put("chassis", CarCheckIntegratedFragment.generateChassisJsonObject());

            // 综合检查 - 泡水检查
            conditions.put("flooded", CarCheckIntegratedFragment.generateFloodedJsonObject());

            // 综合检查 - 备注
            conditions.put("comment", CarCheckIntegratedFragment.generateCommentString());

            // 车体结构检查
            JSONObject frames = new JSONObject();

            // 车体结构检查 - 备注
            frames.put("comment", CarCheckFrameFragment.generateFrameJsonString());

            root.put("features", features);
            root.put("conditions", conditions);
            root.put("frames", frames);

            return root;
        } catch (JSONException e) {
            Log.d(Common.TAG, "Json组织失败。");

            return null;
        }
    }

    public void onUpdateIntegratedUi() {
        carCheckBasicInfoFragment.littleFixAboutRegArea();
       // carCheckFrameFragment.letsEnterModifyMode();
       // carCheckIntegratedFragment.letsEnterModifyMode();
    }
}

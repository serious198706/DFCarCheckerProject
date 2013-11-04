package com.df.dfcarchecker;

import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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

import com.df.service.Common;
import com.df.service.CustomViewPager;
import com.df.service.SoapService;

import org.json.JSONException;
import org.json.JSONObject;

public class CarCheckViewPagerActivity extends FragmentActivity implements ActionBar.TabListener {
    private CarCheckBasicInfoFragment carCheckBasicInfoFragment;
    private CarCheckFrameFragment carCheckFrameFragment;
    private CarCheckIntegratedFragment carCheckIntegratedFragment;

    private CommitDataTask mCommitDataTask;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check_viewpager);

        carCheckBasicInfoFragment = new CarCheckBasicInfoFragment();
        carCheckFrameFragment = new CarCheckFrameFragment();
        carCheckIntegratedFragment = new CarCheckIntegratedFragment();

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_commit_cancle, menu);
        return true;
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
        builder.setMessage(R.string.quitCarCheck);
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

    public void commit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_title);
        builder.setMessage(R.string.commitCarCheck);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 提交
                mCommitDataTask = new CommitDataTask();
                mCommitDataTask.execute((Void) null);

                Toast.makeText(CarCheckViewPagerActivity.this, "提交成功！", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        quitCarCheck();
    }


    public class CommitDataTask extends AsyncTask<Void, Void, Boolean> {
        Context context;
        SoapService soapService;

        private CommitDataTask() {

        }

        private CommitDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            try {
                // 组织车辆检测信息的json
                JSONObject root = new JSONObject();

                // 基本信息
                JSONObject features = new JSONObject();

                JSONObject procedures = new JSONObject();

                // 综合检查
                JSONObject conditions = new JSONObject();


                JSONObject exterior = new JSONObject();

                // 综合检查 - 内饰检查
                JSONObject interior = new JSONObject();

                // 综合检查 - 发动机检查
                JSONObject engine = new JSONObject();

                // 综合检查 - 变速箱检查
                JSONObject gearbox = new JSONObject();

                // 综合检查 - 功能检查
                JSONObject function = new JSONObject();

                // 综合检查 - 底盘检查
                JSONObject chassis = new JSONObject();

                // 综合检查 - 泡水检查
                JSONObject flooded = new JSONObject();

                // 综合检查 - 备注
                JSONObject comment = new JSONObject();


                // 基本信息 - 配置信息
                features.put("options", carCheckBasicInfoFragment.generateOptionsJsonString());

                // 基本信息 - 手续信息
                features.put("procedures", carCheckBasicInfoFragment.generateProceduresJsonString());

                // 综合检查 - 外观检查
                conditions.put("exterior", CarCheckExteriorActivity.generateExteriorJsonString());

                // 综合检查 - 内饰检查m
                conditions.put("interior", CarCheckInteriorActivity.generateInteriorJsonString());


                conditions.put("engine", engine.toString());
                conditions.put("gearbox", gearbox.toString());
                conditions.put("function", function.toString());
                conditions.put("chassis", chassis.toString());
                conditions.put("flooded", flooded.toString());
                conditions.put("comment", comment.toString());

                root.put("features", features.toString());
                root.put("conditions", conditions.toString());
                soapService = new SoapService();

                // 设置soap的配置
                soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                        "http://cheyiju/IUserManageService/SaveCarCheckInfo",
                        "SaveCarCheckInfo");

                success = soapService.communicateWithServer(context, root.toString());

                // 登录失败，获取错误信息并显示
                if(!success) {
                    Log.d("DFCarChecker", "Login error:" + soapService.getErrorMessage());
                } else {

                }
            } catch (JSONException e) {
                Log.d("DFCarChecker", "Json解析错误: " + e.getMessage());
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCommitDataTask = null;


        }

        @Override
        protected void onCancelled() {
            mCommitDataTask = null;
        }
    }
}
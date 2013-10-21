package com.df.dfcarchecker;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.df.service.Common;

import java.util.ArrayList;
import java.util.List;

public class PopupActivity extends Activity {
    private TableLayout root;
    private String RESULT_TYPE = "";
    private String brokenParts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        root = null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("POPUP_TYPE");
            if(value.equals("OUT_GLASS")) {
                SetOutGlassLayout();
            } else if(value.equals("OUT_SCREW")) {
                SetOutScrewLayout();
            } else if(value.equals("OUT_BROKEN")) {
                String brokenParts = extras.getString("BROKEN_PARTS");
                SetOutBrokenLayout(brokenParts);
            } else if(value.equals("IN_BROKEN")) {
                SetInBrokenLayout();
            } else if(value.equals("IN_DIRTY")) {
                SetInDirtyLayout();
            }

        }

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.popup, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_done:
                ReturnResult(CollectCheckedCheckBoxText());
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void SetOutGlassLayout() {
        RESULT_TYPE = Common.OUT_GLASS_RESULT;
    }

    private void SetOutScrewLayout() {
        RESULT_TYPE = Common.OUT_SCREW_RESULT;

        setTitle(getResources().getString(R.string.ac_screw_parts));

        String[] checkBoxTextArray = getResources().getStringArray(R.array.ac_screw_item);

        RenderChildTree(checkBoxTextArray, null);
    }

    // 车身检查 - 破损
    private void SetOutBrokenLayout(String brokenParts) {
        // 破损部位以"0,1,4,7,19"的方式传输
        int[] partArray = null;

        if(brokenParts != null) {
            String[] parts = brokenParts.split(",");

            partArray = new int[parts.length];

            for(int i = 0; i < parts.length; i++) {
                partArray[i] = Integer.parseInt(parts[i]);
            }
        }

        RESULT_TYPE = Common.OUT_BROKEN_RESULT;

        setTitle(getResources().getString(R.string.out_broken_parts));

        String[] checkBoxTextArray = getResources().getStringArray(R.array.out_broken_parts);

        RenderChildTree(checkBoxTextArray, partArray);
    }

    // 内饰检查 - 破损
    private void SetInBrokenLayout() {
        RESULT_TYPE = Common.IN_BROKEN_RESULT;

        setTitle(getResources().getString(R.string.in_broken_parts));

        String[] checkBoxTextArray = getResources().getStringArray(R.array.in_broken_parts_item);

        RenderChildTree(checkBoxTextArray, null);
    }

    // 内饰检查 - 脏污
    private void SetInDirtyLayout() {
        RESULT_TYPE = Common.IN_DIRTY_RESULT;

        setTitle(getResources().getString(R.string.in_dirty_parts));

        String[] checkBoxTextArray = getResources().getStringArray(R.array.in_dirty_parts_item);

        RenderChildTree(checkBoxTextArray, null);
    }


    private void RenderChildTree(String[] checkBoxTextArray, int[] parts) {
        int count = 0;

        root = (TableLayout)findViewById(R.id.root);

        int length = checkBoxTextArray.length;

        // 排多少行
        int row = (length % 3 == 0) ? (length / 3) : (length /3 + 1);

        // 每一行
        for(int i = 0; i < row - 1; i++) {
            TableRow tableRow = new TableRow(this);

            for(int j = 0; j < 3; j++) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(checkBoxTextArray[i * 3 + j]);
                checkBox.setTextSize(20);
                TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                checkBox.setLayoutParams(params);
                tableRow.addView(checkBox);

                // 如果计数器存在于数组中，即选中状态
                if(parts != null) {
                    if(exist(count, parts)) {
                        checkBox.setChecked(true);
                    }
                }

                count++;
            }

            root.addView(tableRow);
        }

        // 最后一行单独处理
        TableRow lastRow = new TableRow(this);

        for(int i = 0; i < length - (row - 1) * 3; i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(checkBoxTextArray[(row - 1) * 3 + i]);
            checkBox.setTextSize(20);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            checkBox.setLayoutParams(params);
            lastRow.addView(checkBox);
        }

        // 为了保持最后一行的整齐，要填充多余的TextView
        for(int i = 0; i < (3 - (length - (row - 1) * 3)); i++) {
            TextView textView = new TextView(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            textView.setLayoutParams(params);
            lastRow.addView(textView);
        }

        root.addView(lastRow);
    }


    private String CollectCheckedCheckBoxText() {
        String result = "";
        brokenParts = "";

        int count = 0;
        int rowCount = root.getChildCount();

        for(int i = 0; i < rowCount; i++)
        {
            TableRow row = (TableRow)root.getChildAt(i);

            for(int j = 0; j < row.getChildCount(); j++) {
                View v = row.getChildAt(j);

                if(v instanceof CheckBox)
                {
                    // you got the spinner
                    CheckBox s = (CheckBox) v;

                    if(s.isChecked()) {
                        result += s.getText();
                        result += ",";

                        brokenParts += Integer.toString(count);
                        brokenParts += ",";
                    }
                }

                count++;
            }
        }

        if(brokenParts != "") {
            brokenParts = brokenParts.substring(0, brokenParts.length() - 1);
        }

        // 去掉最后一个","
        if(!result.equals("")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    private void ReturnResult(String result) {
        // 创建结果意图和包括地址
        Intent intent = new Intent();
        intent.putExtra(RESULT_TYPE, result);
        intent.putExtra("BROKEN_PARTS", brokenParts);

        // 关闭activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private boolean exist(int num, int[] array) {
        for(int i = 0; i < array.length; i++) {
            if(num == array[i]) {
                return true;
            }
        }

        return false;
    }
}

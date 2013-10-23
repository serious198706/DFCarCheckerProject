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

public class PopupActivity extends Activity {
    private TableLayout root;
    private String RESULT_TYPE = "";
    private String partsString;
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
                String brokenParts = extras.getString("BROKEN_PARTS");
                String dirtyParts = extras.getString("DIRTY_PARTS");
                SetInBrokenLayout(brokenParts, dirtyParts);
            } else if(value.equals("IN_DIRTY")) {
                String dirtyParts = extras.getString("DIRTY_PARTS");
                String brokenParts = extras.getString("BROKEN_PARTS");
                SetInDirtyLayout(dirtyParts, brokenParts);
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

    // 外观检查 - 破损
    private void SetOutBrokenLayout(String brokenParts) {
        // 破损部位以"xxxx,xx,xxxxx"的方式传输
        String[] parts = {};

        if(brokenParts != null && !brokenParts.equals("")) {
            parts = brokenParts.split(",");
        }

        RESULT_TYPE = Common.OUT_BROKEN_RESULT;

        setTitle(getResources().getString(R.string.out_broken_parts));

        String[] checkBoxTextArray = getResources().getStringArray(R.array.out_broken_parts);

        RenderChildTree(checkBoxTextArray, parts);
    }

    // 内饰检查 - 破损
    private void SetInBrokenLayout(String brokenParts, String dirtyParts) {
        // 破损部位以"xxxx,xx,xxxxx"的方式传输
        // brokenParts不能为空，且不能为""
        String[] parts = {};

        if(brokenParts != null && !brokenParts.equals("")) {
            parts = brokenParts.split(",");
        }

        String[] disableParts = {};

        if(dirtyParts != null && !dirtyParts.equals("")) {
            disableParts = dirtyParts.split(",");
        }

        RESULT_TYPE = Common.IN_BROKEN_RESULT;

        setTitle(getResources().getString(R.string.in_broken_parts));

        String[] checkBoxTextArray = getResources().getStringArray(R.array.in_broken_parts_item);

        RenderChildTree(checkBoxTextArray, parts, disableParts);
    }

    // 内饰检查 - 脏污
    private void SetInDirtyLayout(String dirtyParts, String brokenParts) {
        // 破损部位以"xxxx,xx,xxxxx"的方式传输
        String[] parts = {};

        if(dirtyParts != null && !dirtyParts.equals("")) {
            parts = dirtyParts.split(",");
        }

        String[] disableParts = {};

        if(brokenParts != null && !brokenParts.equals("")) {
            disableParts = brokenParts.split(",");
        }

        RESULT_TYPE = Common.IN_DIRTY_RESULT;

        setTitle(getResources().getString(R.string.in_dirty_parts));

        String[] checkBoxTextArray = getResources().getStringArray(R.array.in_dirty_parts_item);

        RenderChildTree(checkBoxTextArray, parts, disableParts);
    }


    private void RenderChildTree(String[] checkBoxTextArray, String[] parts) {
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

                // 如果该项存在于parts中，即选中状态
                if(parts != null) {
                    if(exist(checkBoxTextArray[i * 3 + j], parts)) {
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

    private void RenderChildTree(String[] checkBoxTextArray, String[] parts, String[] disableParts) {
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
                checkBox.setEnabled(true);
                TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                checkBox.setLayoutParams(params);
                tableRow.addView(checkBox);

                // 如果计数器存在于数组中，即选中状态
                if(parts != null) {
                    if(exist(checkBoxTextArray[i * 3 + j], parts)) {
                        checkBox.setChecked(true);
                    }
                }

                if(disableParts != null) {
                    if(exist(checkBoxTextArray[i * 3 + j], disableParts)) {
                        checkBox.setEnabled(false);
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
        partsString = "";

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

                        partsString += s.getText();
                        partsString += ",";
                    }
                }
            }
        }

        if(partsString != "" && partsString != null) {
            partsString = partsString.substring(0, partsString.length() - 1);
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

        // 如果是外观或内饰破损
        if(RESULT_TYPE.equals(Common.IN_BROKEN_RESULT) ||
                RESULT_TYPE.equals(Common.OUT_BROKEN_RESULT)) {
            intent.putExtra("BROKEN_PARTS", partsString);
        }
        // 如果是内饰脏污
        else {
            intent.putExtra("DIRTY_PARTS", partsString);
        }

        // 关闭activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private boolean exist(String part, String[] parts) {
        for(int i = 0; i < parts.length; i++) {
            if(part.equals(parts[i])) {
                return true;
            }
        }

        return false;
    }
}

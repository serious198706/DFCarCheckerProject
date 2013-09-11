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

import com.df.service.Common;

public class PopupActivity extends Activity {
    private TableLayout root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        root = null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("POPUP_TYPE");
            if(value.equals("GLASS")) {
                SetGlassLayout();
            } else if(value.equals("SCREW")) {
                SetScrewLayout();
            } else if(value.equals("BROKEN")) {
                SetBrokenLayout();
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


    private void SetGlassLayout() {

    }

    private void SetScrewLayout() {

    }

    private void SetBrokenLayout() {
        setTitle(getResources().getString(R.string.out_broken));
        root = (TableLayout)findViewById(R.id.root);

        String[] checkBoxTextArray = getResources().getStringArray(R.array.out_broken_parts);

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

        root.addView(lastRow);
    }

    private String CollectCheckedCheckBoxText() {
        String result = "";

        int count = root.getChildCount();

        for(int i = 0; i < count; i++)
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
                    }
                }
            }
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
        intent.putExtra(Common.BROKEN_RESULT, result);

        // 结果，完成这项活动
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

package com.df.dfcarchecker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import com.df.service.Helper;

import java.util.List;

/**
 * Created by 岩 on 13-9-2.
 */
public class ProcedureInputCarInfoFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private static LayoutInflater inflater;
    private Dialog dialog;
    private boolean match;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.activity_procedureinput_ci, container, false);

        SetCarTypeSpinner();
        SetFirstLogTimeSpinner();
        SetCarColorSpinner();
        SetManufactureTimeSpinner();
        SetLastTransferTimeSpinner();
        SetRegLocationSpinner();
        SetTransferCountSpinner();

        Button button = (Button) rootView.findViewById(R.id.picture_match_button);
        button.setOnClickListener(this);

        dialog = new Dialog(rootView.getContext(), R.style.Theme_Dialog);
        dialog.setContentView(R.layout.ci_dialog);

        Spinner ci_violated_spinner = (Spinner) rootView.findViewById(R.id.ci_violated_spinner);
        ci_violated_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                switch (position){
                    case 0:
                    {
                        TableRow tableRow1 = (TableRow) rootView.findViewById(R.id.ci_violated_area1);
                        tableRow1.setVisibility(View.GONE);
                        TableRow tableRow2 = (TableRow) rootView.findViewById(R.id.ci_violated_area2);
                        tableRow2.setVisibility(View.GONE);
                        break;
                    }
                    case 1:
                    {
                        TableRow tableRow1 = (TableRow) rootView.findViewById(R.id.ci_violated_area1);
                        tableRow1.setVisibility(View.VISIBLE);
                        TableRow tableRow2 = (TableRow) rootView.findViewById(R.id.ci_violated_area2);
                        tableRow2.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.picture_match_button:
                PictureMatch(v);
                break;
        }
    }

    // 行驶证车辆类型
    private void SetCarTypeSpinner()
    {
        String[] carTypeArray = getResources().getStringArray(R.array.ci_car_type_arrays);
        List<String> carTypeList = Helper.StringArray2List(carTypeArray);

        SetSpinnerData(R.id.ci_car_type_spinner, carTypeList);
    }

    // 初次登记时间
    private void SetFirstLogTimeSpinner()
    {
        SetSpinnerData(R.id.ci_first_log_year_spinner, Helper.GetYearList(21));
        SetSpinnerData(R.id.ci_first_log_month_spinner, Helper.GetMonthList());
    }

    // 车身颜色
    private void SetCarColorSpinner()
    {
        String[] colorArray = getResources().getStringArray(R.array.ci_car_color_arrays);
        List<String> colorList = Helper.StringArray2List(colorArray);

        SetSpinnerData(R.id.ci_car_color_spinner, colorList);
    }

    // 出厂日期
    private void SetManufactureTimeSpinner()
    {
        SetSpinnerData(R.id.ci_manufacture_year_spinner, Helper.GetYearList(21));
        SetSpinnerData(R.id.ci_manufacture_month_spinner, Helper.GetMonthList());
    }


    // 过户次数
    private void SetTransferCountSpinner()
    {
        SetSpinnerData(R.id.ci_transfer_count_spinner, Helper.GetMonthList());
    }

    // 最后过户时间
    private void SetLastTransferTimeSpinner()
    {
        SetSpinnerData(R.id.ci_last_transfer_year_spinner, Helper.GetYearList(17));
        SetSpinnerData(R.id.ci_last_transfer_month_spinner, Helper.GetMonthList());
    }

    // 注册地
    private void SetRegLocationSpinner()
    {
        String[] provinceArray = getResources().getStringArray(R.array.ci_province);
        List<String> province = Helper.StringArray2List(provinceArray);
        SetSpinnerData(R.id.ci_reg_location_spinner, province);
    }

    private void SetSpinnerData(int redID, List<String> list)
    {
        Spinner spinner = (Spinner)rootView.findViewById(redID);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, list);

        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public void PictureMatch(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

        // Get the layout inflater
        LayoutInflater inflater = this.inflater;

        builder.setTitle("注意");
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.ci_dialog, null));

        //builder.setMessage(R.string.ci_attention_content).setTitle(R.string.ci_attention);
        builder.setPositiveButton(R.string.ci_attention_match, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 相符
                match = true;
                EditText editText = (EditText)rootView.findViewById(R.id.picture_match_edit);
                String matches = getResources().getString(R.string.ci_attention_match);
                String notmatch = getResources().getString(R.string.ci_attention_notmatch);
                editText.setText(match ? matches : notmatch);
            }
        });
        builder.setNegativeButton(R.string.ci_attention_notmatch, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 不符
                match = false;
                EditText editText = (EditText)rootView.findViewById(R.id.picture_match_edit);
                String matches = getResources().getString(R.string.ci_attention_match);
                String notmatch = getResources().getString(R.string.ci_attention_notmatch);
                editText.setText(match ? matches : notmatch);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

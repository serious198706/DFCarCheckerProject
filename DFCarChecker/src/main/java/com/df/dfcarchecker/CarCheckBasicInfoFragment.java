package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.df.service.Common;
import com.df.service.Helper;

import java.util.List;

import static com.df.service.Helper.SetSpinnerData;

public class CarCheckBasicInfoFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;
    private int currentGroup;
    private ImageView img;
    private TableLayout tableLayout;
    private LinearLayout brand;
    private EditText vin_edit;
    private Button brandOkButton;
    private Button brandSelectButton;
    private boolean match;

    // 每一个部位的序号关联：
    // 第一个表示序号，第二个表示该序号所对应的TableRow，第三个表示该TableRow是否应该显示
    private int[][] csi_map = {
            {0, R.id.csi_airbag, View.GONE},
            {1, R.id.csi_abs, View.GONE},
            {2, R.id.csi_turn_help, View.GONE},
            {3, R.id.csi_ele_windows, View.GONE},
            {4, R.id.csi_sky_light, View.GONE},
            {5, R.id.csi_air_conditioner, View.GONE},
            {6, R.id.csi_feather_seat, View.GONE},
            {7, R.id.csi_ele_seat, View.GONE},
            {8, R.id.csi_ele_reflect_mirror, View.GONE},
            {9, R.id.csi_parking_sensors, View.GONE},
            {10, R.id.csi_parking_video, View.GONE},
            {11, R.id.csi_ccs, View.GONE},
            {12, R.id.csi_soft_close_doors, View.GONE},
            {13, R.id.csi_rear_ele_seats, View.GONE},
            {14, R.id.csi_auto_chassis, View.GONE},
            {15, R.id.csi_auto_parking, View.GONE},
            {16, R.id.csi_curtain, View.GONE}
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_basic_info, container, false);

        img = (ImageView) rootView.findViewById(R.id.image);

        tableLayout = (TableLayout) rootView.findViewById(R.id.bi_content_table);
        tableLayout.setVisibility(View.GONE);

        brand = (LinearLayout) rootView.findViewById(R.id.brand_input);

        Button cameraButton = (Button) rootView.findViewById(R.id.cbi_start_camera_button);
        cameraButton.setOnClickListener(this);

        Button vinButton = (Button) rootView.findViewById(R.id.bi_vin_button);
        vinButton.setOnClickListener(this);

        brandOkButton = (Button) rootView.findViewById(R.id.bi_brand_ok_button);
        brandOkButton.setEnabled(false);
        brandOkButton.setOnClickListener(this);

        brandSelectButton = (Button) rootView.findViewById(R.id.bi_brand_select_button);
        brandSelectButton.setEnabled(false);
        brandSelectButton.setOnClickListener(this);

        Button matchButton = (Button) rootView.findViewById(R.id.picture_match_button);
        matchButton.setOnClickListener(this);

        vin_edit = (EditText) rootView.findViewById(R.id.bi_vin_edit);

        // TODO：此序列号来自服务器
        int[] whichToShowFromServer = null;

        HandelCSITableRow(whichToShowFromServer);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cbi_start_camera_button:
                cbi_start_camera(v);
                break;
            case R.id.bi_vin_button:
                bi_brand_show();
                break;
            case R.id.bi_brand_ok_button:
                bi_content_show();
                break;
            case R.id.picture_match_button:
                PictureMatch(v);
                break;
        }
    }

    public void cbi_start_camera(View view) {
        // 开始拍摄其他组
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

        builder.setTitle(R.string.cbi_camera);
        builder.setItems(R.array.cbi_camera_cato_item, new DialogInterface.OnClickListener() {
            // 点击某个组时，记录当前组别
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentGroup = i;
                String group = getResources().getStringArray(R.array.cbi_camera_cato_item)[currentGroup];

                Toast.makeText(rootView.getContext(), "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, Common.PHOTO_FOR_OTHER_GROUP);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void bi_brand_show() {
        if(vin_edit.getText() != null) {
            brand.setVisibility(View.VISIBLE);

            // TODO: Get brand from server
            EditText brand_edit = (EditText) rootView.findViewById(R.id.bi_brand_edit);
            brand_edit.setText("大众甲壳虫 1.6 MT");
            brandOkButton.setEnabled(true);
            brandSelectButton.setEnabled(true);
        }
    }

    private void bi_content_show() {
        // 初始化所有的Spinner
        if(tableLayout.getVisibility() != View.VISIBLE) {
            SetRegLocationSpinner();
            SetCarColorSpinner();
            SetFirstLogTimeSpinner();
            SetManufactureTimeSpinner();
            SetTransferCountSpinner();
            SetLastTransferTimeSpinner();
            SetYearlyCheckAvailableDateSpinner();
            SetAvailableDateYearSpinner();
            SetBusinessInsuranceAvailableDateYearSpinner();

            tableLayout.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Common.PHOTO_FOR_OTHER_GROUP:
                if(resultCode == Activity.RESULT_OK) {
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    img.setImageBitmap(image);
                } else {
                    Toast.makeText(rootView.getContext(),
                            "error occured during opening camera", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    // 注册地
    private void SetRegLocationSpinner()
    {
        String[] provinceArray = getResources().getStringArray(R.array.ci_province);
        List<String> province = Helper.StringArray2List(provinceArray);
        SetSpinnerData(R.id.ci_reg_location_spinner, province, rootView);
    }

    // 车身颜色
    private void SetCarColorSpinner()
    {
        String[] colorArray = getResources().getStringArray(R.array.ci_car_color_arrays);
        List<String> colorList = Helper.StringArray2List(colorArray);

        SetSpinnerData(R.id.ci_car_color_spinner, colorList, rootView);
    }

    // 初次登记时间
    private void SetFirstLogTimeSpinner()
    {
        SetSpinnerData(R.id.ci_first_log_year_spinner, Helper.GetYearList(21), rootView);
        SetSpinnerData(R.id.ci_first_log_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 出厂日期
    private void SetManufactureTimeSpinner()
    {
        SetSpinnerData(R.id.ci_manufacture_year_spinner, Helper.GetYearList(21), rootView);
        SetSpinnerData(R.id.ci_manufacture_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 过户次数
    private void SetTransferCountSpinner()
    {
        SetSpinnerData(R.id.ci_transfer_count_spinner, Helper.GetMonthList(), rootView);
    }

    // 最后过户时间
    private void SetLastTransferTimeSpinner()
    {
        SetSpinnerData(R.id.ci_last_transfer_year_spinner, Helper.GetYearList(17), rootView);
        SetSpinnerData(R.id.ci_last_transfer_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 年检有效期
    private void SetYearlyCheckAvailableDateSpinner() {
        SetSpinnerData(R.id.ct_yearly_check_available_date_year_spinner, Helper.GetYearList(19), rootView);
        SetSpinnerData(R.id.ct_yearly_check_available_date_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 有效期至（交强险）
    private void SetAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_available_date_year_spinner, Helper.GetYearList(19), rootView);
        SetSpinnerData(R.id.ct_available_date_month_spinner, Helper.GetMonthList(), rootView);
        SetSpinnerData(R.id.ct_available_date_day_spinner, Helper.GetDayList(31), rootView);
    }

    // 商险有效期
    private void SetBusinessInsuranceAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_business_insurance_available_date_year_spinner, Helper.GetYearList(19), rootView);
        SetSpinnerData(R.id.ct_business_insurance_available_date_month_spinner, Helper.GetMonthList(), rootView);
        SetSpinnerData(R.id.ct_business_insurance_available_date_day_spinner, Helper.GetDayList(31), rootView);
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

    private void HandelCSITableRow(int[] tableRow) {
        for(int i = 0; i < csi_map.length; i++) {
            // 将每一行的状态进行更新
            csi_map[i][2] = tableRow[i];

            TableRow row = (TableRow) rootView.findViewById(csi_map[i][2]);
            row.setVisibility(csi_map[i][2]);
        }
    }
}

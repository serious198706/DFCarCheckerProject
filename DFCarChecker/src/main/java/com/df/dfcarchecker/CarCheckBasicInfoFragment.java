package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.df.entry.Brand;
import com.df.entry.CarSettings;
import com.df.entry.Country;
import com.df.entry.Production;
import com.df.entry.Serial;
import com.df.entry.VehicleModel;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.VehicleModelParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static com.df.service.Helper.SetSpinnerData;

public class CarCheckBasicInfoFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;
    private int currentGroup;

    // 测试用ImageView
    private ImageView img;

    // 内容TableLayout
    private TableLayout tableLayout;


    private LinearLayout brand;
    private EditText vin_edit;
    private Button brandOkButton;
    private Button brandSelectButton;
    private boolean match;
    public static int[] carSets;

    // 在型号选择对话框中的五个spinner
    private Spinner countrySpinner;
    private Spinner brandSpinner;
    private Spinner productionSpinner;
    private Spinner serialSpinner;
    private Spinner modelSpinner;

    private VehicleModel vehicleModel = null;

    private FileInputStream fis = null;

    private EditText brandEdit;
    private EditText volumeEdit;

    private String volumeString = null;
    private String brandString = null;

    private CarSettings carSettings;

    // 每一个部位的序号关联：
    // 第一个表示序号，
    // 第二个表示该序号所对应的TableRow的id，
    // 第三个表示该TableRow是否应该显示，
    // 第四个表示该序号所对应的Spinner id，
    // 第五个表示该Spinner目前的选择项

    public static int[][] csi_map = {
            {0,     R.id.csi_drive_type_spinner,             0},
            {1,     R.id.csi_gear_type_spinner,              0},
            {2,     R.id.csi_airbag_spinner,                 0},
            {3,     R.id.csi_abs_spinner,                    0},
            {4,     R.id.csi_turn_help_spinner,              0},
            {5,     R.id.csi_ele_windows_spinner,            0},
            {6,     R.id.csi_sky_light_spinner,              0},
            {7,     R.id.csi_air_conditioner_spinner,        0},
            {8,     R.id.csi_feather_seat_spinner,           0},
            {9,     R.id.csi_ele_seat_spinner,               0},
            {10,    R.id.csi_ele_reflect_mirror_spinner,     0},
            {11,    R.id.csi_parking_sensors_spinner,        0},
            {12,    R.id.csi_parking_video_spinner,          0},
            {13,    R.id.csi_ccs_spinner,                    0},
            {14,    R.id.csi_soft_close_doors_spinner,       0},
            {15,    R.id.csi_rear_ele_seats_spinner,         0},
            {16,    R.id.csi_auto_chassis_spinner,           0},
            {17,    R.id.csi_auto_parking_spinner,           0},
            {18,    R.id.csi_curtain_spinner,                0}
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_basic_info, container, false);

        img = (ImageView) rootView.findViewById(R.id.image);

        tableLayout = (TableLayout) rootView.findViewById(R.id.bi_content_table);

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
        brandEdit = (EditText) rootView.findViewById(R.id.bi_brand_edit);
        volumeEdit = (EditText) rootView.findViewById(R.id.csi_volume_edit);

        carSettings = new CarSettings();

        try {
            File f = new File("/mnt/sdcard/VehicleModel.xml");
            fis = new FileInputStream(f);

            if(fis == null) {
                Toast.makeText(rootView.getContext(), "SD卡挂载有问题", 1);
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(rootView.getContext(), "文件不存在", 1);
            e.printStackTrace();
        }

        // 隐藏一些项目：废除
        carSets = GetCarSets();
        // HandelCSITableRow(carSets);

        return rootView;
    }

    int[] GetCarSets() {
        // TODO：此序列号来自服务器
        int[] temp = {0, 2, 8};
        return temp;
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
            case R.id.bi_brand_select_button:
                selectBrand();
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
        if(vin_edit.getText().toString().equals("")) {
            Toast.makeText(rootView.getContext(), "请输入VIN码", Toast.LENGTH_SHORT).show();
        } else {
            brand.setVisibility(View.VISIBLE);

            // TODO: 从服务器获取厂牌型号
            // TODO: 如果返回多个，要提供选择功能
            getCarSettings();

            if(brandEdit.getText().toString().equals("")) {
                updateSettings();
            }

            brandOkButton.setEnabled(true);
            brandSelectButton.setEnabled(true);
        }
    }

    private void getCarSettings() {
        // TODO: 改为从网络获取，目前是来自资源文件
        carSettings.setBrand("一汽奥迪 100 1.6 MT");
        carSettings.setVolume("1.6");
        carSettings.setDriveType(arrayToList(getResources().getStringArray(R.array.csi_drive_type_item)));
        carSettings.setGearType(arrayToList(getResources().getStringArray(R.array.csi_gear_type_item)));
        carSettings.setAirbag(arrayToList(getResources().getStringArray(R.array.csi_airbag_number)));
        carSettings.setAbs(arrayToList(getResources().getStringArray(R.array.csi_abs_items)));
        carSettings.setTurnHelper(arrayToList(getResources().getStringArray(R.array.csi_turn_help_items)));
        carSettings.setEleWindows(arrayToList(getResources().getStringArray(R.array.csi_ele_windows_items)));
        carSettings.setSkyLight(arrayToList(getResources().getStringArray(R.array.csi_sky_light_items)));
        carSettings.setAirConditioner(arrayToList(getResources().getStringArray(R.array.csi_air_conditioner_items)));
        carSettings.setFeatherSeather(arrayToList(getResources().getStringArray(R.array.csi_feather_seat_items)));
        carSettings.setEleSeats(arrayToList(getResources().getStringArray(R.array.csi_ele_seats_items)));
        carSettings.setEleReflectMirror(arrayToList(getResources().getStringArray(R.array.csi_ele_reflect_mirror_items)));
        carSettings.setParkingSensors(arrayToList(getResources().getStringArray(R.array.csi_parking_sensors_items)));
        carSettings.setParkingVideo(arrayToList(getResources().getStringArray(R.array.csi_parking_video_items)));
        carSettings.setCcs(arrayToList(getResources().getStringArray(R.array.csi_ccs_items)));
        carSettings.setSoftCloseDoors(arrayToList(getResources().getStringArray(R.array.csi_soft_close_doors_items)));
        carSettings.setRearEleSeats(arrayToList(getResources().getStringArray(R.array.csi_rear_ele_seats_items)));
        carSettings.setAutoChassis(arrayToList(getResources().getStringArray(R.array.csi_auto_chassis_items)));
        carSettings.setAutoParking(arrayToList(getResources().getStringArray(R.array.csi_auto_parking_items)));
        carSettings.setCurtain(arrayToList(getResources().getStringArray(R.array.csi_curtain_items)));

        // 初始化所有的Spinner
        setRegLocationSpinner();
        setCarColorSpinner();
        setFirstLogTimeSpinner();
        setManufactureTimeSpinner();
        setTransferCountSpinner();
        setLastTransferTimeSpinner();
        setYearlyCheckAvailableDateSpinner();
        setAvailableDateYearSpinner();
        setBusinessInsuranceAvailableDateYearSpinner();
    }

    // 更新配置信息
    private void updateSettings() {
        brandEdit.setText(carSettings.getBrand());
        volumeEdit.setText(carSettings.getVolume());

        // TODO: 顺带更新其他Spinner
    }



    private void bi_content_show() {

        if(tableLayout.getVisibility() != View.VISIBLE) {
            tableLayout.setVisibility(View.VISIBLE);
            CarCheckIntegratedFragment.ShowContent();
            CarCheckStructureFragment.ShowContent();
        }
    }



    private void selectBrand() {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

        // Get the layout inflater
        LayoutInflater inflater = this.inflater;

        builder.setTitle("车型选择");
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_vehiclemodel_select, null);

        builder.setView(view);

        countrySpinner = (Spinner) view.findViewById(R.id.country_spinner);
        brandSpinner = (Spinner) view.findViewById(R.id.brand_spinner);
        productionSpinner = (Spinner) view.findViewById(R.id.production_spinner);
        serialSpinner = (Spinner) view.findViewById(R.id.serial_spinner);
        modelSpinner = (Spinner) view.findViewById(R.id.model_spinner);

        //builder.setMessage(R.string.ci_attention_content).setTitle(R.string.ci_attention);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // TODO: getCarSettings();
                // 确定
                brandString = productionSpinner.getSelectedItem().toString() + " " +
                        serialSpinner.getSelectedItem().toString() + " " +
                        modelSpinner.getSelectedItem().toString();
                volumeString = modelSpinner.getSelectedItem().toString().substring(0, 3);

                carSettings.setBrand(brandString);
                carSettings.setVolume(volumeString);

                updateSettings();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });

        if(vehicleModel == null) {
            ParseXml();
        } else {
            setCountrySpinner();
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void ParseXml() {


        VehicleModelParser parser = new VehicleModelParser();
        vehicleModel = parser.parseVehicleModelXml(fis);

        setCountrySpinner();
    }

    private void setCountrySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, vehicleModel.getCountryNames());

        countrySpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择国别时，更改品牌的Spinner Adapter
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setBrandSpinner(vehicleModel.countries.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setBrandSpinner(final Country country) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, country.getBrandNames());

        brandSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择国别时，更改品牌的Spinner Adapter
        brandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setProductionSpinner(country.brands.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setProductionSpinner(final Brand brand) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, brand.getProductionNames());

        productionSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择国别时，更改品牌的Spinner Adapter
        productionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setSerialSpinner(brand.productions.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setSerialSpinner(final Production production) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, production.getSerialNames());

        serialSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择国别时，更改品牌的Spinner Adapter
        serialSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setModelSpinner(production.serials.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setModelSpinner(final Serial serial) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, serial.getModelNames());

        modelSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
    private void setRegLocationSpinner()
    {
        String[] provinceArray = getResources().getStringArray(R.array.ci_province);
        List<String> province = Helper.StringArray2List(provinceArray);
        SetSpinnerData(R.id.ci_reg_location_spinner, province, rootView);
    }

    // 车身颜色
    private void setCarColorSpinner()
    {
        String[] colorArray = getResources().getStringArray(R.array.ci_car_color_arrays);
        List<String> colorList = Helper.StringArray2List(colorArray);

        SetSpinnerData(R.id.ci_car_color_spinner, colorList, rootView);
    }

    // 初次登记时间
    private void setFirstLogTimeSpinner()
    {
        SetSpinnerData(R.id.ci_first_log_year_spinner, Helper.GetYearList(21), rootView);
        SetSpinnerData(R.id.ci_first_log_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 出厂日期
    private void setManufactureTimeSpinner()
    {
        SetSpinnerData(R.id.ci_manufacture_year_spinner, Helper.GetYearList(21), rootView);
        SetSpinnerData(R.id.ci_manufacture_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 过户次数
    private void setTransferCountSpinner()
    {
        SetSpinnerData(R.id.ci_transfer_count_spinner, Helper.GetMonthList(), rootView);
    }

    // 最后过户时间
    private void setLastTransferTimeSpinner()
    {
        SetSpinnerData(R.id.ci_last_transfer_year_spinner, Helper.GetYearList(17), rootView);
        SetSpinnerData(R.id.ci_last_transfer_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 年检有效期
    private void setYearlyCheckAvailableDateSpinner() {
        SetSpinnerData(R.id.ct_yearly_check_available_date_year_spinner, Helper.GetYearList(19), rootView);
        SetSpinnerData(R.id.ct_yearly_check_available_date_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 有效期至（交强险）
    private void setAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_available_date_year_spinner, Helper.GetYearList(19), rootView);
        SetSpinnerData(R.id.ct_available_date_month_spinner, Helper.GetMonthList(), rootView);
        SetSpinnerData(R.id.ct_available_date_day_spinner, Helper.GetDayList(31), rootView);
    }

    // 商险有效期
    private void setBusinessInsuranceAvailableDateYearSpinner() {
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
        for(int i = 0; i < 19; i++) {
            Spinner spinner = (Spinner) rootView.findViewById(csi_map[i][1]);
        }


        for(int i = 0; i < tableRow.length; i++) {
            final int index = tableRow[i];

            csi_map[index][2] = View.VISIBLE;
            TableRow row = (TableRow) rootView.findViewById(csi_map[index][1]);
            row.setVisibility(View.VISIBLE);

            Spinner spinner = (Spinner) rootView.findViewById(csi_map[index][3]);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    csi_map[index][4] = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }


        for(int i = 0; i < csi_map.length; i++) {
            // 将每一行的状态进行更新
            TableRow row = (TableRow) rootView.findViewById(csi_map[i][1]);
            row.setVisibility(csi_map[i][2]);
        }
    }

    private List<String> arrayToList(String[] array) {
        List<String> result = new ArrayList<String>();
        for(int i = 0; i < array.length; i++) {
            result.add(array[i]);
        }

        return result;
    }
}

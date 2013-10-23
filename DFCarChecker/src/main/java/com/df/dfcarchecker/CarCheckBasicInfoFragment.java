package com.df.dfcarchecker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.df.service.Helper;
import com.df.service.VehicleModelParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.List;

import static com.df.service.Helper.SetSpinnerData;

public class CarCheckBasicInfoFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;

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

    private EditText runEdit;

    private Spinner firstLogYearSpinner;
    private Spinner firstLogMonthSpinner;
    private Spinner manufactureYearSpinner;
    private Spinner manufactureMonthSpinner;

    private Spinner ticketSpinner;
    private Spinner lastTransferCountSpinner;
    private Spinner businessInsuranceSpinner;

    private EditText carNumberEdit;

    private boolean isPorted;
    private TableRow portedProcedureRow;

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

        tableLayout = (TableLayout) rootView.findViewById(R.id.bi_content_table);

        brand = (LinearLayout) rootView.findViewById(R.id.brand_input);

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

        runEdit = (EditText) rootView.findViewById(R.id.bi_run_edit);
        runEdit.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable edt)
            {
                String temp = edt.toString();
                int posDot = temp.indexOf(".");
                if (posDot <= 0) return;
                if (temp.length() - posDot - 1 > 2)
                {
                    edt.delete(posDot + 3, posDot + 4);
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        });

        ticketSpinner = (Spinner) rootView.findViewById(R.id.ct_buy_tickets_spinner);
        ticketSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 有发票
                if(i <= 1) {
                    Helper.showView(true, rootView, R.id.ct_buy_tickets_edit);
                    Helper.showView(true, rootView, R.id.yuan);
                    Helper.showView(false, rootView, R.id.placeholder);
                } else {
                // 无发票
                    Helper.showView(false, rootView, R.id.ct_buy_tickets_edit);
                    Helper.showView(false, rootView, R.id.yuan);
                    Helper.showView(true, rootView, R.id.placeholder);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lastTransferCountSpinner = (Spinner) rootView.findViewById(R.id.ci_transfer_count_spinner);
        lastTransferCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 过户次数大于0
                if(i > 0) {
                    Helper.showView(true, rootView, R.id.ci_last_transfer_row);
                } else {
                    Helper.showView(false, rootView, R.id.ci_last_transfer_row);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        businessInsuranceSpinner = (Spinner) rootView.findViewById(R.id.ct_business_insurance_spinner);
        businessInsuranceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 保险随车出售
                if(i == 0) {
                    Helper.showView(true, rootView, R.id.ct_business_table);
                } else {
                    Helper.showView(false, rootView, R.id.ct_business_table);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        carNumberEdit = (EditText) rootView.findViewById(R.id.ci_car_number_edit);

        manufactureYearSpinner = (Spinner) rootView.findViewById(R.id.ci_manufacture_year_spinner);
        manufactureMonthSpinner = (Spinner) rootView.findViewById(R.id.ci_manufacture_month_spinner);

        portedProcedureRow = (TableRow) rootView.findViewById(R.id.ct_ported_procedure);

        carSettings = new CarSettings();

        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + "/cheyipai/VehicleModel.xml");
            fis = new FileInputStream(f);

            if(fis == null) {
                Toast.makeText(rootView.getContext(), "SD卡挂载有问题", Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(rootView.getContext(), "文件不存在", Toast.LENGTH_LONG).show();
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
            case R.id.bi_vin_button:
                bi_brand_show();
                break;
            case R.id.bi_brand_ok_button:
                bi_content_show();
                break;
            case R.id.picture_match_button:
                PictureMatch();
                break;
            case R.id.bi_brand_select_button:
                selectBrand();
                break;
        }
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
        carSettings.setDriveType(Helper.StringArray2List(getResources().getStringArray(R.array.csi_drive_type_item)));
        carSettings.setGearType(Helper.StringArray2List(getResources().getStringArray(R.array.csi_gear_type_item)));
        carSettings.setAirbag(Helper.StringArray2List(getResources().getStringArray(R.array.csi_airbag_number)));
        carSettings.setAbs(Helper.StringArray2List(getResources().getStringArray(R.array.csi_abs_items)));
        carSettings.setTurnHelper(Helper.StringArray2List(getResources().getStringArray(R.array.csi_turn_help_items)));
        carSettings.setEleWindows(Helper.StringArray2List(getResources().getStringArray(R.array.csi_ele_windows_items)));
        carSettings.setSkyLight(Helper.StringArray2List(getResources().getStringArray(R.array.csi_sky_light_items)));
        carSettings.setAirConditioner(Helper.StringArray2List(getResources().getStringArray(R.array.csi_air_conditioner_items)));
        carSettings.setFeatherSeather(Helper.StringArray2List(getResources().getStringArray(R.array.csi_feather_seat_items)));
        carSettings.setEleSeats(Helper.StringArray2List(getResources().getStringArray(R.array.csi_ele_seats_items)));
        carSettings.setEleReflectMirror(Helper.StringArray2List(getResources().getStringArray(R.array.csi_ele_reflect_mirror_items)));
        carSettings.setParkingSensors(Helper.StringArray2List(getResources().getStringArray(R.array.csi_parking_sensors_items)));
        carSettings.setParkingVideo(Helper.StringArray2List(getResources().getStringArray(R.array.csi_parking_video_items)));
        carSettings.setCcs(Helper.StringArray2List(getResources().getStringArray(R.array.csi_ccs_items)));
        carSettings.setSoftCloseDoors(Helper.StringArray2List(getResources().getStringArray(R.array.csi_soft_close_doors_items)));
        carSettings.setRearEleSeats(Helper.StringArray2List(getResources().getStringArray(R.array.csi_rear_ele_seats_items)));
        carSettings.setAutoChassis(Helper.StringArray2List(getResources().getStringArray(R.array.csi_auto_chassis_items)));
        carSettings.setAutoParking(Helper.StringArray2List(getResources().getStringArray(R.array.csi_auto_parking_items)));
        carSettings.setCurtain(Helper.StringArray2List(getResources().getStringArray(R.array.csi_curtain_items)));

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
        if(isPorted) {
            portedProcedureRow.setVisibility(View.VISIBLE);
        }
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
                // 判断是否为进口车
                if(countrySpinner.getSelectedItemPosition() > 1) {
                    isPorted = true;
                } else {
                    isPorted = false;
                }

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
        }

        setCountrySpinner(vehicleModel);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void ParseXml() {
        VehicleModelParser parser = new VehicleModelParser();
        vehicleModel = parser.parseVehicleModelXml(fis);
    }

    private void setCountrySpinner(final VehicleModel vehicleModel) {
        ArrayAdapter<String> adapter;

        if(vehicleModel == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, vehicleModel.getCountryNames());
        }

        countrySpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 选择国别时，更改品牌的Spinner Adapter
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0) {
                    setBrandSpinner(null);
                } else if(i >= 1) {
                    setBrandSpinner(vehicleModel.countries.get(i - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setBrandSpinner(final Country country) {
        ArrayAdapter<String> adapter;
        if(country == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, country.getBrandNames());
        }

        brandSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 该国家只有一个品牌（虽然不太可能哈），则默认选中吧
        if(country != null && country.getBrandNames().size() == 2) {
            brandSpinner.setSelection(1);
        }

        // 选择品牌时，更改厂商的Spinner Adapter
        brandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(country == null || i == 0) {
                    setProductionSpinner(null);
                } else if(i >= 1) {
                    setProductionSpinner(country.brands.get(i - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setProductionSpinner(final Brand brand) {
        ArrayAdapter<String> adapter;

        if(brand == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, brand.getProductionNames());
        }

        productionSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(brand != null && brand.getProductionNames().size() == 2) {
            productionSpinner.setSelection(1);
        }

        // 选择厂商时，更改车系的Spinner Adapter
        productionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(brand == null || i == 0) {
                    setSerialSpinner(null);
                } else if( i >= 1){
                    setSerialSpinner(brand.productions.get(i - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setSerialSpinner(final Production production) {
        ArrayAdapter<String> adapter;

        if(production == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, production.getSerialNames());
        }

        serialSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(production != null && production.getSerialNames().size() == 2) {
            serialSpinner.setSelection(1);
        }

        // 选择车系时，更改型号的Spinner Adapter
        serialSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(production == null || i == 0) {
                    setModelSpinner(null);
                } else if(i >= 1) {
                    setModelSpinner(production.serials.get(i - 1));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setModelSpinner(final Serial serial) {
        ArrayAdapter<String> adapter;

        if(serial == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, serial.getModelNames());
        }

        modelSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(serial != null && serial.getModelNames().size() == 2) {
            modelSpinner.setSelection(1);
        }
    }

    // 注册地
    private void setRegLocationSpinner()
    {
        String[] provinceArray = getResources().getStringArray(R.array.ci_province);
        List<String> province = Helper.StringArray2List(provinceArray);
        SetSpinnerData(R.id.ci_reg_location_spinner, province, rootView);

        String[] privinceAbbreviationArray = getResources().getStringArray(R.array.ci_province_abbreviation);
        final List<String> provinceAbbreviation = Helper.StringArray2List(privinceAbbreviationArray);

        Spinner regLocationSpinner = (Spinner) rootView.findViewById(R.id.ci_reg_location_spinner);
        regLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                carNumberEdit.setText(provinceAbbreviation.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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
        SetSpinnerData(R.id.ci_first_log_year_spinner, Helper.GetYearList(20), rootView);
        SetSpinnerData(R.id.ci_first_log_month_spinner, Helper.GetMonthList(), rootView);

        firstLogYearSpinner = (Spinner) rootView.findViewById(R.id.ci_first_log_year_spinner);
        firstLogYearSpinner.setSelection(17);
        firstLogYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 出厂日期不能晚于登记日期
                List<String> temp = Helper.GetYearList(20);
                SetSpinnerData(R.id.ci_manufacture_year_spinner, temp.subList(0, i + 1), rootView);
                manufactureYearSpinner.setSelection(i);

                // 最后过户时间不能早于登记日期
                SetSpinnerData(R.id.ci_last_transfer_year_spinner, temp.subList(i, temp.size()), rootView);

                // 年检有效期、交强险有效期不能早于登记日期
                int from = Integer.parseInt(temp.get(i));
                int to = Calendar.getInstance().get(Calendar.YEAR) + 2;

                SetSpinnerData(R.id.ct_yearly_check_available_date_year_spinner, Helper.GetNumbersList(from, to), rootView);
                SetSpinnerData(R.id.ct_available_date_year_spinner, Helper.GetNumbersList(from, to), rootView);
                SetSpinnerData(R.id.ct_business_insurance_available_date_year_spinner, Helper.GetNumbersList(from, to), rootView);

                // 商险有效期不能早于登记日期
                SetSpinnerData(R.id.ct_business_insurance_available_date_year_spinner, temp.subList(i, temp.size()), rootView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        firstLogMonthSpinner = (Spinner) rootView.findViewById(R.id.ci_first_log_month_spinner);
        firstLogMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<String> temp = Helper.GetMonthList();
                SetSpinnerData(R.id.ci_manufacture_month_spinner, temp.subList(0, i + 1), rootView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // 出厂日期
    private void setManufactureTimeSpinner()
    {
        SetSpinnerData(R.id.ci_manufacture_year_spinner, Helper.GetYearList(20), rootView);
        SetSpinnerData(R.id.ci_manufacture_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 过户次数
    private void setTransferCountSpinner()
    {
        SetSpinnerData(R.id.ci_transfer_count_spinner, Helper.GetNumbersList(0, 15), rootView);
    }

    // 最后过户时间
    private void setLastTransferTimeSpinner()
    {
        SetSpinnerData(R.id.ci_last_transfer_year_spinner, Helper.GetYearList(17), rootView);
        SetSpinnerData(R.id.ci_last_transfer_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 年检有效期
    private void setYearlyCheckAvailableDateSpinner() {
        SetSpinnerData(R.id.ct_yearly_check_available_date_year_spinner, Helper.GetYearList(2), rootView);
        SetSpinnerData(R.id.ct_yearly_check_available_date_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 有效期至（交强险）
    private void setAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_available_date_year_spinner, Helper.GetYearList(2), rootView);
        SetSpinnerData(R.id.ct_available_date_month_spinner, Helper.GetMonthList(), rootView);
    }

    // 商险有效期
    private void setBusinessInsuranceAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_business_insurance_available_date_year_spinner, Helper.GetYearList(19), rootView);
        SetSpinnerData(R.id.ct_business_insurance_available_date_month_spinner, Helper.GetMonthList(), rootView);
    }

    public void PictureMatch()
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
}

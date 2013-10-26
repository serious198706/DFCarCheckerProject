package com.df.dfcarchecker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.df.entry.Brand;
import com.df.entry.CarSettings;
import com.df.entry.Country;
import com.df.entry.Model;
import com.df.entry.Manufacturer;
import com.df.entry.Series;
import com.df.entry.VehicleModel;
import com.df.service.Helper;
import com.df.service.SoapService;
import com.df.service.VehicleModelParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
    private Spinner manufacturerSpinner;
    private Spinner seriesSpinner;
    private Spinner modelSpinner;
    private int lastCountryIndex = 0;
    private int lastBrandIndex = 0;
    private int lastManufacturerIndex = 0;
    private int lastSeriesIndex = 0;
    private int lastModelIndex = 0;

    private VehicleModel vehicleModel = null;

    private FileInputStream fis = null;

    private EditText brandEdit;
    private EditText volumeEdit;

    private String volumeString = null;
    private String brandString = null;

    private CarSettings mCarSettings;

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

    private SoapService soapService;

    private GetCarSettingsTask mGetCarSettingsTask = null;

    // 每一个部位的序号关联：
    // 第一个表示序号，
    // 第二个表示该序号所对应的TableRow的id，
    // 第三个表示该TableRow是否应该显示，
    // 第四个表示该序号所对应的Spinner id，
    // 第五个表示该Spinner目前的选择项

    private String result;
    private ProgressDialog progressDialog;

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

        mCarSettings = new CarSettings();


        List<String> dummy = new ArrayList<String>();
        // 1
        dummy.add("LE4FG65Z487015744");
        // 2
        dummy.add("LJDBAA33630037414");
        // 0
        dummy.add("LVVDB12A86D193156");
        SetSpinnerData(R.id.bi_vin_spinner, dummy, rootView);

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

        if(vehicleModel == null) {
            ParseXml();
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bi_vin_button:
                getCarSettings();
                break;
            case R.id.bi_brand_ok_button:
                showContent();
                break;
            case R.id.picture_match_button:
                PictureMatch();
                break;
            case R.id.bi_brand_select_button:
                selectBrand();
                break;
        }
    }

    // 检查VIN并获取车辆配置
    private void getCarSettings() {
        String vinString = vin_edit.getText().toString();
        Spinner dummySpinner = (Spinner) rootView.findViewById(R.id.bi_vin_spinner);
        vinString = dummySpinner.getSelectedItem().toString();
        vin_edit.setText(vinString);

        // 是否为空
        if(vinString.equals("")) {
            Toast.makeText(rootView.getContext(), "请输入VIN码", Toast.LENGTH_SHORT).show();
            vin_edit.requestFocus();
            return;
        }

        // 检查VIN码
        if(!Helper.isVin(vinString)) {
            Toast.makeText(rootView.getContext(), "VIN码输入有误，请检查", Toast.LENGTH_SHORT).show();
            vin_edit.requestFocus();
            return;
        }

        brandEdit.setText("");
        brandOkButton.setEnabled(false);
        brandSelectButton.setEnabled(false);

        // 传参数为空，表示提交的为VIN
        getCarSettingsFromServer("");

        brand.setVisibility(View.VISIBLE);
    }

    // 从服务器获取车辆配置
    private void getCarSettingsFromServer(String seriesId) {
        mGetCarSettingsTask = new GetCarSettingsTask(rootView.getContext());
        mGetCarSettingsTask.execute(seriesId);
    }

    // 更新配置信息
    private void updateUIAccordingToCarSettings() {
        // 更新品牌输入框
        brandEdit.setText(mCarSettings.getBrand());
        volumeEdit.setText(mCarSettings.getDisplacement());

        // TODO: 顺带更新其他Spinner
        if(isPorted) {
            portedProcedureRow.setVisibility(View.VISIBLE);
        }

        Spinner driveTypeSpinner = (Spinner) rootView.findViewById(R.id.csi_drive_type_spinner);
        driveTypeSpinner.setSelection(Integer.parseInt(mCarSettings.getDriveType()));

        Spinner transmissionSpinner = (Spinner) rootView.findViewById(R.id.csi_transmission_spinner);
        transmissionSpinner.setSelection(Integer.parseInt(mCarSettings.getTransmission()));
    }

    // 显示车辆的配置信息
    private void showContent() {
        if(tableLayout.getVisibility() != View.VISIBLE) {
            tableLayout.setVisibility(View.VISIBLE);
            CarCheckIntegratedFragment.ShowContent();
            CarCheckStructureFragment.ShowContent();
        }
    }

    // 选择车辆型号
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
        manufacturerSpinner = (Spinner) view.findViewById(R.id.production_spinner);
        seriesSpinner = (Spinner) view.findViewById(R.id.serial_spinner);
        modelSpinner = (Spinner) view.findViewById(R.id.model_spinner);

        //builder.setMessage(R.string.ci_attention_content).setTitle(R.string.ci_attention);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 确定
                // 判断是否为进口车
                if(countrySpinner.getSelectedItemPosition() > 1) {
                    isPorted = true;
                } else {
                    isPorted = false;
                }

                // TODO: 提交方式不同，只提交车系id（seriesId)
                // TODO: getCarSettingsFromServer();
                // 获取当前选择的seriesID
                lastCountryIndex = countrySpinner.getSelectedItemPosition() - 1;
                lastBrandIndex = brandSpinner.getSelectedItemPosition() - 1;
                lastManufacturerIndex = manufacturerSpinner.getSelectedItemPosition() - 1;
                lastSeriesIndex = seriesSpinner.getSelectedItemPosition() - 1;
                lastModelIndex = modelSpinner.getSelectedItemPosition() - 1;

                Country country = vehicleModel.countries.get(lastCountryIndex);
                Brand brand = country.brands.get(lastBrandIndex);
                Manufacturer manufacturer = brand.manufacturers.get(lastManufacturerIndex);
                Series series = manufacturer.serieses.get(lastSeriesIndex);
                Model model = series.models.get(lastModelIndex);

                getCarSettingsFromServer(series.id);

                brandString = manufacturerSpinner.getSelectedItem().toString() + " " +
                        seriesSpinner.getSelectedItem().toString() + " " +
                        modelSpinner.getSelectedItem().toString();

                volumeString = modelSpinner.getSelectedItem().toString();
                if(volumeString.length() > 3) {
                    volumeString = volumeString.substring(0, 3);
                }

                mCarSettings.setBrand(brandString);
                mCarSettings.setDisplacement(volumeString);

                setCarSettingsSpinners(model.getName());

                updateUIAccordingToCarSettings();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });

        setCountrySpinner(vehicleModel);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 解析车型XML
    public void ParseXml() {
        VehicleModelParser parser = new VehicleModelParser();
        vehicleModel = parser.parseVehicleModelXml(fis);
    }

    // 设置国家Spinner
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

    // 设置品牌Spinner
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
                    setManufacturerSpinner(null);
                } else if(i >= 1) {
                    setManufacturerSpinner(country.brands.get(i - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // 设置厂商Spinner
    private void setManufacturerSpinner(final Brand brand) {
        ArrayAdapter<String> adapter;

        if(brand == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, brand.getProductionNames());
        }

        manufacturerSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(brand != null && brand.getProductionNames().size() == 2) {
            manufacturerSpinner.setSelection(1);
        }

        // 选择厂商时，更改车系的Spinner Adapter
        manufacturerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (brand == null || i == 0) {
                    setSeriesSpinner(null);
                } else if (i >= 1) {
                    setSeriesSpinner(brand.manufacturers.get(i - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // 设置车系Spinner
    private void setSeriesSpinner(final Manufacturer manufacturer) {
        ArrayAdapter<String> adapter;

        if(manufacturer == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, manufacturer.getSerialNames());
        }

        seriesSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(manufacturer != null && manufacturer.getSerialNames().size() == 2) {
            seriesSpinner.setSelection(1);
        }

        // 选择车系时，更改型号的Spinner Adapter
        seriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (manufacturer == null || i == 0) {
                    setModelSpinner(null);
                } else if (i >= 1) {
                    setModelSpinner(manufacturer.serieses.get(i - 1));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // 设置车型Spinner
    private void setModelSpinner(final Series series) {
        ArrayAdapter<String> adapter;

        if(series == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, series.getModelNames());
        }

        modelSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(series != null && series.getModelNames().size() == 2) {
            modelSpinner.setSelection(1);
        }
    }

    // 设置车辆配置Spinner（所有的）
    private void setCarSettingsSpinners(String modelString) {
        // 将排量框设置文字
        if(modelString.length() >= 3)
        {
            mCarSettings.setDisplacement(modelString.substring(0, 3));
        }

        // 设置驱动方式Spinner
        if(modelString.contains("四驱")) {
            mCarSettings.setDriveType("四驱");
        } else {
            mCarSettings.setDriveType("两驱");
        }

        // 设置变速器形式Spinner
        if(modelString.contains("A/MT")) {
            mCarSettings.setTransmission("A/MT");
        } else if(modelString.contains("MT")) {
            mCarSettings.setTransmission("MT");
        } else if(modelString.contains("CVT") || modelString.contains("DSG")) {
            mCarSettings.setTransmission("CVT");
        } else {
            mCarSettings.setTransmission("AT");
        }
    }

    // 注册地
    private void setRegLocationSpinner()
    {
        String[] provinceArray = getResources().getStringArray(R.array.ci_province);
        List<String> province = Helper.StringArray2List(provinceArray);
        SetSpinnerData(R.id.ci_reg_location_spinner, province, rootView);

        String[] provinceAbbreviationArray = getResources().getStringArray(R.array.ci_province_abbreviation);
        final List<String> provinceAbbreviation = Helper.StringArray2List(provinceAbbreviationArray);

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


    private class GetCarSettingsTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        String seriesId;
        Model model;
        String modelName = "";
        List<String> modelNames;
        JSONObject jsonObject;
        List<JSONObject> jsonObjects;

        private GetCarSettingsTask(Context context) {
            this.context = context;
            this.seriesId = null;
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(rootView.getContext(), "提醒",
                    "正在获取车辆信息，请稍候。。", false, false);
            model = null;
            modelName = "";
            modelNames = null;
            jsonObject = null;
            jsonObjects = null;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
            boolean success = false;

            seriesId = params[0];

            // 传输seriesId
            if(!seriesId.equals("")) {
                try {
                    JSONObject jsonObject = new JSONObject();

                    // SeriesId + userID + key
                    jsonObject.put("SeriesId", seriesId);
                    jsonObject.put("UserId", LoginActivity.userInfo.getId());
                    jsonObject.put("Key", LoginActivity.userInfo.getKey());

                    soapService = new SoapService();

                    // 设置soap的配置
                    soapService.setUtils("http://192.168.8.33:801/ReportService.svc",
                            "http://cheyiju/IReportService/GetOptionsBySeriesId",
                            "GetOptionsBySeriesId");

                    success = soapService.communicateWithServer(context, jsonObject.toString());

                    // TODO: 加入用户是否登录的状态改变

                    // 传输失败，获取错误信息并显示
                    if(!success) {
                        Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());
                    } else {
                        result = soapService.getResultMessage();
                    }
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                    return false;
                }
            }
            // 传输VIN
            else {
                try {
                    JSONObject jsonObject = new JSONObject();

                    // vin + userID + key
                    jsonObject.put("Vin", vin_edit.getText().toString());
                    jsonObject.put("UserId", LoginActivity.userInfo.getId());
                    jsonObject.put("Key", LoginActivity.userInfo.getKey());

                    soapService = new SoapService();

                    // 设置soap的配置
                    soapService.setUtils("http://192.168.8.33:801/ReportService.svc",
                            "http://cheyiju/IReportService/GetCarConfigInfoByVin",
                            "GetCarConfigInfoByVin");

                    success = soapService.communicateWithServer(context, jsonObject.toString());

                    // TODO: 加入用户是否登录的状态改变

                    // 传输失败，获取错误信息并显示
                    if(!success) {
                        Log.d("DFCarChecker", "获取车辆配置信息失败：" + soapService.getErrorMessage());
                    } else {
                        result = soapService.getResultMessage();
                    }
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                    return false;
                }
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetCarSettingsTask = null;

            progressDialog.dismiss();

            if (success) {
                // TODO：可以得到车辆配置了，解析字符串并且进行惨无人道的赋值吧=。=
                // outputStringJson : [{"countryId":1,"brandId":5,"manufacturerId":2,"seriesId":24,"modelId":29,"config":"powerWindows,airConditioning,sunroof,leatherSeats,powerSeats,powerMirror,reversingRadar"}]
                // 设置车辆配置
                // mCarSettings.setCarSettings(result);

                Country carCountry = null;
                Brand brand = null;
                Manufacturer manufacturer = null;
                Series series = null;
                String config = null;

                try {
                    // 开始位为[，表示传输的是全部信息
                    if(result.startsWith("[")) {
                        JSONArray jsonArray = new JSONArray(result);

                        // 用来存储车辆配置信息的jsonobject list
                        jsonObjects = new ArrayList<JSONObject>();

                        // 用来存储车辆型号的string list
                        modelNames = new ArrayList<String>();

                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            jsonObjects.add(jsonObject);
                            carCountry = vehicleModel.getCountryById(jsonObject.getString("countryId"));
                            brand = carCountry.getBrandById(jsonObject.getString("brandId"));
                            manufacturer = brand.getProductionById(jsonObject.getString("manufacturerId"));
                            series = manufacturer.getSerialById(jsonObject.getString("seriesId"));
                            model = series.getModelById(jsonObject.getString("modelId"));
                            config = jsonObject.getString("config");

                            modelNames.add(manufacturer.name + " " + series.name + " " + model.name);
                        }

                        // 弹出一个对话框，供用户进行选择
                        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

                        builder.setTitle(R.string.bi_select_model);

                        String[] tempArray = new String[modelNames.size()];

                        for(int i = 0; i < modelNames.size(); i++) {
                            tempArray[i] = modelNames.get(i);
                        }

                        builder.setItems(tempArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                modelName = modelNames.get(i);
                                jsonObject = jsonObjects.get(i);

                                // 将品牌框设置文字
                                mCarSettings.setBrand(modelName);

                                setCarSettingsSpinners(model.getName());

                                // 更新UI
                                updateUIAccordingToCarSettings();

                                brandOkButton.setEnabled(true);
                                brandSelectButton.setEnabled(true);
                            }
                        });
                        // Inflate and set the layout for the dialog
                        // Pass null as the parent view because its going in the dialog layout
                        //builder.setView(inflater.inflate(R.layout.bi_camera_cato_dialog, null));

                        //builder.setMessage(R.string.ci_attention_content).setTitle(R.string.ci_attention);
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 取消
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    // 开始位为{，表示传输的是配置信息
                    else if(result.startsWith("{")) {
                        JSONObject jsonObject = new JSONObject(result);
                        config = jsonObject.getString("config");

                        // 因为是从车型选择中选择的，所以品牌输入框不需要设置文字
                    }
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                }
            } else {
                Log.d("DFCarChecker", "连接错误！");
            }
        }

        @Override
        protected void onCancelled() {
            mGetCarSettingsTask = null;
        }
    }
}

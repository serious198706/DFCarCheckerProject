package com.df.dfcarchecker.CarCheck;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
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

import com.df.dfcarchecker.LoginActivity;
import com.df.dfcarchecker.MainActivity;
import com.df.dfcarchecker.R;
import com.df.entry.Brand;
import com.df.entry.CarSettings;
import com.df.entry.Country;
import com.df.entry.Model;
import com.df.entry.Manufacturer;
import com.df.entry.PhotoEntity;
import com.df.entry.Series;
import com.df.entry.VehicleModel;
import com.df.service.Common;
import com.df.service.EncryptDecryptFile;
import com.df.service.Helper;
import com.df.service.SoapService;
import com.df.service.VehicleModelParser;
import com.df.service.XmlHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import static com.df.service.Helper.SetSpinnerData;
import static com.df.service.Helper.enableView;
import static com.df.service.Helper.getDateString;
import static com.df.service.Helper.getEditText;
import static com.df.service.Helper.getSpinnerSelectedText;
import static com.df.service.Helper.setEditText;
import static com.df.service.Helper.setEditWeight;
import static com.df.service.Helper.setSpinnerSelectionWithString;
import static com.df.service.Helper.showView;

public class CarCheckBasicInfoFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;

    // 内容TableLayout
    private TableLayout tableLayout;

    private LinearLayout contentLayout;
    private EditText vin_edit;
    private Button brandOkButton;
    private Button brandSelectButton;
    private boolean match;

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
    private EditText displacementEdit;

    private String volumeString = null;
    private String brandString = null;

    public static CarSettings mCarSettings;

    private EditText runEdit;

    private Spinner firstLogYearSpinner;
    private Spinner manufactureYearSpinner;

    private Spinner ticketSpinner;
    private Spinner lastTransferCountSpinner;
    private Spinner businessInsuranceSpinner;

    private EditText carNumberEdit;

    private Spinner transmissionSpinner;
    private EditText transmissionEdit;

    private boolean isPorted;
    private TableRow portedProcedureRow;

    private SoapService soapService;

    private GetCarSettingsTask mGetCarSettingsTask = null;

    public static String uniqueId;

    private String result;
    private ProgressDialog progressDialog;

    public static List<PhotoEntity> sketchPhotoEntities;

    private ProgressDialog mProgressDialog;

    private EditText licencePhotoMatchEdit;

    // 用于修改
    private String jsonData = "";
    private boolean modifyMode = false;

    OnHeadlineSelectedListener mCallback;
    private JSONObject procedures;
    private JSONObject options;

    public CarCheckBasicInfoFragment(String jsonData) {
        this.jsonData = jsonData;
    }

    // Container Activity must implement this interface
    public interface OnHeadlineSelectedListener {
        public void onUpdateIntegratedUi();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Random r=new Random();
        int uniqueNumber =(r.nextInt(999) + 100);

        // 根据产生uniqueId（对应一次检测）
        uniqueId = Integer.toString(uniqueNumber);

        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_basic_info, container, false);

        // <editor-fold defaultstate="collapsed" desc="各种View的初始化">
        tableLayout = (TableLayout) rootView.findViewById(R.id.bi_content_table);

        contentLayout = (LinearLayout) rootView.findViewById(R.id.brand_input);

        Button vinButton = (Button) rootView.findViewById(R.id.bi_vin_button);
        vinButton.setOnClickListener(this);

        brandOkButton = (Button) rootView.findViewById(R.id.bi_brand_ok_button);
        brandOkButton.setEnabled(false);
        brandOkButton.setOnClickListener(this);

        brandSelectButton = (Button) rootView.findViewById(R.id.bi_brand_select_button);
        brandSelectButton.setEnabled(false);
        brandSelectButton.setOnClickListener(this);

        sketchPhotoEntities = new ArrayList<PhotoEntity>();

        // 实车与行驶本照片
        Button matchButton = (Button) rootView.findViewById(R.id.ct_licencePhotoMatch_button);
        matchButton.setOnClickListener(this);

        InputFilter alphaNumericFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence arg0, int arg1, int arg2, Spanned arg3, int arg4, int arg5)
            {
                for (int k = arg1; k < arg2; k++) {
                    if (!Character.isLetterOrDigit(arg0.charAt(k))) {
                        return "";
                    }
                }
                return null;
            }
        };
        vin_edit = (EditText) rootView.findViewById(R.id.bi_vin_edit);
        vin_edit.setFilters(new InputFilter[]{ alphaNumericFilter, new InputFilter.AllCaps()});

        brandEdit = (EditText) rootView.findViewById(R.id.bi_brand_edit);
        displacementEdit = (EditText) rootView.findViewById(R.id.csi_displacement_edit);

        transmissionEdit = (EditText) rootView.findViewById(R.id.csi_transmission_edit);
//
//        transmissionSpinner = (Spinner)rootView.findViewById(R.id.csi_transmission_spinner);
//        transmissionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                transmissionEdit.setText(adapterView.getSelectedItem().toString());
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        runEdit = (EditText) rootView.findViewById(R.id.bi_mileage_edit);

        ScrollView view = (ScrollView)rootView.findViewById(R.id.root);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        // 只允许小数点后两位
        runEdit.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable edt) {
                String temp = edt.toString();

                if(temp.contains(".")) {
                    int posDot = temp.indexOf(".");
                    if (posDot <= 0) return;
                    if (temp.length() - posDot - 1 > 2) {
                        edt.delete(posDot + 3, posDot + 4);
                    }
                } else {
                    if(temp.length() > 2) {
                        edt.clear();
                        edt.append(temp.substring(0, 2));
                    }
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        });

        licencePhotoMatchEdit = (EditText) rootView.findViewById(R.id
                .ct_licencePhotoMatch_edit);
        licencePhotoMatchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                licencePhotoMatchEdit.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                licencePhotoMatchEdit.setError(null);
            }
        });

        // 购车发票
        ticketSpinner = (Spinner) rootView.findViewById(R.id.ct_invoice_spinner);
        ticketSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 有发票
                if(i <= 1) {
                    Helper.showView(true, rootView, R.id.ct_invoice_edit);
                    Helper.showView(true, rootView, R.id.yuan);
                    Helper.showView(false, rootView, R.id.placeholder);
                } else {
                    // 无发票
                    Helper.showView(false, rootView, R.id.ct_invoice_edit);
                    Helper.showView(false, rootView, R.id.yuan);
                    Helper.showView(true, rootView, R.id.placeholder);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 最后过户日期
        lastTransferCountSpinner = (Spinner) rootView.findViewById(R.id.ci_transferCount_spinner);
        lastTransferCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 过户次数大于0
                if (i > 0) {
                    Helper.showView(true, rootView, R.id.ci_last_transfer_row);
                } else {
                    Helper.showView(false, rootView, R.id.ci_last_transfer_row);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 商业保险
        businessInsuranceSpinner = (Spinner) rootView.findViewById(R.id.ct_insurance_spinner);
        businessInsuranceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 保险随车出售
                if (i == 0) {
                    Helper.showView(true, rootView, R.id.ct_insurance_table);
                } else {
                    Helper.showView(false, rootView, R.id.ct_insurance_table);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 牌照号码
        carNumberEdit = (EditText) rootView.findViewById(R.id.ci_plateNumber_edit);

        // 出厂日期
        manufactureYearSpinner = (Spinner) rootView.findViewById(R.id.ci_builtYear_spinner);

        // 进口车手续
        portedProcedureRow = (TableRow) rootView.findViewById(R.id.ct_ported_procedure);

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
        // </editor-fold>

        mCarSettings = new CarSettings();

        List<String> dummy = new ArrayList<String>();
        // 1
        dummy.add("LE4FG65Z487015744");
        // 2
        dummy.add("LJDBAA33630037414");
        // 0
        dummy.add("LVVDB12A86D193156");
        // 两厢
        dummy.add("LVFAC2AD87K000028");
        // 16位
        dummy.add("LAD23SVAXTM08271");
        // 10位
        dummy.add("LENYVI234N");

        SetSpinnerData(R.id.bi_vin_spinner, dummy, rootView);

        // 开启一个新的线程解析xml文件
        if(vehicleModel == null) {
            mProgressDialog = ProgressDialog.show(rootView.getContext(), null,
                    "请稍候..", false, false);

            Thread thread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {
                        //Your code goes here
                        ParseXml();

                        // 如果jsonData不为空，表示为修改模式
                        if(!jsonData.equals("")) {
                            modifyMode = true;
                            letsEnterModifyMode();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bi_vin_button:
                checkVinAndGetCarSettings();
                break;
            case R.id.bi_brand_ok_button:
                showContent();
                break;
            case R.id.ct_licencePhotoMatch_button:
                PictureMatch();
                break;
            case R.id.bi_brand_select_button:
                selectCarBrand();
                break;
        }
    }

    // 检查VIN并获取车辆配置
    private void checkVinAndGetCarSettings() {
        // 如果是修改模式，则不允许修改vin
        if(!jsonData.equals("")) {
            AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                    .setTitle(R.string.alert_title)
                    .setMessage("不允许修改VIN码！")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                   }).create();

            dialog.show();
            return;
        }

        final String vinString = vin_edit.getText().toString();
        //Spinner dummySpinner = (Spinner) rootView.findViewById(R.id.bi_vin_spinner);
        //final String vinString = dummySpinner.getSelectedItem().toString();

        // 是否为空
        if(vinString.equals("")) {
            Toast.makeText(rootView.getContext(), "请输入VIN码", Toast.LENGTH_SHORT).show();
            vin_edit.requestFocus();
            return;
        }

        // 检查VIN码
        if(!Helper.isVin(vinString)) {
            AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                    .setTitle(R.string.alert_title)
                    .setMessage("您输入的VIN码为: " + vinString + "\n" + "系统检测到VIN码可能有误，是否确认继续提交？\n" )
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            brandEdit.setText("");
                            brandOkButton.setEnabled(false);
                            brandSelectButton.setEnabled(false);

                            // 传参数为空，表示提交的为VIN
                            getCarSettingsFromServer("");
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            vin_edit.requestFocus();
                        }
                    }).create();

            dialog.show();
            return;
        }

        brandEdit.setText("");
        brandOkButton.setEnabled(false);
        brandSelectButton.setEnabled(false);

        // 传参数为空，表示提交的为VIN
        getCarSettingsFromServer("");
    }

    // 从服务器获取车辆配置
    private void getCarSettingsFromServer(String seriesId) {
        mGetCarSettingsTask = new GetCarSettingsTask(rootView.getContext());
        mGetCarSettingsTask.execute(seriesId);
    }

    // 更新UI
    private void updateUi() {
        // 设置厂牌型号的EditText
        setEditText(rootView, R.id.bi_brand_edit, mCarSettings.getBrandString());

        brandOkButton.setEnabled(true);
        brandSelectButton.setEnabled(true);

        // 设置排量EditText
        setEditText(rootView, R.id.csi_displacement_edit, mCarSettings.getDisplacement());

        // 根据是否进口更改手续选项
        if(isPorted) {
            portedProcedureRow.setVisibility(View.VISIBLE);
        }

        // 改动配置信息中的Spinner
        String carConfigs = mCarSettings.getCarConfigs();
        String configArray[] = carConfigs.split(",");

        for(int i = 0; i < configArray.length; i++) {
            int selection = Integer.parseInt(configArray[i]);
            setSpinnerSelection(Common.carSettingsSpinnerMap[i][0], selection);
        }

        transmissionEdit.setText(mCarSettings.getTransmissionText());

        // 发动“车体结构检查”里显示的图片
        if(!mCarSettings.getFigure().equals(""))
            CarCheckFrameFragment.setFigureImage(Integer.parseInt(mCarSettings.getFigure()));

        // 改动“综合检查”里的档位类型选项
        CarCheckIntegratedFragment.setGearType(mCarSettings.getTransmissionText());

        mCallback.onUpdateIntegratedUi();
    }

    // 设置配置信息中的Spinner，并与综合检查中的Spinner产生联动
    private void setSpinnerSelection(final int spinnerId, int selection) {
        final Spinner spinner = (Spinner) rootView.findViewById(spinnerId);
        spinner.setSelection(selection);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CarCheckIntegratedFragment.updateAssociatedSpinners(spinnerId, adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    // 显示车辆的配置信息
    private void showContent() {
        if(tableLayout.getVisibility() != View.VISIBLE) {
            tableLayout.setVisibility(View.VISIBLE);
            CarCheckIntegratedFragment.showContent();
            CarCheckFrameFragment.showContent();
        }
    }

    // 选择厂牌型号
    private void selectCarBrand() {
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

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 确定
                // 判断是否为进口车
                if(countrySpinner.getSelectedItemPosition() > 1) {
                    isPorted = true;
                } else {
                    isPorted = false;
                }

                // 记录用户选择的位置
                lastCountryIndex = countrySpinner.getSelectedItemPosition();
                lastBrandIndex = brandSpinner.getSelectedItemPosition();
                lastManufacturerIndex = manufacturerSpinner.getSelectedItemPosition();
                lastSeriesIndex = seriesSpinner.getSelectedItemPosition();
                lastModelIndex = modelSpinner.getSelectedItemPosition();

                // 如果用户点击确定，则必须要求所有的Spinner为选中状态
                if(lastCountryIndex == 0 ||
                        lastBrandIndex == 0 ||
                        lastManufacturerIndex == 0 ||
                        lastSeriesIndex == 0 ||
                        lastModelIndex == 0) {
                    Toast.makeText(rootView.getContext(), "请选择所有项目", Toast.LENGTH_SHORT).show();

                    return;
                }

                Country country = vehicleModel.countries.get(lastCountryIndex - 1);
                Brand brand = country.brands.get(lastBrandIndex - 1);
                Manufacturer manufacturer = brand.manufacturers.get(lastManufacturerIndex - 1);
                Series series = manufacturer.serieses.get(lastSeriesIndex - 1);
                Model model = series.models.get(lastModelIndex - 1);

                getCarSettingsFromServer(series.id + "," + model.id);

                brandString = manufacturerSpinner.getSelectedItem().toString() + " " +
                        seriesSpinner.getSelectedItem().toString() + " " +
                        modelSpinner.getSelectedItem().toString();

                volumeString = modelSpinner.getSelectedItem().toString();
                if(volumeString.length() > 3) {
                    volumeString = volumeString.substring(0, 3);
                }

                mCarSettings.setBrandString(brandString);
                mCarSettings.setDisplacement(volumeString);
                mCarSettings.setCountry(country);
                mCarSettings.setBrand(brand);
                mCarSettings.setManufacturer(manufacturer);
                mCarSettings.setSeries(series);
                mCarSettings.setModel(model);

                setCarSettingsSpinners(model.getName());

                //updateUi();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
                lastCountryIndex = countrySpinner.getSelectedItemPosition();
                lastBrandIndex = brandSpinner.getSelectedItemPosition();
                lastManufacturerIndex = manufacturerSpinner.getSelectedItemPosition();
                lastSeriesIndex = seriesSpinner.getSelectedItemPosition();
                lastModelIndex = modelSpinner.getSelectedItemPosition();
            }
        });

        setCountrySpinner(vehicleModel);

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    // 解析车型XML
    public void ParseXml() {
        try {
            String path = Environment.getExternalStorageDirectory().getPath() + "/.cheyipai/";
            String zippedFile = path + "df001";

            try {
                XmlHandler.unzip(zippedFile, path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            File f = new File(path + "vm");

            //EncryptDecryptFile.decryptFile(f.getPath(), "jwm65700DFCar");

            fis = new FileInputStream(f);

            if(fis == null) {
                Toast.makeText(rootView.getContext(), "SD卡挂载有问题", Toast.LENGTH_LONG).show();
            } else {
                VehicleModelParser parser = new VehicleModelParser();
                vehicleModel = parser.parseVehicleModelXml(fis);
                f.delete();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(rootView.getContext(), "文件不存在", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        //} catch (GeneralSecurityException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mProgressDialog.dismiss();
    }

    // <editor-fold defaultstate="collapsed" desc="生成JsonString">
    // 生成配置信息jsonString
    public JSONObject generateOptionsJsonObject() {
        JSONObject options = new JSONObject();

        try {
            options.put("vin", vin_edit.getText().toString());
            options.put("country", mCarSettings.getCountry().name);
            options.put("countryId", Integer.parseInt(mCarSettings.getCountry().id));
            options.put("brand", mCarSettings.getBrand().name);
            options.put("brandId", Integer.parseInt(mCarSettings.getBrand().id));
            options.put("manufacturer", mCarSettings.getManufacturer().name);
            options.put("manufacturerId", Integer.parseInt(mCarSettings.getManufacturer().id));
            options.put("series", mCarSettings.getSeries().name);
            options.put("seriesId", Integer.parseInt(mCarSettings.getSeries().id));
            options.put("model", mCarSettings.getModel().name);
            options.put("modelId", Integer.parseInt(mCarSettings.getModel().id));
            options.put("displacement", displacementEdit.getText().toString());

            String[] categoryArray = rootView.getResources().getStringArray(R.array.csi_category_item);
            options.put("category", mCarSettings.getCategory());
            options.put("driveType", getSpinnerSelectedText(rootView, R.id.csi_driveType_spinner));
            options.put("transmission", getSpinnerSelectedText(rootView, R.id.csi_transmission_spinner));
            options.put("airBags", getSpinnerSelectedText(rootView, R.id.csi_airbag_spinner));

            if(!getSpinnerSelectedText(rootView, R.id.csi_abs_spinner).equals("无"))
                options.put("abs", getSpinnerSelectedText(rootView, R.id.csi_abs_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_powerSteering_spinner).equals("无"))
                options.put("powerSteering", getSpinnerSelectedText(rootView, R.id.csi_powerSteering_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_powerWindows_spinner).equals("无"))
                options.put("powerWindows", getSpinnerSelectedText(rootView, R.id.csi_powerWindows_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_sunroof_spinner).equals("无"))
                options.put("sunroof", getSpinnerSelectedText(rootView, R.id.csi_sunroof_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_airConditioning_spinner).equals("无"))
                options.put("airConditioning", getSpinnerSelectedText(rootView, R.id.csi_airConditioning_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_leatherSeats_spinner).equals("无"))
                options.put("leatherSeats", getSpinnerSelectedText(rootView, R.id.csi_leatherSeats_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_powerSeats_spinner).equals("无"))
                options.put("powerSeats", getSpinnerSelectedText(rootView, R.id.csi_powerSeats_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_powerMirror_spinner).equals("无"))
                options.put("powerMirror", getSpinnerSelectedText(rootView, R.id.csi_powerMirror_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_reversingRadar_spinner).equals("无"))
                options.put("reversingRadar", getSpinnerSelectedText(rootView, R.id.csi_reversingRadar_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_reversingCamera_spinner).equals("无"))
                options.put("reversingCamera", getSpinnerSelectedText(rootView, R.id.csi_reversingCamera_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_ccs_spinner).equals("无"))
                options.put("ccs", getSpinnerSelectedText(rootView, R.id.csi_ccs_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_softCloseDoors_spinner).equals("无"))
                options.put("softCloseDoors", getSpinnerSelectedText(rootView, R.id.csi_softCloseDoors_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_rearPowerSeats_spinner).equals("无"))
                options.put("rearPowerSeats", getSpinnerSelectedText(rootView, R.id.csi_rearPowerSeats_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_ahc_spinner).equals("无"))
                options.put("ahc", getSpinnerSelectedText(rootView, R.id.csi_ahc_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_parkAssist_spinner).equals("无"))
                options.put("parkAssist", getSpinnerSelectedText(rootView, R.id.csi_parkAssist_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.csi_clapboard_spinner).equals("无"))
                options.put("clapboard", getSpinnerSelectedText(rootView, R.id.csi_clapboard_spinner));
        } catch (JSONException e) {

        }

        return options;
    }

    //生成手续信息jsonString
    public JSONObject generateProceduresJsonObject() {
        JSONObject procedures = new JSONObject();

        try {
            procedures.put("regArea", getSpinnerSelectedText(rootView, R.id.ci_regArea_spinner));
            procedures.put("plateNumber", getEditText(rootView, R.id.ci_plateNumber_edit));
            procedures.put("licenseModel", getEditText(rootView, R.id.ci_licenseModel_edit));
            procedures.put("vehicleType", getSpinnerSelectedText(rootView, R.id.ci_vehicleType_spinner));
            procedures.put("useCharacter", getSpinnerSelectedText(rootView, R.id.ci_useCharacter_spinner));

            // 数字，单独判断
            if(getEditText(rootView, R.id.bi_mileage_edit).equals(""))
                procedures.put("mileage", 0);
            else
                procedures.put("mileage", getEditText(rootView, R.id.bi_mileage_edit));

            procedures.put("exteriorColor", getSpinnerSelectedText(rootView, R.id.ci_exteriorColor_spinner));
            procedures.put("regDate", getDateString(getSpinnerSelectedText(rootView,
                    R.id.ci_regYear_spinner), getSpinnerSelectedText(rootView,
                    R.id.ci_regMonth_spinner)));
            procedures.put("builtDate", getDateString(getSpinnerSelectedText(rootView,
                    R.id.ci_builtYear_spinner), getSpinnerSelectedText(rootView,
                    R.id.ci_builtMonth_spinner)));
            procedures.put("invoice", getSpinnerSelectedText(rootView, R.id.ct_invoice_spinner));

            // 数字，单独判断
            if(getEditText(rootView, R.id.ct_invoice_edit).equals(""))
                procedures.put("invoicePrice", 0);
            else
                procedures.put("invoicePrice", getEditText(rootView, R.id.ct_invoice_edit));

            procedures.put("surtax", getSpinnerSelectedText(rootView, R.id.ct_surtax_spinner));
            procedures.put("transferCount", getSpinnerSelectedText(rootView, R.id.ci_transferCount_spinner));
            procedures.put("transferLastDate", getDateString(getSpinnerSelectedText(rootView,
                    R.id.ci_transferLastYear_spinner), getSpinnerSelectedText(rootView,
                    R.id.ci_transferLastMonth_spinner)));
            procedures.put("annualInspection", getDateString(getSpinnerSelectedText(rootView,
                    R.id.ct_annualInspectionYear_spinner), getSpinnerSelectedText(rootView,
                    R.id.ct_annualInspectionMonth_spinner)));
            procedures.put("compulsoryInsurance", getDateString(getSpinnerSelectedText(rootView,
                    R.id.ct_compulsoryInsuranceYear_spinner), getSpinnerSelectedText(rootView,
                    R.id.ct_compulsoryInsuranceMonth_spinner)));
            procedures.put("licensePhotoMatch", getEditText(rootView, R.id.ct_licencePhotoMatch_edit));
            procedures.put("insurance", getSpinnerSelectedText(rootView, R.id.ct_insurance_spinner));
            procedures.put("insuranceRegion", getSpinnerSelectedText(rootView, R.id.ct_insuranceRegion_spinner));

            // 数字，单独判断
            if(getEditText(rootView, R.id.ct_insuranceAmount_edit).equals(""))
                procedures.put("insuranceAmount", 0);
            else
                procedures.put("insuranceAmount", getEditText(rootView, R.id.ct_insuranceAmount_edit));

            procedures.put("insuranceExpiryDate", getDateString(getSpinnerSelectedText(rootView,
                    R.id.ct_insuranceExpiryYear_spinner), getSpinnerSelectedText(rootView,
                    R.id.ct_insuranceExpiryMonth_spinner)));
            procedures.put("insuranceCompany", getSpinnerSelectedText(rootView, R.id.ct_insuranceCompany_spinner));
            procedures.put("importProcedures", getSpinnerSelectedText(rootView, R.id.ct_importProcedures_spinner));
            procedures.put("spareTire", getSpinnerSelectedText(rootView, R.id.ct_spareTire_spinner));
            procedures.put("spareKey", getSpinnerSelectedText(rootView, R.id.ct_spareKey_spinner));
            procedures.put("ownerName", getEditText(rootView, R.id.ci_ownerName_edit));
            procedures.put("ownerIdNumber", getEditText(rootView, R.id.ci_ownerIdNumber_edit));
            procedures.put("ownerPhone", getEditText(rootView, R.id.ci_ownerPhone_edit));
            //procedures.put("transferAgree", getSpinnerSelectedText(rootView,
            //        R.id.ex_transferAgree_spinner));
            //procedures.put("transferRequire", getEditText(rootView,
            //        R.id.ct_transferRequire_edit));
        } catch (JSONException e) {

        }

        return procedures;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="获取车辆配置信息的Task">

    private class GetCarSettingsTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        String seriesId;
        String modelId;

        String modelName = "";
        List<String> modelNames;
        JSONObject jsonObject;
        List<JSONObject> jsonObjects;

        Country country = null;
        Brand brand = null;
        Manufacturer manufacturer = null;
        Series series = null;
        Model model = null;
        String config = null;
        String category = null;
        String figure = null;

        private GetCarSettingsTask(Context context) {
            this.context = context;
            this.seriesId = null;
        }

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(rootView.getContext(), null,
                    "正在获取车辆信息，请稍候。。", false, false);
            model = null;
            modelName = "";
            modelNames = null;
            jsonObject = null;
            jsonObjects = null;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = false;

            // 传输seriesId和modelId
            if(!params[0].equals("")) {
                // 从传入的参数中解析出seriesId和modelId
                String temp[] = params[0].split(",");
                seriesId = temp[0];
                modelId = temp[1];

                try {
                    JSONObject jsonObject = new JSONObject();

                    // SeriesId + userID + key
                    jsonObject.put("SeriesId", seriesId);
                    jsonObject.put("ModelId", modelId);
                    jsonObject.put("UserId", MainActivity.userInfo.getId());
                    jsonObject.put("Key", MainActivity.userInfo.getKey());

                    soapService = new SoapService();

                    // 设置soap的配置
                    soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                            "http://cheyipai/IReportService/GetOptionsBySeriesIdAndModelId",
                            "GetOptionsBySeriesIdAndModelId");

                    success = soapService.communicateWithServer(jsonObject.toString());

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
                    jsonObject.put("UserId", MainActivity.userInfo.getId());
                    jsonObject.put("Key", MainActivity.userInfo.getKey());

                    soapService = new SoapService();

                    // 设置soap的配置
                    soapService.setUtils(Common.SERVER_ADDRESS + Common.REPORT_SERVICE,
                            "http://cheyipai/IReportService/GetCarConfigInfoByVin",
                            "GetCarConfigInfoByVin");

                    success = soapService.communicateWithServer(jsonObject.toString());

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

            // 如果成功通信
            if (success) {
                try {
                    // 开始位为[，表示传输的是全部信息
                    if(result.startsWith("[")) {
                        JSONArray jsonArray = new JSONArray(result);

                        // 用来存储车辆配置信息的jsonobject list
                        jsonObjects = new ArrayList<JSONObject>();

                        // 用来存储车辆型号的string list
                        modelNames = new ArrayList<String>();

                        // TODO: 如果获取的是空？或者有的为空有的不为空？
                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            jsonObjects.add(jsonObject);
                            country = vehicleModel.getCountryById(jsonObject.getString("countryId"));
                            brand = country.getBrandById(jsonObject.getString("brandId"));
                            manufacturer = brand.getProductionById(jsonObject.getString("manufacturerId"));
                            series = manufacturer.getSerialById(jsonObject.getString("seriesId"));
                            model = series.getModelById(jsonObject.getString("modelId"));
                            config = jsonObject.getString("config");
                            category = jsonObject.getString("category");
                            figure = jsonObject.getString("figure");

                            modelNames.add(manufacturer.name + " " + series.name + " " + model.name);
                        }

                        // 弹出一个对话框，供用户进行选择
                        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

                        builder.setTitle(R.string.bi_select_model);

                        String[] tempArray = new String[modelNames.size() + 1];

                        for(int i = 0; i < modelNames.size(); i++) {
                            tempArray[i] = modelNames.get(i);
                        }

                        // 加入一个“其他”，当用户点击时，直接弹出型号选择页面
                        tempArray[modelNames.size()] = "其他";

                        builder.setItems(tempArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 如果点击了“其他”（也就是传递回来的配置不符合现车）
                                if(i == modelNames.size()) {
                                    selectCarBrand();
                                } else {
                                    modelName = modelNames.get(i);
                                    jsonObject = jsonObjects.get(i);

                                    try {
                                        country = vehicleModel.getCountryById(jsonObject.getString("countryId"));
                                        brand = country.getBrandById(jsonObject.getString("brandId"));
                                        manufacturer = brand.getProductionById(jsonObject.getString("manufacturerId"));
                                        series = manufacturer.getSerialById(jsonObject.getString("seriesId"));
                                        model = series.getModelById(jsonObject.getString("modelId"));
                                        config = jsonObject.getString("config");
                                        category = jsonObject.getString("category");
                                        figure = jsonObject.getString("figure");

                                        // 根据用户选择的车型的id，记录车型选择spinner的位置
                                        lastCountryIndex = vehicleModel.getCountryNames().indexOf(country.name);
                                        lastBrandIndex = country.getBrandNames().indexOf(brand.name);
                                        lastManufacturerIndex = brand.getManufacturerNames().indexOf(manufacturer.name);
                                        lastSeriesIndex = manufacturer.getSerialNames().indexOf(series.name);
                                        lastModelIndex = series.getModelNames().indexOf(model.name);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    // 将品牌框设置文字
                                    mCarSettings.setBrandString(modelName);

                                    mCarSettings.setCountry(country);
                                    mCarSettings.setBrand(brand);
                                    mCarSettings.setManufacturer(manufacturer);
                                    mCarSettings.setSeries(series);
                                    mCarSettings.setModel(model);

                                    // 设置配置信息
                                    mCarSettings.setConfig(config);

                                    // 设置车型分类，以用于图片类型判断
                                    String categoryArray[] = getResources().getStringArray(R
                                            .array.csi_category_item);

                                    if(Integer.parseInt(category) > 0)
                                        mCarSettings.setCategory(categoryArray[Integer.parseInt
                                                (category) - 1]);
                                    else
                                        mCarSettings.setCategory(categoryArray[Integer.parseInt
                                                (category)]);

                                    mCarSettings.setFigure(figure);

                                    setCarSettingsSpinners(model.getName());

                                    // 更新UI
                                    updateUi();
                                }
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                        contentLayout.setVisibility(View.VISIBLE);
                    }
                    // 开始位为{，表示传输的是配置信息
                    else {
                        JSONObject jsonObject = new JSONObject(result);
                        config = jsonObject.getString("config");
                        category = jsonObject.getString("category");
                        figure = jsonObject.getString("figure");

                        // 设置配置信息
                        mCarSettings.setConfig(config);

                        // 设置车型分类，以用于图片类型判断
                        mCarSettings.setCategory(category);
                        mCarSettings.setFigure(figure);

                        // 更新UI
                        updateUi();
                    }
                } catch (JSONException e) {
                    Log.d("DFCarChecker", "Json解析错误：" + e.getMessage());
                }
            }
            // 如果失败
            else {
                if(soapService.getErrorMessage().equals("用户名或Key解析错误，请输入正确的用户Id和Key")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
                    builder.setTitle(R.string.bi_select_model)
                            .setMessage("连接失败，请重新登录！");

                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(rootView.getContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else if(soapService.getErrorMessage().equals("没有检测到任何有关此VIN的相关配置信息")) {
                    AlertDialog dialog = new AlertDialog.Builder(this.context).setTitle(R.string
                            .alert_title)
                            .setMessage("没有匹配的车型")
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    selectCarBrand();
                                    contentLayout.setVisibility(View.VISIBLE);
                                }
                            }).create();

                    dialog.show();

                    updateUi();
                }
                else {
                    Toast.makeText(context, soapService.getErrorMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mGetCarSettingsTask = null;
        }
    }
    // </editor-fold>

    // 提交前的检查
    public boolean runOverAllCheck() {
        int id2Check[] = {R.id.ci_licenseModel_edit,
        R.id.bi_mileage_edit,
        R.id.ct_invoice_edit,
        R.id.ct_licencePhotoMatch_edit,
        R.id.ct_insuranceAmount_edit,
        R.id.ci_ownerName_edit,
        R.id.ci_ownerIdNumber_edit,
        R.id.ci_ownerPhone_edit};

        for(int i = 0; i < id2Check.length; i++) {
            if(id2Check[i] == R.id.ct_invoice_edit) {
                // 如果选择了无购车发票
                if(ticketSpinner.getSelectedItemPosition() > 1)
                    continue;
            }

            if(id2Check[i] == R.id.ct_insuranceAmount_edit) {
                // 如果选择了无商险
                if(businessInsuranceSpinner.getSelectedItemPosition() >= 1)
                    continue;
            }

            if(Helper.getEditText(rootView, id2Check[i]).equals("")) {
                Helper.setEditError(rootView, id2Check[i]);
                Helper.setEditFocus(rootView, id2Check[i]);
                return false;
            }
        }

        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="设置各种Spinner">
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

        countrySpinner.setSelection(lastCountryIndex);
        lastCountryIndex = 0;
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

        // 如果该项只有一个条目，则默认选中，否则选中上次记录的值
        if(country != null && country.getBrandNames().size() == 2) {
            brandSpinner.setSelection(1);
        } else {
            brandSpinner.setSelection(lastBrandIndex);
        }

        lastBrandIndex = 0;
    }

    // 设置厂商Spinner
    private void setManufacturerSpinner(final Brand brand) {
        ArrayAdapter<String> adapter;

        if(brand == null) {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, Helper.getEmptyStringList());
        } else {
            adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, brand.getManufacturerNames());
        }

        manufacturerSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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

        // 如果该项只有一个条目，则默认选中，否则选中上次记录的值
        if(brand != null && brand.getManufacturerNames().size() == 2) {
            manufacturerSpinner.setSelection(1);
        } else {
            manufacturerSpinner.setSelection(lastManufacturerIndex);
        }

        lastManufacturerIndex = 0;
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

        // 如果该项只有一个条目，则默认选中，否则选中上次记录的值
        if(manufacturer != null && manufacturer.getSerialNames().size() == 2) {
            seriesSpinner.setSelection(1);
        } else {
            seriesSpinner.setSelection(lastSeriesIndex);
        }

        lastSeriesIndex = 0;
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

        // 如果该项只有一个条目，则默认选中，否则选中上次记录的值
        if(series != null && series.getModelNames().size() == 2) {
            modelSpinner.setSelection(1);
        } else {
            modelSpinner.setSelection(lastModelIndex);
        }

        lastModelIndex = 0;
    }

    // 设置车辆配置Spinner
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
        SetSpinnerData(R.id.ci_regArea_spinner, province, rootView);

        String[] provinceAbbreviationArray = getResources().getStringArray(R.array.ci_province_abbreviation);
        final List<String> provinceAbbreviation = Helper.StringArray2List(provinceAbbreviationArray);

        Spinner regLocationSpinner = (Spinner) rootView.findViewById(R.id.ci_regArea_spinner);

        regLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!modifyMode) {
                    carNumberEdit.setText(provinceAbbreviation.get(i));
                }

                modifyMode = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    // 如果为修改模式，要手动填写一下注册地，因为spinner变化会导致注册地变化
    // 同时修改一下vin
    public void littleFixAboutRegArea() {
        try {
            setEditText(rootView, R.id.ci_plateNumber_edit, procedures.getString("plateNumber"));
            //setEditText(rootView, R.id.bi_vin_edit, options.getString("vin"));
        } catch (Exception e) {

        }
    }

    // 车身颜色
    private void setCarColorSpinner()
    {
        String[] colorArray = getResources().getStringArray(R.array.ci_car_color_arrays);
        List<String> colorList = Helper.StringArray2List(colorArray);

        SetSpinnerData(R.id.ci_exteriorColor_spinner, colorList, rootView);
    }

    // 初次登记时间
    private void setFirstLogTimeSpinner()
    {
        SetSpinnerData(R.id.ci_regYear_spinner, Helper.GetYearList(20), rootView);
        SetSpinnerData(R.id.ci_regMonth_spinner, Helper.GetMonthList(), rootView);

        firstLogYearSpinner = (Spinner) rootView.findViewById(R.id.ci_regYear_spinner);
        firstLogYearSpinner.setSelection(17);
        firstLogYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 出厂日期不能晚于登记日期
                List<String> temp = Helper.GetYearList(20);
                SetSpinnerData(R.id.ci_builtYear_spinner, temp.subList(0, i + 1), rootView);
                manufactureYearSpinner.setSelection(i);

                // 最后过户时间不能早于登记日期
                SetSpinnerData(R.id.ci_transferLastYear_spinner, temp.subList(i, temp.size()), rootView);

                // 年检有效期、交强险有效期不能早于登记日期
                int from = Integer.parseInt(temp.get(i));
                int to = Calendar.getInstance().get(Calendar.YEAR) + 2;

                SetSpinnerData(R.id.ct_annualInspectionYear_spinner, Helper.GetNumbersList(from, to), rootView);
                SetSpinnerData(R.id.ct_compulsoryInsuranceYear_spinner, Helper.GetNumbersList(from, to), rootView);
                SetSpinnerData(R.id.ct_insuranceExpiryYear_spinner, Helper.GetNumbersList(from, to), rootView);

                // 商险有效期不能早于登记日期
                SetSpinnerData(R.id.ct_insuranceExpiryYear_spinner, temp.subList(i, temp.size()), rootView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
//
//        firstLogMonthSpinner = (Spinner) rootView.findViewById(R.id.ci_regMonth_spinner);
//        firstLogMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                List<String> temp = Helper.GetMonthList();
//                SetSpinnerData(R.id.ci_builtMonth_spinner, temp.subList(0, i + 1), rootView);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
    }

    // 出厂日期
    private void setManufactureTimeSpinner()
    {
        SetSpinnerData(R.id.ci_builtYear_spinner, Helper.GetYearList(20), rootView);
        SetSpinnerData(R.id.ci_builtMonth_spinner, Helper.GetMonthList(), rootView);
    }

    // 过户次数
    private void setTransferCountSpinner()
    {
        SetSpinnerData(R.id.ci_transferCount_spinner, Helper.GetNumbersList(0, 15), rootView);
    }

    // 最后过户时间
    private void setLastTransferTimeSpinner()
    {
        SetSpinnerData(R.id.ci_transferLastYear_spinner, Helper.GetYearList(17), rootView);
        SetSpinnerData(R.id.ci_transferLastMonth_spinner, Helper.GetMonthList(), rootView);
    }

    // 年检有效期
    private void setYearlyCheckAvailableDateSpinner() {
        SetSpinnerData(R.id.ct_annualInspectionYear_spinner, Helper.GetYearList(2), rootView);
        SetSpinnerData(R.id.ct_annualInspectionMonth_spinner, Helper.GetMonthList(), rootView);
    }

    // 有效期至（交强险）
    private void setAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_compulsoryInsuranceYear_spinner, Helper.GetYearList(2), rootView);
        SetSpinnerData(R.id.ct_compulsoryInsuranceMonth_spinner, Helper.GetMonthList(), rootView);
    }

    // 商险有效期
    private void setBusinessInsuranceAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_insuranceExpiryYear_spinner, Helper.GetYearList(19), rootView);
        SetSpinnerData(R.id.ct_insuranceExpiryMonth_spinner, Helper.GetMonthList(), rootView);
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
                EditText editText = (EditText)rootView.findViewById(R.id.ct_licencePhotoMatch_edit);
                String matches = getResources().getString(R.string.ci_attention_match);
                String notmatch = getResources().getString(R.string.ci_attention_notmatch);
                editText.setText(match ? matches : notmatch);
            }
        });
        builder.setNegativeButton(R.string.ci_attention_notmatch, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 不符
                match = false;
                EditText editText = (EditText)rootView.findViewById(R.id.ct_licencePhotoMatch_edit);
                String matches = getResources().getString(R.string.ci_attention_match);
                String notmatch = getResources().getString(R.string.ci_attention_notmatch);
                editText.setText(match ? matches : notmatch);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    // </editor-fold>

    private void letsEnterModifyMode() {
        try {
            JSONObject features = new JSONObject(jsonData).getJSONObject("features");

            options = features.getJSONObject("options");
            procedures = features.getJSONObject("procedures");

            String brandString = options.getString("manufacturer") + " " +
                    options.getString("series") + " " +
                    options.getString("model");

            mCarSettings.setBrandString(brandString);
            mCarSettings.setDisplacement(options.getString("displacement"));
            mCarSettings.setCategory(options.getString("category"));

            Country country = vehicleModel.getCountryById(Integer.toString(options.getInt
                    ("countryId")));
            Brand brand = country.getBrandById(Integer.toString(options.getInt("brandId")));
            Manufacturer manufacturer = brand.getProductionById(Integer.toString(options.getInt
                    ("manufacturerId")));
            Series series = manufacturer.getSerialById(Integer.toString(options.getInt
                    ("seriesId")));
            Model model = series.getModelById(Integer.toString(options.getInt("modelId")));

            mCarSettings.setCountry(country);
            mCarSettings.setBrand(brand);
            mCarSettings.setManufacturer(manufacturer);
            mCarSettings.setSeries(series);
            mCarSettings.setModel(model);
            mCarSettings.setCarSettings(options.toString());

            // 设置车型分类，以用于图片类型判断
            mCarSettings.setCategory(options.getString("category"));
            mCarSettings.setFigure("1");

            setCarSettingsSpinners(model.getName());

            this.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    //stuff that updates ui
                    contentLayout.setVisibility(View.VISIBLE);
                    tableLayout.setVisibility(View.VISIBLE);
                    CarCheckFrameFragment.showContent();
                    CarCheckIntegratedFragment.showContent();
                    // 更新UI

                    updateUi();
                }
            });

            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // vin内容覆盖
                        setEditText(rootView, R.id.bi_vin_edit, options.getString("vin"));
                        enableView(false, rootView, R.id.bi_vin_edit);
                        showView(false, rootView, R.id.bi_vin_button);
                        showView(false, rootView, R.id.bi_brand_ok_button);

                        setEditWeight(rootView, R.id.bi_vin_edit, 4f);
                        setEditWeight(rootView, R.id.bi_brand_edit, 3f);

                        // 手续组内容覆盖
                        setSpinnerSelectionWithString(rootView, R.id.ci_regArea_spinner,
                                procedures.getString("regArea"));

                        setEditText(rootView, R.id.ci_licenseModel_edit, procedures.getString("licenseModel"));
                        setSpinnerSelectionWithString(rootView, R.id.ci_vehicleType_spinner,
                                procedures.getString("vehicleType"));
                        setSpinnerSelectionWithString(rootView, R.id.ci_useCharacter_spinner,
                                procedures.getString("useCharacter"));
                        setEditText(rootView, R.id.bi_mileage_edit, procedures.getString("mileage"));
                        setSpinnerSelectionWithString(rootView, R.id.ci_exteriorColor_spinner,
                                procedures.getString("exteriorColor"));
                        setSpinnerSelectionWithString(rootView, R.id.ci_regYear_spinner,
                                procedures.getString("regDate").substring(0, 4));
                        setSpinnerSelectionWithString(rootView, R.id.ci_regMonth_spinner,
                                procedures.getString("regDate").substring(6, 7));
                        setSpinnerSelectionWithString(rootView, R.id.ci_builtYear_spinner,
                                procedures.getString("builtDate").substring(0, 4));
                        setSpinnerSelectionWithString(rootView, R.id.ci_builtMonth_spinner,
                                procedures.getString("builtDate").substring(6, 7));
                        setSpinnerSelectionWithString(rootView, R.id.ct_invoice_spinner,
                                procedures.getString("invoice"));
                        setEditText(rootView, R.id.ct_invoice_edit, procedures.getString("invoicePrice"));
                        setSpinnerSelectionWithString(rootView, R.id.ct_surtax_spinner,
                                procedures.getString("surtax"));
                        setSpinnerSelectionWithString(rootView, R.id.ci_transferCount_spinner,
                                procedures.getString("transferCount"));
                        setSpinnerSelectionWithString(rootView, R.id.ci_transferLastYear_spinner,
                                procedures.getString("transferLastDate").substring(0, 4));
                        setSpinnerSelectionWithString(rootView, R.id.ci_transferLastMonth_spinner,
                                procedures.getString("transferLastDate").substring(6, 7));
                        setEditText(rootView, R.id.ct_licencePhotoMatch_edit, procedures.getString("licensePhotoMatch"));
                        setSpinnerSelectionWithString(rootView, R.id.ct_insurance_spinner, procedures.getString("insurance"));
                        setSpinnerSelectionWithString(rootView, R.id.ct_insuranceRegion_spinner, procedures.getString("insuranceRegion"));
                        setEditText(rootView, R.id.ct_insuranceAmount_edit, procedures.getString("insuranceAmount"));
                        setSpinnerSelectionWithString(rootView, R.id.ct_insuranceCompany_spinner, procedures.getString("insuranceCompany"));
                        setSpinnerSelectionWithString(rootView, R.id.ct_importProcedures_spinner, procedures.getString("importProcedures"));
                        setSpinnerSelectionWithString(rootView, R.id.ct_spareTire_spinner, procedures.getString("spareTire"));
                        setSpinnerSelectionWithString(rootView, R.id.ct_spareKey_spinner, procedures.getString("spareKey"));
                        setEditText(rootView, R.id.ci_ownerName_edit, procedures.getString("ownerName"));
                        setEditText(rootView, R.id.ci_ownerIdNumber_edit, procedures.getString("ownerIdNumber"));
                        setEditText(rootView, R.id.ci_ownerPhone_edit, procedures.getString("ownerPhone"));
                        //setSpinnerSelectionWithString(rootView, R.id.ex_transferAgree_spinner,
                        //        procedures.getString("transferAgree"));
                        //setEditText(rootView,R.id.ct_transferRequire_edit,
                        //        procedures.getString("transferRequire"));
                        setSpinnerSelectionWithString(rootView, R.id.ct_insuranceExpiryYear_spinner,
                                procedures.getString("insuranceExpiryDate").substring(0, 4));
                        setSpinnerSelectionWithString(rootView, R.id.ct_insuranceExpiryMonth_spinner, procedures.getString("insuranceExpiryDate").substring(6, 7));
                        setSpinnerSelectionWithString(rootView, R.id.ct_annualInspectionYear_spinner, procedures.getString("annualInspection").substring(0, 4));
                        setSpinnerSelectionWithString(rootView, R.id.ct_annualInspectionMonth_spinner, procedures.getString("annualInspection").substring(6, 7));
                        setSpinnerSelectionWithString(rootView, R.id.ct_compulsoryInsuranceYear_spinner, procedures.getString("compulsoryInsurance").substring(0, 4));
                        setSpinnerSelectionWithString(rootView, R.id.ct_compulsoryInsuranceMonth_spinner, procedures.getString("compulsoryInsurance").substring(6, 7));

                        mProgressDialog.dismiss();
                    } catch (JSONException e) {

                    }
                }
            });

        } catch (Exception e) {
            Log.d(Common.TAG, e.getMessage());
        }
    }
}

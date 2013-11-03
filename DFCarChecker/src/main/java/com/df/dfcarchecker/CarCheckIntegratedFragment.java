package com.df.dfcarchecker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.df.entry.CarSettings;
import com.df.entry.FaultPhotoEntity;
import com.df.entry.PhotoEntity;
import com.df.service.Common;

import java.util.ArrayList;
import java.util.List;

public class CarCheckIntegratedFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private static ScrollView root;
    private LayoutInflater inflater;
    public static List<FaultPhotoEntity> outsidePaintEntities;
    public static List<PhotoEntity> outsidePhotoEntities;
    public static List<FaultPhotoEntity> insidePaintEntities;
    public static List<PhotoEntity> insidePhotoEntities;
    private String paintIndex;
    private String outsideComment;
    private String sealIndex;
    private String insideComment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_intergrated, container, false);

        Button outButton = (Button) rootView.findViewById(R.id.out_button);
        outButton.setOnClickListener(this);
        Button inButton = (Button) rootView.findViewById(R.id.in_button);
        inButton.setOnClickListener(this);

        root = (ScrollView)rootView.findViewById(R.id.root);
        root.setVisibility(View.GONE);

        // 坐标们
        outsidePaintEntities = new ArrayList<FaultPhotoEntity>();
        insidePaintEntities = new ArrayList<FaultPhotoEntity>();

        // 照片们
        outsidePhotoEntities = new ArrayList<PhotoEntity>();
        insidePhotoEntities = new ArrayList<PhotoEntity>();

        paintIndex = sealIndex = "0";
        outsideComment = insideComment = "";

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.out_button:
                CheckOutSide();
                break;
            case R.id.in_button:
                CheckInside();
                break;
        }
    }

    public static void ShowContent() {
        updateUi();
        root.setVisibility(View.VISIBLE);
    }

    public static void setGearType(String gearType) {
        // 手动档
        TextView manuallyTextView = (TextView)rootView.findViewById(R.id.it_gear_manually_row);
        TableRow manuallyRow1 = (TableRow)rootView.findViewById(R.id.it_gear_manually_row_1);
        TableRow manuallyRow2 = (TableRow)rootView.findViewById(R.id.it_gear_manually_row_2);
        TableRow manuallyRow3 = (TableRow)rootView.findViewById(R.id.it_gear_manually_row_3);
        TextView autoTextView = (TextView)rootView.findViewById(R.id.it_gear_auto_row);
        TableRow autoRow1 = (TableRow)rootView.findViewById(R.id.it_gear_auto_row_1);
        TableRow autoRow2 = (TableRow)rootView.findViewById(R.id.it_gear_auto_row_2);
        TableRow autoRow3 = (TableRow)rootView.findViewById(R.id.it_gear_auto_row_3);

        if(gearType.equals("MT")) {
            manuallyTextView.setVisibility(View.VISIBLE);
            manuallyRow1.setVisibility(View.VISIBLE);
            manuallyRow2.setVisibility(View.VISIBLE);
            manuallyRow3.setVisibility(View.VISIBLE);

            autoTextView.setVisibility(View.GONE);
            autoRow1.setVisibility(View.GONE);
            autoRow2.setVisibility(View.GONE);
            autoRow3.setVisibility(View.GONE);
        }
        // 自动档
        else {
            manuallyTextView.setVisibility(View.GONE);
            manuallyRow1.setVisibility(View.GONE);
            manuallyRow2.setVisibility(View.GONE);
            manuallyRow3.setVisibility(View.GONE);

            autoTextView.setVisibility(View.VISIBLE);
            autoRow1.setVisibility(View.VISIBLE);
            autoRow2.setVisibility(View.VISIBLE);
            autoRow3.setVisibility(View.VISIBLE);
        }
    }


    private static void updateUi() {
        CarSettings carSettings = CarCheckBasicInfoFragment.mCarSettings;

        setSpinnerSelection(R.id.it_airBag_spinner, Integer.parseInt(carSettings.getAirbag()));
        setSpinnerSelection(R.id.it_abs_spinner, Integer.parseInt(carSettings.getAbs()));
        setSpinnerSelection(R.id.it_powerWindows_spinner, Integer.parseInt(carSettings.getPowerWindows()));
        setSpinnerSelection(R.id.it_sunroof_spinner, Integer.parseInt(carSettings.getSunroof()));
        setSpinnerSelection(R.id.it_airConditioning_spinner, Integer.parseInt(carSettings.getAirConditioning()));
        setSpinnerSelection(R.id.it_powerSeats_spinner, Integer.parseInt(carSettings.getPowerSeats()));
        setSpinnerSelection(R.id.it_powerMirror_spinner, Integer.parseInt(carSettings.getPowerMirror()));
        setSpinnerSelection(R.id.it_reversingRadar_spinner, Integer.parseInt(carSettings.getReversingRadar()));
        setSpinnerSelection(R.id.it_reversingCamera_spinner, Integer.parseInt(carSettings.getReversingCamera()));
        setSpinnerSelection(R.id.it_softCloseDoors_spinner, Integer.parseInt(carSettings.getSoftCloseDoors()));
        setSpinnerSelection(R.id.it_rearPowerSeats_spinner, Integer.parseInt(carSettings.getRearPowerSeats()));
        setSpinnerSelection(R.id.it_ahc_spinner, Integer.parseInt(carSettings.getAhc()));
        setSpinnerSelection(R.id.it_parkAssist_spinner,  Integer.parseInt(carSettings.getParkAssist()));
    }

    private static void setSpinnerSelection(int spinnerId, int selection) {
        Spinner spinner = (Spinner) rootView.findViewById(spinnerId);

        // 如果在配置信息处是“无”，则处此也应为“无”
        if(selection == 1) {
            spinner.setSelection(2);
            spinner.setClickable(false);
            spinner.setAlpha(0.3f);
        } else {
            spinner.setSelection(0);
            spinner.setClickable(true);
            spinner.setAlpha(1.0f);
        }

        // 气囊部件特殊处理
        if(spinnerId == R.id.it_airBag_spinner) {
            if(selection == 5) {
                spinner.setSelection(2);
                spinner.setClickable(false);
                spinner.setAlpha(0.3f);
            } else {
                spinner.setSelection(0);
                spinner.setClickable(true);
                spinner.setAlpha(1.0f);
            }
        }
    }

    // 进入“外观”
    private void CheckOutSide() {
        Intent intent = new Intent(rootView.getContext(), CarCheckOutsideActivity.class);
        intent.putExtra("INDEX", paintIndex);
        intent.putExtra("COMMENT", outsideComment);
        startActivityForResult(intent, Common.IT_OUT);
    }

    // 进入“内饰”
    private void CheckInside() {
        Intent intent = new Intent(rootView.getContext(), CarCheckInsideActivity.class);
        intent.putExtra("INDEX", sealIndex);
        intent.putExtra("COMMENT", insideComment);
        startActivityForResult(intent, Common.IT_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Common.IT_WATER:
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String waterResult = bundle.getString(Common.IT_WATER_RESULT);
                            if(waterResult != null) {
                                // 保存泡水车检查的结果
                            }
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
            case Common.IT_OUT:
                if(resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String paintIndex = bundle.getString("INDEX");
                            if(paintIndex != null) {
                                // 保存车辆漆面光洁度的选择项
                                this.paintIndex = paintIndex;
                            }

                            String comment = bundle.getString("COMMENT");
                            if(comment != null) {
                                // 保存备注信息
                                this.outsideComment = comment;
                            }
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
            case Common.IT_IN:
                if(resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String sealIndex = bundle.getString("INDEX");
                            if(sealIndex != null) {
                                // 保存车辆漆面光洁度的选择项
                                this.sealIndex = sealIndex;
                            }

                            String comment = bundle.getString("COMMENT");
                            if(comment != null) {
                                // 保存备注信息
                                this.insideComment = comment;
                            }
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
        }
    }
}

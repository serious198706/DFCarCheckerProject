package com.df.dfcarchecker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.df.entry.CarSettings;
import com.df.entry.PosEntity;
import com.df.entry.PhotoEntity;
import com.df.service.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.service.Helper.getEditText;
import static com.df.service.Helper.getSpinnerSelectedText;

public class CarCheckIntegratedFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private static ScrollView root;
    public static List<PosEntity> outsidePaintEntities;
    public static List<PhotoEntity> outsidePhotoEntities;
    public static List<PosEntity> insidePaintEntities;
    public static List<PhotoEntity> insidePhotoEntities;
    private String paintIndex;
    private String outsideComment;
    private String sealIndex;
    private String insideComment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_car_check_intergrated, container, false);

        Button outButton = (Button) rootView.findViewById(R.id.out_button);
        outButton.setOnClickListener(this);
        Button inButton = (Button) rootView.findViewById(R.id.in_button);
        inButton.setOnClickListener(this);

        root = (ScrollView)rootView.findViewById(R.id.root);
        root.setVisibility(View.GONE);

        // 坐标们
        outsidePaintEntities = new ArrayList<PosEntity>();
        insidePaintEntities = new ArrayList<PosEntity>();

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
        updateAssociatedSpinners();
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


    public static void updateAssociatedSpinners() {
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
        Intent intent = new Intent(rootView.getContext(), CarCheckExteriorActivity.class);
        intent.putExtra("INDEX", paintIndex);
        intent.putExtra("COMMENT", outsideComment);
        startActivityForResult(intent, Common.IT_OUT);
    }

    // 进入“内饰”
    private void CheckInside() {
        Intent intent = new Intent(rootView.getContext(), CarCheckInteriorActivity.class);
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
                            // 保存泡水车检查的结果
                            //this.waterResult = bundle.getString(Common
                            //        .IT_WATER_RESULT);
                        }
                    }
                    catch(NullPointerException ex) {
                        Log.d(Common.TAG, "Empty bundle.");
                    }
                }
                break;
            case Common.IT_OUT:
                if(resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            // 保存车辆漆面光洁度的选择项
                            this.paintIndex = bundle.getString("INDEX");

                            // 保存备注信息
                            this.outsideComment = bundle.getString("COMMENT");
                        }
                    }
                    catch(NullPointerException ex) {
                        Log.d(Common.TAG, "Empty bundle.");
                    }
                }
                break;
            case Common.IT_IN:
                if(resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            // 保存车辆漆面光洁度的选择项
                            this.sealIndex = bundle.getString("INDEX");

                            // 保存备注信息
                            this.insideComment = bundle.getString("COMMENT");
                        }
                    }
                    catch(NullPointerException ex) {
                        Log.d(Common.TAG, "Empty bundle.");
                    }
                }
                break;
        }
    }

    public static JSONObject generateEngineJsonObject() {
        JSONObject engine = new JSONObject();

        try {
            engine.put("started", getSpinnerSelectedText(rootView, R.id.it_engineStarted_spinner));
            engine.put("steady", getSpinnerSelectedText(rootView, R.id.it_engineSteady_spinner));
            engine.put("strangeNoices", getSpinnerSelectedText(rootView,
                    R.id.it_engineStrangeNoices_spinner));
            engine.put("exhaustColor", getSpinnerSelectedText(rootView,
                    R.id.it_engineExhaustColor_spinner));
            engine.put("fluid", getSpinnerSelectedText(rootView, R.id.it_engineFluid_spinner));
        } catch (JSONException e) {

        }

        return engine;
    }

    public static JSONObject generateGearboxJsonObject() {
        JSONObject gearbox = new JSONObject();

        try {
            gearbox.put("mtClutch", getSpinnerSelectedText(rootView,
                    R.id.it_gearMtClutch_spinner));
            gearbox.put("mtShiftEasy", getSpinnerSelectedText(rootView,
                    R.id.it_gearMtShiftEasy_spinner));
            gearbox.put("mtShiftSpace", getSpinnerSelectedText(rootView,
                    R.id.it_gearMtShiftSpace_spinner));
            gearbox.put("atShiftShock", getSpinnerSelectedText(rootView,
                    R.id.it_gearAtShiftShock_spinner));
            gearbox.put("atShiftNoise", getSpinnerSelectedText(rootView,
                    R.id.it_gearAtShiftNoise_spinner));
            gearbox.put("atShiftEasy", getSpinnerSelectedText(rootView,
                    R.id.it_gearAtShiftEasy_spinner));
        } catch (JSONException e) {

        }

        return gearbox;
    }

    public static JSONObject generateFunctionJsonObject() {
        JSONObject function = new JSONObject();

        try {
            function.put("engineFault", getSpinnerSelectedText(rootView, R.id.it_engineFault_spinner));
            function.put("oilPressure", getSpinnerSelectedText(rootView, R.id.it_oilPressure_spinner));
            function.put("parkingBrake", getSpinnerSelectedText(rootView, R.id.it_parkingBrake_spinner));
            function.put("waterTemp", getSpinnerSelectedText(rootView, R.id.it_waterTemp_spinner));
            function.put("tachometer", getSpinnerSelectedText(rootView, R.id.it_tachometer_spinner));
            function.put("milometer", getSpinnerSelectedText(rootView, R.id.it_milometer_spinner));
            function.put("audio", getSpinnerSelectedText(rootView, R.id.it_audio_spinner));

            if(!getSpinnerSelectedText(rootView, R.id.it_abs_spinner).equals("无"))
                function.put("abs", getSpinnerSelectedText(rootView, R.id.it_abs_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_airBag_spinner).equals("无"))
                function.put("airBag", getSpinnerSelectedText(rootView, R.id.it_airBag_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_powerWindows_spinner).equals("无"))
                function.put("powerWindows", getSpinnerSelectedText(rootView, R.id.it_powerWindows_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_sunroof_spinner).equals("无"))
                function.put("sunroof", getSpinnerSelectedText(rootView, R.id.it_sunroof_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_airConditioning_spinner).equals("无"))
                function.put("airConditioning", getSpinnerSelectedText(rootView, R.id.it_airConditioning_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_powerSeats_spinner).equals("无"))
                function.put("powerSeats", getSpinnerSelectedText(rootView, R.id.it_powerSeats_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_powerMirror_spinner).equals("无"))
                function.put("powerMirror", getSpinnerSelectedText(rootView, R.id.it_powerMirror_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_reversingRadar_spinner).equals("无"))
                function.put("reversingRadar", getSpinnerSelectedText(rootView, R.id.it_reversingRadar_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_reversingCamera_spinner).equals("无"))
                function.put("reversingCamera", getSpinnerSelectedText(rootView, R.id.it_reversingCamera_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_softCloseDoors_spinner).equals("无"))
                function.put("softCloseDoors", getSpinnerSelectedText(rootView, R.id.it_softCloseDoors_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_rearPowerSeats_spinner).equals("无"))
                function.put("rearPowerSeats", getSpinnerSelectedText(rootView, R.id.it_rearPowerSeats_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_ahc_spinner).equals("无"))
                function.put("ahc", getSpinnerSelectedText(rootView, R.id.it_ahc_spinner));
            if(!getSpinnerSelectedText(rootView, R.id.it_parkAssist_spinner).equals("无"))
                function.put("parkAssist", getSpinnerSelectedText(rootView, R.id.it_parkAssist_spinner));
        } catch (JSONException e) {

        }

        return function;
    }

    public static JSONObject generateChassisJsonObject() {
        JSONObject chassis = new JSONObject();

        try {
            chassis.put("leftFront", getSpinnerSelectedText(rootView,
                    R.id.it_chassisLeftFront_spinner));
            chassis.put("rightFront", getSpinnerSelectedText(rootView,
                    R.id.it_chassisRightFront_spinner));
            chassis.put("leftRear", getSpinnerSelectedText(rootView,
                    R.id.it_chassisLeftRear_spinner));
            chassis.put("rightRear", getSpinnerSelectedText(rootView,
                    R.id.it_chassisRightRear_spinner));
            chassis.put("perfect", getSpinnerSelectedText(rootView, R.id.it_chassisPerfect_spinner));
            chassis.put("engineBottom", getSpinnerSelectedText(rootView,
                    R.id.it_chassisEngineBottom_spinner));
            chassis.put("gearboxBottom", getSpinnerSelectedText(rootView,
                    R.id.it_chassisGearboxBottom_spinner));
        } catch (JSONException e) {

        }

        return chassis;
    }

    public static JSONObject generateFloodedJsonObject() {
        JSONObject flooded = new JSONObject();

        try {
            flooded.put("cigarLighter", getSpinnerSelectedText(rootView,
                    R.id.it_waterCigarLighter_spinner));
            flooded.put("ashtray", getSpinnerSelectedText(rootView, R.id.it_waterAshtray_spinner));
            flooded.put("seatBelts", getSpinnerSelectedText(rootView,
                    R.id.it_waterSeatBelts_spinner));
            flooded.put("rearSeats", getSpinnerSelectedText(rootView,
                    R.id.it_waterReatSeats_spinner));
            flooded.put("trunkCorner", getSpinnerSelectedText(rootView,
                    R.id.it_waterTrunkCorner_spinner));
            flooded.put("spareTireGroove", getSpinnerSelectedText(rootView,
                    R.id.it_waterSpareTireGroove_spinner));
        } catch (JSONException e) {

        }

        return flooded;
    }

    public static String generateCommentString() {
        return getEditText(rootView, R.id.it_comment_edit);
    }
}

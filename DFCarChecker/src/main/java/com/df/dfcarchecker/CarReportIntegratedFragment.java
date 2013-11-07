package com.df.dfcarchecker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.df.entry.PosEntity;
import com.df.service.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.df.service.Helper.setTextView;

public class CarReportIntegratedFragment extends Fragment{
    private static View rootView;
    private LayoutInflater inflater;

    private String jsonData;
    private JSONObject conditions;
    private JSONObject exterior;
    private JSONObject interior;
    private JSONObject engine;
    private JSONObject gearbox;
    private JSONObject function;
    private JSONObject chassis;
    private JSONObject flooded;
    private String comment;
    private JSONObject features;
    private JSONObject options;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_report_integrated, container, false);

        parsJsonData();
        updateUi();

        return rootView;
    }

    public CarReportIntegratedFragment(String jsonData) {
        this.jsonData = jsonData;
    }

    private void parsJsonData() {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            conditions = jsonObject.getJSONObject("conditions");
            exterior = conditions.getJSONObject("exterior");
            interior = conditions.getJSONObject("interior");
            engine = conditions.getJSONObject("engine");
            gearbox = conditions.getJSONObject("gearbox");
            function = conditions.getJSONObject("function");
            chassis = conditions.getJSONObject("chassis");
            flooded = conditions.getJSONObject("flooded");
            comment = conditions.getString("comment");

            features = jsonObject.getJSONObject("features");
            options = features.getJSONObject("options");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUi() {
        try {
            setTextView(rootView, R.id.comment_edit, comment);

            setTextView(rootView, R.id.exteriorScore_text, "得分：" + exterior.getString("score"));
            setTextView(rootView, R.id.interiorScore_text, "得分：" + interior.getString("score"));
            setTextView(rootView, R.id.engineStarted_text, engine.getString("started"));
            setTextView(rootView, R.id.engineSteady_text, engine.getString("steady"));
            setTextView(rootView, R.id.engineStrangeNoices_text, engine.getString("strangeNoices"));
            setTextView(rootView, R.id.engineExhaustColor_text, engine.getString("exhaustColor"));
            setTextView(rootView, R.id.engineFluid_text, engine.getString("fluid"));
            setTextView(rootView, R.id.engineScore_text, "得分：" + engine.getString("score"));
            setTextView(rootView, R.id.gearMtClutch_text, gearbox.getString("mtClutch"));
            setTextView(rootView, R.id.gearMtShiftEasy_text, gearbox.getString("mtShiftEasy"));
            setTextView(rootView, R.id.gearMtShiftSpace_text, gearbox.getString("mtShiftSpace"));
            setTextView(rootView, R.id.gearAtShiftShock_text, gearbox.getString("atShiftShock"));
            setTextView(rootView, R.id.gearAtShiftNoise_text, gearbox.getString("atShiftNoise"));
            setTextView(rootView, R.id.gearAtShiftEasy_text, gearbox.getString("atShiftEasy"));
            setTextView(rootView, R.id.gearboxScore_text, "得分：" + gearbox.getString("score"));
            setTextView(rootView, R.id.engineFault_text, function.getString("engineFault"));
            setTextView(rootView, R.id.oilPressure_text, function.getString("oilPressure"));
            setTextView(rootView, R.id.parkingBrake_text, function.getString("parkingBrake"));
            setTextView(rootView, R.id.waterTemp_text, function.getString("waterTemp"));
            setTextView(rootView, R.id.tachometer_text, function.getString("tachometer"));
            setTextView(rootView, R.id.milometer_text, function.getString("milometer"));
            setTextView(rootView, R.id.audio_text, function.getString("audio"));
            if(function.has("abs"))
                setTextView(rootView, R.id.abs_text, function.getString("abs"));
            else
                setTextView(rootView, R.id.abs_text, null);
            if(function.has("airBag"))
                setTextView(rootView, R.id.airBag_text, function.getString("airBag"));
            else
                setTextView(rootView, R.id.airBag_text, null);
            if(function.has("powerWindows"))
                setTextView(rootView, R.id.powerWindows_text, function.getString("powerWindows"));
            else
                setTextView(rootView, R.id.powerWindows_text, null);
            if(function.has("sunroof"))
                setTextView(rootView, R.id.sunroof_text, function.getString("sunroof"));
            else
                setTextView(rootView, R.id.sunroof_text, null);
            if(function.has("airConditioning"))
                setTextView(rootView, R.id.airConditioning_text, function.getString("airConditioning"));
            else
                setTextView(rootView, R.id.airConditioning_text, null);
            if(function.has("powerSeats"))
                setTextView(rootView, R.id.powerSeats_text, function.getString("powerSeats"));
            else
                setTextView(rootView, R.id.powerSeats_text, null);
            if(function.has("powerMirror"))
                setTextView(rootView, R.id.powerMirror_text, function.getString("powerMirror"));
            else
                setTextView(rootView, R.id.powerMirror_text, null);
            if(function.has("reversingRadar"))
                setTextView(rootView, R.id.reversingRadar_text, function.getString("reversingRadar"));
            else
                setTextView(rootView, R.id.reversingRadar_text, null);
            if(function.has("reversingCamera"))
                setTextView(rootView, R.id.reversingCamera_text, function.getString("reversingCamera"));
            else
                setTextView(rootView, R.id.reversingCamera_text, null);
            if(function.has("softCloseDoors"))
                setTextView(rootView, R.id.softCloseDoors_text, function.getString("softCloseDoors"));
            else
                setTextView(rootView, R.id.softCloseDoors_text, null);
            if(function.has("rearPowerSeats"))
                setTextView(rootView, R.id.rearPowerSeats_text, function.getString("rearPowerSeats"));
            else
                setTextView(rootView, R.id.rearPowerSeats_text, null);
            if(function.has("ahc"))
                setTextView(rootView, R.id.ahc_text, function.getString("ahc"));
            else
                setTextView(rootView, R.id.ahc_text, null);
            if(function.has("parkAssist"))
                setTextView(rootView, R.id.parkAssist_text, function.getString("parkAssist"));
            else
                setTextView(rootView, R.id.parkAssist_text, null);
            setTextView(rootView, R.id.functionScore_text, "得分：" + function.getString("score"));
            setTextView(rootView, R.id.chassisLeftFront_text, chassis.getString("leftFront"));
            setTextView(rootView, R.id.chassisRightFront_text, chassis.getString("rightFront"));
            setTextView(rootView, R.id.chassisLeftRear_text, chassis.getString("leftRear"));
            setTextView(rootView, R.id.chassisRightRear_text, chassis.getString("rightRear"));
            setTextView(rootView, R.id.chassisPerfect_text, chassis.getString("perfect"));
            setTextView(rootView, R.id.chassisEngineBottom_text, chassis.getString("engineBottom"));
            setTextView(rootView, R.id.chassisGearboxBottom_text, chassis.getString("gearboxBottom"));
            setTextView(rootView, R.id.chassisScore_text, "得分：" + chassis.getString("score"));
            setTextView(rootView, R.id.waterCigarLighter_text, flooded.getString("cigarLighter"));
            setTextView(rootView, R.id.waterAshtray_text, flooded.getString("ashtray"));
            setTextView(rootView, R.id.waterSeatBelts_text, flooded.getString("seatBelts"));
            setTextView(rootView, R.id.waterReatSeats_text, flooded.getString("rearSeats"));
            setTextView(rootView, R.id.waterTrunkCorner_text, flooded.getString("trunkCorner"));
            setTextView(rootView, R.id.waterSpareTireGroove_text, flooded.getString("spareTireGroove"));

            if(options.getString("transmission").equals("MT")) {
                SetViewVisibility(R.id.it_gear_manually_row, View.VISIBLE);
                SetViewVisibility(R.id.it_gear_manually_row_1, View.VISIBLE);
                SetViewVisibility(R.id.it_gear_manually_row_2, View.VISIBLE);
                SetViewVisibility(R.id.it_gear_manually_row_3, View.VISIBLE);

                SetViewVisibility(R.id.it_gear_auto_row, View.GONE);
                SetViewVisibility(R.id.it_gear_auto_row_1, View.GONE);
                SetViewVisibility(R.id.it_gear_auto_row_2, View.GONE);
                SetViewVisibility(R.id.it_gear_auto_row_3, View.GONE);
            }
            // 自动档
            else {
                SetViewVisibility(R.id.it_gear_manually_row, View.GONE);
                SetViewVisibility(R.id.it_gear_manually_row_1, View.GONE);
                SetViewVisibility(R.id.it_gear_manually_row_2, View.GONE);
                SetViewVisibility(R.id.it_gear_manually_row_3, View.GONE);

                SetViewVisibility(R.id.it_gear_auto_row, View.VISIBLE);
                SetViewVisibility(R.id.it_gear_auto_row_1, View.VISIBLE);
                SetViewVisibility(R.id.it_gear_auto_row_2, View.VISIBLE);
                SetViewVisibility(R.id.it_gear_auto_row_3, View.VISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void SetViewVisibility(int viewId, int visibility) {
        View view  = (View)rootView.findViewById(viewId);
        view.setVisibility(visibility);
    }
}

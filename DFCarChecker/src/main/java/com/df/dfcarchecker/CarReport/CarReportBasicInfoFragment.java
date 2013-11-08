package com.df.dfcarchecker.CarReport;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.df.dfcarchecker.R;
import com.df.entry.CarSettings;
import com.df.entry.Manufacturer;
import com.df.service.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.df.service.Helper.setTextView;

public class CarReportBasicInfoFragment extends Fragment {
    private static View rootView;
    private LayoutInflater inflater;

    private String jsonData;

    private JSONObject features;
    private JSONObject options;
    private JSONObject procedures;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_report_basic_info, container, false);

        if(jsonData != null) {
            if(parsJsonData())
                updateUi();
        }

        return rootView;
    }

    public CarReportBasicInfoFragment(String jsonData) {
        this.jsonData = jsonData;
    }

    private boolean parsJsonData() {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            features = jsonObject.getJSONObject("features");
            options = features.getJSONObject("options");
            procedures = features.getJSONObject("procedures");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(Common.TAG, "JSON解析失败！" + e.getMessage());

            return false;
        }

        return true;
    }

    private void updateUi() {
        try {
            setTextView(rootView, R.id.vin_text, options.getString("vin"));
            String brandString = options.getString("manufacturer") + " " +
                    options.getString("series") + " " +
                    options.getString("model");
            setTextView(rootView, R.id.brand_text, brandString);
            setTextView(rootView, R.id.displacement_text, options.getString("displacement"));
            setTextView(rootView, R.id.driveType_text, options.getString("driveType"));
            setTextView(rootView, R.id.transmission_text, options.getString("transmission"));

            if(options.has("airBags"))
                setTextView(rootView, R.id.airBags_text, options.getString("airBags"));
            else
                setTextView(rootView, R.id.airBags_text, null);
            if(options.has("displacement"))
                setTextView(rootView, R.id.displacement_text, options.getString("displacement"));
            else
                setTextView(rootView, R.id.displacement_text, null);
            if(options.has("driveType"))
                setTextView(rootView, R.id.driveType_text, options.getString("driveType"));
            else
                setTextView(rootView, R.id.driveType_text, null);
            if(options.has("transmission"))
                setTextView(rootView, R.id.transmission_text, options.getString("transmission"));
            else
                setTextView(rootView, R.id.transmission_text, null);
            if(options.has("airBags"))
                setTextView(rootView, R.id.airBags_text, options.getString("airBags"));
            else
                setTextView(rootView, R.id.airBags_text, null);
            if(options.has("abs"))
                setTextView(rootView, R.id.abs_text, options.getString("abs"));
            else
                setTextView(rootView, R.id.abs_text, null);
            if(options.has("powerSteering"))
                setTextView(rootView, R.id.powerSteering_text, options.getString("powerSteering"));
            else
                setTextView(rootView, R.id.powerSteering_text, null);
            if(options.has("powerWindows"))
                setTextView(rootView, R.id.powerWindows_text, options.getString("powerWindows"));
            else
                setTextView(rootView, R.id.powerWindows_text, null);
            if(options.has("sunroof"))
                setTextView(rootView, R.id.sunroof_text, options.getString("sunroof"));
            else
                setTextView(rootView, R.id.sunroof_text, null);
            if(options.has("airConditioning"))
                setTextView(rootView, R.id.airConditioning_text, options.getString("airConditioning"));
            else
                setTextView(rootView, R.id.airConditioning_text, null);
            if(options.has("leatherSeats"))
                setTextView(rootView, R.id.leatherSeats_text, options.getString("leatherSeats"));
            else
                setTextView(rootView, R.id.leatherSeats_text, null);
            if(options.has("powerSeats"))
                setTextView(rootView, R.id.powerSeats_text, options.getString("powerSeats"));
            else
                setTextView(rootView, R.id.powerSeats_text, null);
            if(options.has("powerMirror"))
                setTextView(rootView, R.id.powerMirror_text, options.getString("powerMirror"));
            else
                setTextView(rootView, R.id.powerMirror_text, null);
            if(options.has("reversingRadar"))
                setTextView(rootView, R.id.reversingRadar_text, options.getString("reversingRadar"));
            else
                setTextView(rootView, R.id.reversingRadar_text, null);
            if(options.has("reversingCamera"))
                setTextView(rootView, R.id.reversingCamera_text, options.getString("reversingCamera"));
            else
                setTextView(rootView, R.id.reversingCamera_text, null);
            if(options.has("ccs"))
                setTextView(rootView, R.id.ccs_text, options.getString("ccs"));
            else
                setTextView(rootView, R.id.ccs_text, null);
            if(options.has("softCloseDoors"))
                setTextView(rootView, R.id.softCloseDoors_text, options.getString("softCloseDoors"));
            else
                setTextView(rootView, R.id.softCloseDoors_text, null);
            if(options.has("rearPowerSeats"))
                setTextView(rootView, R.id.rearPowerSeats_text, options.getString("rearPowerSeats"));
            else
                setTextView(rootView, R.id.rearPowerSeats_text, null);
            if(options.has("ahc"))
                setTextView(rootView, R.id.ahc_text, options.getString("ahc"));
            else
                setTextView(rootView, R.id.ahc_text, null);
            if(options.has("parkAssist"))
                setTextView(rootView, R.id.parkAssist_text, options.getString("parkAssist"));
            else
                setTextView(rootView, R.id.parkAssist_text, null);
            if(options.has("clapBoard"))
                setTextView(rootView, R.id.clapboard_text, options.getString("clapBoard"));
            else
                setTextView(rootView, R.id.clapboard_text, null);



            if(procedures.has("regArea"))
                setTextView(rootView, R.id.regArea_text, procedures.getString("regArea"));
            else
                setTextView(rootView, R.id.regArea_text, null);
            if(procedures.has("plateNumber"))
                setTextView(rootView, R.id.plateNumber_text, procedures.getString("plateNumber"));
            else
                setTextView(rootView, R.id.plateNumber_text, null);
            if(procedures.has("licenseModel"))
                setTextView(rootView, R.id.licenceModel_text, procedures.getString("licenseModel"));
            else
                setTextView(rootView, R.id.licenceModel_text, null);
            if(procedures.has("vehicleType"))
                setTextView(rootView, R.id.vehicleType_text, procedures.getString("vehicleType"));
            else
                setTextView(rootView, R.id.vehicleType_text, null);
            if(procedures.has("useCharacter"))
                setTextView(rootView, R.id.useCharacter_text, procedures.getString("useCharacter"));
            else
                setTextView(rootView, R.id.useCharacter_text, null);
            if(procedures.has("mileage"))
                setTextView(rootView, R.id.mileAge_text, procedures.getString("mileage"));
            else
                setTextView(rootView, R.id.mileAge_text, null);
            if(procedures.has("exteriorColor"))
                setTextView(rootView, R.id.exteriorColor_text, procedures.getString("exteriorColor"));
            else
                setTextView(rootView, R.id.exteriorColor_text, null);

            setTextView(rootView, R.id.regDate_text, procedures.getString("regDate"));
            setTextView(rootView, R.id.builtDate_text, procedures.getString("builtDate"));
            setTextView(rootView, R.id.transferLastDate_text, procedures.getString("transferLastDate"));
            setTextView(rootView, R.id.annualInspectionDate_text, procedures.getString("annualInspectionDate"));
            setTextView(rootView, R.id.compulsoryInsuranceDate_text, procedures.getString("compulsoryInsuranceDate"));
            setTextView(rootView, R.id.insuranceExpiryDate_text, procedures.getString("insuranceExpiryDate"));

            if(procedures.has("invoice"))
                setTextView(rootView, R.id.invoice_text, procedures.getString("invoice"));
            else
                setTextView(rootView, R.id.invoice_text, null);
            if(procedures.has("invoicePrice"))
                setTextView(rootView, R.id.invoice_text, procedures.getString("invoicePrice"));
            else
                setTextView(rootView, R.id.invoice_text, null);
            if(procedures.has("surtax"))
                setTextView(rootView, R.id.surtax_text, procedures.getString("surtax"));
            else
                setTextView(rootView, R.id.surtax_text, null);
            if(procedures.has("transferCount"))
                setTextView(rootView, R.id.transferCount_text, procedures.getString("transferCount"));
            else
                setTextView(rootView, R.id.transferCount_text, null);
            if(procedures.has("licensePhotoMatch"))
                setTextView(rootView, R.id.licencePhotoMatch_text, procedures.getString("licensePhotoMatch"));
            else
                setTextView(rootView, R.id.licencePhotoMatch_text, null);
            if(procedures.has("insurance"))
                setTextView(rootView, R.id.insurance_text, procedures.getString("insurance"));
            else
                setTextView(rootView, R.id.insurance_text, null);
            if(procedures.has("insuranceRegion"))
                setTextView(rootView, R.id.insuranceRegion_text, procedures.getString("insuranceRegion"));
            else
                setTextView(rootView, R.id.insuranceRegion_text, null);
            if(procedures.has("insuranceAmount"))
                setTextView(rootView, R.id.insuranceAmount_text, procedures.getString("insuranceAmount"));
            else
                setTextView(rootView, R.id.insuranceAmount_text, null);
            if(procedures.has("insuranceCompany"))
                setTextView(rootView, R.id.insuranceCompany_text, procedures.getString("insuranceCompany"));
            else
                setTextView(rootView, R.id.insuranceCompany_text, null);
            if(procedures.has("importProcedures"))
                setTextView(rootView, R.id.importProcedures_text, procedures.getString("importProcedures"));
            else
                setTextView(rootView, R.id.importProcedures_text, null);
            if(procedures.has("spareTire"))
                setTextView(rootView, R.id.spareTire_text, procedures.getString("spareTire"));
            else
                setTextView(rootView, R.id.spareTire_text, null);
            if(procedures.has("spareKey"))
                setTextView(rootView, R.id.spareKey_text, procedures.getString("spareKey"));
            else
                setTextView(rootView, R.id.spareKey_text, null);
            if(procedures.has("ownerName"))
                setTextView(rootView, R.id.ownerName_text, procedures.getString("ownerName"));
            else
                setTextView(rootView, R.id.ownerName_text, null);
            if(procedures.has("ownerIdNumber"))
                setTextView(rootView, R.id.ownerIdNumber_text, procedures.getString("ownerIdNumber"));
            else
                setTextView(rootView, R.id.ownerIdNumber_text, null);
            if(procedures.has("ownerPhone"))
                setTextView(rootView, R.id.ownerPhone_text, procedures.getString("ownerPhone"));
            else
                setTextView(rootView, R.id.ownerPhone_text, null);
            if(procedures.has("transferAgree"))
                setTextView(rootView, R.id.transferAgree_text, procedures.getString("transferAgree"));
            else
                setTextView(rootView, R.id.transferAgree_text, null);
            if(procedures.has("transferRequire"))
                setTextView(rootView, R.id.transferRequire_text, procedures.getString("transferRequire"));
            else
                setTextView(rootView, R.id.transferRequire_text, null);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

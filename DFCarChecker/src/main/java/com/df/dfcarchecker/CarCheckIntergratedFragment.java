package com.df.dfcarchecker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.df.service.Common;

public class CarCheckIntergratedFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.activity_car_check_intergrated, container, false);

        Button waterButton = (Button)rootView.findViewById(R.id.it_other_water_button);
        waterButton.setOnClickListener(this);
        Button tireButton = (Button)rootView.findViewById(R.id.it_other_tire_button);
        tireButton.setOnClickListener(this);
        Button fireButton = (Button)rootView.findViewById(R.id.it_other_fire_button);
        fireButton.setOnClickListener(this);
        Button electricityButton = (Button)rootView.findViewById(R.id.it_other_electricity_button);
        electricityButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.it_other_water_button:
                CheckWaterCar(v);
                break;
            case R.id.it_other_tire_button:
                CheckTire(v);
                break;
            case R.id.it_other_fire_button:
                CheckFireCar(v);
                break;
            case R.id.it_other_electricity_button:
                CheckElectricityCar(v);
                break;
        }
    }

    private void CheckWaterCar(View v) {
        Intent intent = new Intent(rootView.getContext(), CarCheckWaterActivity.class);
        startActivityForResult(intent, Common.IT_WATER);
    }

    private void CheckTire(View v) {
        Intent intent = new Intent(rootView.getContext(), CarCheckTireActivity.class);
        startActivityForResult(intent, Common.IT_TIRE);
    }

    private void CheckFireCar(View v) {
        Intent intent = new Intent(rootView.getContext(), CarCheckWaterActivity.class);
        startActivityForResult(intent, Common.IT_FIRE);
    }

    private void CheckElectricityCar(View v) {
        Intent intent = new Intent(rootView.getContext(), CarCheckWaterActivity.class);
        startActivityForResult(intent, Common.IT_ELECTRICITY);
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
            case Common.IT_TIRE:
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String tireResult = bundle.getString(Common.IT_TIRE_RESULT);
                            if(tireResult != null) {
                                // 保存轮胎检查的结果
                            }
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
            case Common.IT_FIRE:
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String fireResult = bundle.getString(Common.IT_FIRE_RESULT);
                            if(fireResult != null) {
                                // 保存火烧车检查的结果
                            }
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
            case Common.IT_ELECTRICITY:
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String electricityResult = bundle.getString(Common.IT_ELECTRICITY_RESULT);
                            if(electricityResult != null) {
                                // 保存电控单元检查的结果
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

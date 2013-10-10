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
        rootView = inflater.inflate(R.layout.fragment_car_check_intergrated, container, false);

        Button outButton = (Button) rootView.findViewById(R.id.out_button);
        outButton.setOnClickListener(this);
        Button inButton = (Button) rootView.findViewById(R.id.in_button);
        inButton.setOnClickListener(this);
        Button chassisButton = (Button) rootView.findViewById(R.id.it_chassis_button);
        chassisButton.setOnClickListener(this);
        Button waterButton = (Button)rootView.findViewById(R.id.it_other_water_button);
        waterButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.it_other_water_button:
                CheckWaterCar();
                break;
            case R.id.out_button:
                CheckOutSide();
                break;
            case R.id.it_chassis_button:
                CheckChassis();
                break;
            case R.id.in_button:
                CheckInside();
                break;
        }
    }

    private void CheckOutSide() {
        Intent intent = new Intent(rootView.getContext(), CarCheckOutsideActivity.class);
        startActivityForResult(intent, Common.IT_OUT);
    }

    private void CheckInside() {
        Intent intent = new Intent(rootView.getContext(), CarCheckInsideActivity.class);
        startActivityForResult(intent, Common.IT_IN);
    }

    private void CheckChassis() {

    }

    private void CheckWaterCar() {
        Intent intent = new Intent(rootView.getContext(), CarCheckWaterActivity.class);
        startActivityForResult(intent, Common.IT_WATER);
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
        }
    }
}

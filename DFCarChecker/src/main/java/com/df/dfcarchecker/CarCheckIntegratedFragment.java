package com.df.dfcarchecker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.Toast;

import com.df.service.Common;

public class CarCheckIntegratedFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;

    private int[][] csi_map = {
            {0, R.id.csi_airbag, View.GONE},
            {1, R.id.csi_abs, View.GONE},
            {2, R.id.csi_turn_help, View.GONE},
            {3, R.id.csi_ele_windows, View.GONE},
            {4, R.id.csi_sky_light, View.GONE},
            {5, R.id.csi_air_conditioner, View.GONE},
            {6, R.id.csi_feather_seat, View.GONE},
            {7, R.id.csi_ele_seat, View.GONE},
            {8, R.id.csi_ele_reflect_mirror, View.GONE},
            {9, R.id.csi_parking_sensors, View.GONE},
            {10, R.id.csi_parking_video, View.GONE},
            {11, R.id.csi_ccs, View.GONE},
            {12, R.id.csi_soft_close_doors, View.GONE},
            {13, R.id.csi_rear_ele_seats, View.GONE},
            {14, R.id.csi_auto_chassis, View.GONE},
            {15, R.id.csi_auto_parking, View.GONE},
            {16, R.id.csi_curtain, View.GONE}
    };

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

        HandelCSITableRow(CarCheckBasicInfoFragment.carSets);

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

//    @Override
//    public void onResume() {
//        Toast.makeText(rootView.getContext(), "front~", Toast.LENGTH_SHORT).show();
//        super.onResume();
//    }

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

    private void HandelCSITableRow(int[] tableRow) {
        for(int i = 0; i < tableRow.length; i++) {
            csi_map[tableRow[i]][2] = View.VISIBLE;
        }


        for(int i = 0; i < csi_map.length; i++) {
            // 将每一行的状态进行更新
            TableRow row = (TableRow) rootView.findViewById(csi_map[i][1]);
            row.setVisibility(csi_map[i][2]);
        }
    }
}

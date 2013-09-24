package com.df.dfcarchecker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CarReportAccidentFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_report_accident, container, false);

        return rootView;
    }

    @Override
    public void onClick(View v) {

    }
}
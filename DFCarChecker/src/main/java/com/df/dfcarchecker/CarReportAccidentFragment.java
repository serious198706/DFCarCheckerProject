package com.df.dfcarchecker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.df.service.ExtendableGridView;

import java.util.ArrayList;
import java.util.List;

public class CarReportAccidentFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_report_accident, container, false);

        ExtendableGridView grid = (ExtendableGridView)rootView.findViewById(R.id.cr_base_point_data_gridView);
        grid.setExpanded(true);
        ArrayAdapter adapter = new ArrayAdapter(rootView.getContext(), android.R.layout.simple_list_item_1, getBasePointData());
        grid.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onClick(View v) {

    }

    private String[] getBasePointData() {
        List<String> content = new ArrayList<String>();
        String[] part = getResources().getStringArray(R.array.ac_part_name);

        for(int i = 0; i < part.length; i++) {
            content.add(part[i]);

            int randomCarNumber = 100 + (int)(Math.random() * ((300 - 100) + 1));
            content.add(Integer.toString(randomCarNumber));
        }

        String[] result = new String[content.size()];

        for(int i = 0; i < result.length; i++) {
            result[i] = content.get(i);
        }

        return result;
    }
}
package com.df.dfcarchecker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TableRow;

/**
 * Created by 岩 on 13-9-2.
 */
public class ProcedureInputExchangeFragment extends Fragment {
    private static View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_procedureinput_ex, container, false);

        Spinner ci_violated_spinner = (Spinner) rootView.findViewById(R.id.ex_exchange_time_type_spinner);
        ci_violated_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                switch (position){
                    case 0:
                    {
                        TableRow tableRow1 = (TableRow) rootView.findViewById(R.id.ex_exchange_time_area1);
                        tableRow1.setVisibility(View.VISIBLE);
                        TableRow tableRow2 = (TableRow) rootView.findViewById(R.id.ex_exchange_time_area2);
                        tableRow2.setVisibility(View.GONE);
                        break;
                    }
                    case 1:
                    {
                        TableRow tableRow1 = (TableRow) rootView.findViewById(R.id.ex_exchange_time_area1);
                        tableRow1.setVisibility(View.GONE);
                        TableRow tableRow2 = (TableRow) rootView.findViewById(R.id.ex_exchange_time_area2);
                        tableRow2.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });


        return rootView;
    }
}

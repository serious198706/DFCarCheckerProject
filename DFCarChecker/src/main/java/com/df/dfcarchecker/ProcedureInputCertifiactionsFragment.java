package com.df.dfcarchecker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

/**
 * Created by 岩 on 13-9-2.
 */
public class ProcedureInputCertifiactionsFragment extends Fragment {
    private static View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_procedureinput_ct, container, false);

        Spinner ci_violated_spinner = (Spinner) rootView.findViewById(R.id.ct_available_date_month_spinner);
        ci_violated_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                switch (position){
                    case 0:
                    case 2:
                    case 4:
                    case 6:
                    case 7:
                    case 9:
                    case 11:
                        SetSpinnerData(R.id.ct_available_date_day_spinner, Helper.GetDayList(31));
                        break;
                    case 1:
                        SetSpinnerData(R.id.ct_available_date_day_spinner, Helper.GetDayList(28));
                        break;
                    default:
                        SetSpinnerData(R.id.ct_available_date_day_spinner, Helper.GetDayList(30));
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });


        SetYearlyCheckAvailableDateSpinner();
        SetAvailableDateYearSpinner();
        SetBusinessInsuranceAvailableDateYearSpinner();
        return rootView;
    }

    private void SetYearlyCheckAvailableDateSpinner() {
        SetSpinnerData(R.id.ct_yearly_check_available_date_year_spinner, Helper.GetYearList(19));
        SetSpinnerData(R.id.ct_yearly_check_available_date_month_spinner, Helper.GetMonthList());
    }

    private void SetAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_available_date_year_spinner, Helper.GetYearList(19));
        SetSpinnerData(R.id.ct_available_date_month_spinner, Helper.GetMonthList());
        SetSpinnerData(R.id.ct_available_date_day_spinner, Helper.GetDayList(31));
    }

    private void SetBusinessInsuranceAvailableDateYearSpinner() {
        SetSpinnerData(R.id.ct_business_insurance_available_date_year_spinner, Helper.GetYearList(19));
        SetSpinnerData(R.id.ct_business_insurance_available_date_month_spinner, Helper.GetMonthList());
        SetSpinnerData(R.id.ct_business_insurance_available_date_day_spinner, Helper.GetDayList(31));
    }

    private void SetSpinnerData(int redID, List<String> list)
    {
        Spinner spinner = (Spinner)rootView.findViewById(redID);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(rootView.getContext(), android.R.layout.simple_spinner_item, list);

        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
}

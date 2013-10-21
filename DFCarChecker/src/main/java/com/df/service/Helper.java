package com.df.service;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by on 13-8-30.
 */
public class Helper {
    public static List<String> GetYearList(int count)
    {
        int year = Calendar.getInstance().get(Calendar.YEAR);

        List<String> yearList = new ArrayList<String>();

        for(int i = year - count; i <= year; i++)
        {
            yearList.add(Integer.toString(i));
        }

        return  yearList;
    }

    public static List<String> GetMonthList()
    {
        List<String> monthList = new ArrayList<String>();

        for(int i = 0; i< 12; i++)
        {
            monthList.add(Integer.toString(i + 1));
        }

        return monthList;
    }

    public static List<String> GetDayList(int days)
    {
        List<String> dayList = new ArrayList<String>();

        for(int i = 0; i< days; i++)
        {
            dayList.add(Integer.toString(i + 1));
        }

        return dayList;
    }

    public static List<String> GetNumbersList(int from, int to) {
        if(from > to) {
            return null;
        }

        List<String> numberList = new ArrayList<String>();

        for(int i = from; i <= to; i++) {
            numberList.add(Integer.toString(i));
        }

        return numberList;
    }


    public static List<String> StringArray2List(String[] array)
    {
        List<String> list = new ArrayList<String>();

        for(int i = 0; i < array.length; i++)
        {
            list.add(array[i]);
        }

        return list;
    }

    public static void SetSpinnerData(int redID, List<String> list, View view)
    {
        Spinner spinner = (Spinner)view.findViewById(redID);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, list);

        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public static List<String> getEmptyStringList() {
        List<String> emptyStringList = new ArrayList<String>();
        emptyStringList.add("");

        return  emptyStringList;
    }

    public static void showView(boolean show, View view, int id) {
        view.findViewById(id).setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

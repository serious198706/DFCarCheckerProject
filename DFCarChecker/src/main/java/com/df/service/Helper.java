package com.df.service;

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

        for(int i = 0; i < count; i++)
        {
            yearList.add(Integer.toString(year - i));
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


    public static List<String> StringArray2List(String[] array)
    {
        List<String> list = new ArrayList<String>();

        for(int i = 0; i < array.length; i++)
        {
            list.add(array[i]);
        }

        return list;
    }
}
package com.df.service;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.net.ContentHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

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


    /** Create a file Uri for saving an image*/
    public static Uri getOutputMediaFileUri(String fileName){
        return Uri.fromFile(getOutputMediaFile(fileName));
    }

    /** Create a File for saving an image*/
    private static File getOutputMediaFile(String fileName){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "DFCarChecker");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("DFCarChecker", "failed to create directory");
                return null;
            }
        }

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                fileName + ".jpg");

        return mediaFile;
    }

    public static boolean isVin(String vin) {
        boolean find = Pattern.compile("^([0123456789ABCDEFGHJKLMNPRSTUVWXYZ]){12}(\\d){5}$").matcher(vin).find();

        // 如果前12位不为数字或字母，或者后5位不为数字，则错误
        if(!find) {
            return false;
        }

        find = Pattern.compile("^[UZ]{1}$").matcher(vin.substring(9, 10)).find();
        // 如果第9位是U或者Z，则错误
        if(find) {
            return false;
        }

        String lab = "0123456789ABCDEFGHJKLMNPRSTUVWXYZ";

        int[] val = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 7, 9, 2, 3, 4, 5, 6, 7, 8, 9 };
        int[] idx = { 8, 7, 6, 5, 4, 3, 2, 10, 1, 9, 8, 7, 6, 5, 4, 3, 2 };
        int value = 0;

        for (int i = 0; i < vin.length(); i++)
        {
            if (i == 8) continue;
            value += val[lab.indexOf(vin.substring(i, i + 1))] * idx[i];
        }

        return (vin.substring(8, 9).equals(Integer.toString(value % 11).replace("10", "X")));
    }
}

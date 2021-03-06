package com.df.service;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ContentHandler;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

    public static void enableView(boolean enable, View view, int id) {
        view.findViewById(id).setEnabled(enable);
    }


    /** Create a file Uri for saving an image*/
    public static Uri getOutputMediaFileUri(long fileName){
        return Uri.fromFile(getOutputMediaFile(Long.toString(fileName)));
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


    public static String getSpinnerSelectedText(View view, int spinnerId) {
        Spinner spinner = (Spinner)view.findViewById(spinnerId);

        return spinner.getSelectedItem().toString();
    }

    public static void setSpinnerSelectionWithString(View view, int spinnerId, String text) {
        Spinner spinner = (Spinner)view.findViewById(spinnerId);

        int count = spinner.getCount();
        for(int i = 0; i < count; i++) {
            if(spinner.getItemAtPosition(i).toString().equals(text))
                spinner.setSelection(i);
        }
    }

    public static int getSpinnerSelectedIndex(View view, int spinnerId) {
        Spinner spinner = (Spinner)view.findViewById(spinnerId);

        return spinner.getSelectedItemPosition();
    }

    public static String getEditText(View view, int editId) {
        EditText editText = (EditText)view.findViewById(editId);

        return editText.getText().toString();
    }

    public static String getDateString(String year, String month) {
        return year + "-" + (month.length() == 1 ? "0" + month : month);
    }

    public static void setTextView(View view, int textId, String string) {
        TextView textView = (TextView)view.findViewById(textId);

        if(string == null)
            textView.setText("无");
        else
            textView.setText(string);
    }

    public static void setEditFocus(View view, int editId) {
        EditText editText = (EditText)view.findViewById(editId);
        editText.requestFocus();
    }

    public static void setEditError(View view, int editId) {
        EditText editText = (EditText)view.findViewById(editId);
        editText.setError("请填写必要字段！");
    }

    public static void setEditText(View view, int editId, String text) {
        EditText editText = (EditText)view.findViewById(editId);

        editText.setText(text);
    }

    public static void setEditWeight(View view, int editId, float weight) {
        EditText editText = (EditText)view.findViewById(editId);

        editText.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, weight));

    }

    public static void setPhotoSize(long currentTimeMillis, int max) {
        String path = Environment.getExternalStorageDirectory().getPath() +
                "/Pictures/DFCarChecker/";
        String fileName = Long.toString(currentTimeMillis) + ".jpg";

        File file = new File(path + fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(path + fileName);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap newBitmap = null;

        float ratio;
        float newWidth;
        float newHeight;

        // 如果宽度小于800, 无视
        if(width > max) {
            ratio = (float)width / (float)max;
            newWidth = max;
            newHeight = height / ratio;
        } else if(height > max) {
            ratio = (float)height / (float)max;
            newWidth = width / ratio;
            newHeight = max;
        } else {
            newWidth = width;
            newHeight = height;
        }

        newBitmap = Bitmap.createScaledBitmap(bitmap, (int)newWidth, (int)newHeight, true);

        try {
            FileOutputStream ostream = new FileOutputStream(file);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, ostream);

            ostream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

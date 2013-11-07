package com.df.dfcarchecker;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;

import com.df.entry.PosEntity;
import com.df.paintview.ExteriorPaintPreviewView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.service.Helper.setTextView;

public class CarReportExteriorActivity extends Activity {

    private ExteriorPaintPreviewView exteriorPaintPreviewView;
    private List<PosEntity> posEntities;
    private EditText commentEdit;
    private JSONObject photos;
    private JSONObject conditions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_report_exterior);

        posEntities = new ArrayList<PosEntity>();

        Bundle extras = getIntent().getExtras();

        String jsonData = extras.getString("JSONData");
        parsJsonData(jsonData);
        updateUi();

        // 备注
        commentEdit = (EditText) findViewById(R.id.out_comment_edit);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car_report_exterior, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap getBitmapFromFigure(int figure) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String path = Environment.getExternalStorageDirectory().toString();
        path += "/.cheyipai/";

        // 默认为三厢四门图
        String name = "r3d4";

        switch (figure) {
            case 2:
                name = "r3d2";
                break;
            case 3:
                name = "r2d2";
                break;
            case 4:
                name = "r2d4";
                break;
            case 5:
                name = "van_o";
                break;
        }

        return BitmapFactory.decodeFile(path + name, options);
    }

    private void parsJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            photos = jsonObject.getJSONObject("photos");
            conditions = jsonObject.getJSONObject("conditions");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateUi() {
        try {
            JSONObject exterior = conditions.getJSONObject("exterior");

            setTextView(getWindow().getDecorView(), R.id.comment_text, exterior.getString("comment"));
            setTextView(getWindow().getDecorView(), R.id.smooth_text, exterior.getString("smooth"));

            JSONArray jsonArray = exterior.getJSONArray("fault");

            

            // 点击图片进入绘制界面

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            String sdcardPath = Environment.getExternalStorageDirectory().getPath();
            Bitmap previewViewBitmap = BitmapFactory.decodeFile(sdcardPath + "/.cheyipai/r3d4", options);

            exteriorPaintPreviewView = (ExteriorPaintPreviewView) findViewById(R.id.out_base_image_preview);
            exteriorPaintPreviewView.init(previewViewBitmap, posEntities);
        } catch (JSONException e) {

        }

    }
}

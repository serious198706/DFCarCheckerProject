package com.df.dfcarchecker.CarReport;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import com.df.dfcarchecker.R;
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

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            JSONObject exterior = photos.getJSONObject("exterior");

            String comment = conditions.getJSONObject("exterior").getString("comment");
            setTextView(getWindow().getDecorView(), R.id.comment_text, comment);

            String smooth = conditions.getJSONObject("exterior").getString("smooth");
            setTextView(getWindow().getDecorView(), R.id.smooth_text, smooth);

            JSONArray fault = exterior.getJSONArray("fault");

            for(int i = 0; i < fault.length(); i++) {
                JSONObject jsonObject = fault.getJSONObject(i);

                PosEntity posEntity = new PosEntity(jsonObject.getInt("type"));
                posEntity.setStart(jsonObject.getInt("startX"), jsonObject.getInt("startY"));
                posEntity.setEnd(jsonObject.getInt("endX"), jsonObject.getInt("endY"));

                posEntities.add(posEntity);
            }

            // 初始化预览界面
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


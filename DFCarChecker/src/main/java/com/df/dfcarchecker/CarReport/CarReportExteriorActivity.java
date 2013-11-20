package com.df.dfcarchecker.CarReport;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.df.dfcarchecker.R;
import com.df.entry.PosEntity;
import com.df.paintview.ExteriorPaintPreviewView;
import com.df.service.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.df.service.Helper.setTextView;

public class CarReportExteriorActivity extends Activity {

    private ExteriorPaintPreviewView exteriorPaintPreviewView;
    private List<PosEntity> posEntities;
    private JSONObject photos;
    private JSONObject conditions;
    private Dialog mPictureDialog;
    private View view;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_car_report_exterior);

        posEntities = new ArrayList<PosEntity>();

        // 初始化预览界面
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        Bitmap previewViewBitmap = BitmapFactory.decodeFile(sdcardPath + "/.cheyipai/r3d4", options);

        exteriorPaintPreviewView = (ExteriorPaintPreviewView) findViewById(R.id.out_base_image_preview);
        exteriorPaintPreviewView.init(previewViewBitmap, new ArrayList<PosEntity>());
        exteriorPaintPreviewView.setAlpha(0.4f);
        exteriorPaintPreviewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();

                        for(int i = 0; i < posEntities.size(); i++) {
                            if(inArea(new Point((int)x, (int)y),
                                    posEntities.get(i).getStartX(),
                                    posEntities.get(i).getStartY(),
                                    posEntities.get(i).getEndX(),
                                    posEntities.get(i).getEndY())) {

                                if(posEntities.get(i).getImageFileName().equals(""))
                                    Toast.makeText(CarReportExteriorActivity.this, "该缺陷点没有照片！",
                                            Toast.LENGTH_SHORT).show();
                                else
                                    loadPhoto(Common.PICTURE_ADDRESS + posEntities.get(i)
                                            .getImageFileName(), 0, 0);
                            }
                        }

                        break;
                }

                return true;
            }
        });

        Bundle extras = getIntent().getExtras();

        String jsonData = extras.getString("JSONData");
        parsJsonData(jsonData);
        updateUi();

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        view = getLayoutInflater().inflate(R.layout.picture_popup,
                (ViewGroup) findViewById(R.id.layout_root));

        image = (ImageView) view.findViewById(R.id.fullimage);

        mPictureDialog = new Dialog(CarReportExteriorActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        mPictureDialog.setContentView(view);
        mPictureDialog.setCancelable(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean inArea(Point touch, int startX, int startY, int endX, int endY) {
        if(endX == 0 && endY == 0) {
            endX = startX;
            endY = startY;
        }

        Point areaStartP = new Point(startX - 40, startY - 40);
        Point areaEndP = new Point(endX + 60, endY + 60);

        if(touch.x >= areaStartP.x
                && touch.x <= areaEndP.x
                && touch.y >= areaStartP.y
                && touch.y <= areaEndP.y)
            return true;
        else
            return false;
    }

    private void loadPhoto(String url, int width, int height) {
        mPictureDialog.show();
        GetBitmapTask getBitmapTask = new GetBitmapTask();
        getBitmapTask.execute(url);
    }

    private class GetBitmapTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap tempBitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                tempBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return tempBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            image.setImageBitmap(result);
        }
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
                // 要设置max，否则在使用endx endy时会返回零
                posEntity.setMaxX(jsonObject.getInt("endX"));
                posEntity.setMaxY(jsonObject.getInt("endY"));
                posEntity.setImageFileName(jsonObject.getString("photo"));

                posEntities.add(posEntity);
            }

            JSONObject sketch = exterior.getJSONObject("sketch");

            if(sketch != null) {
                String sketchUrl = sketch.getString("photo");
                new DownloadImageTask().execute(Common.PICTURE_ADDRESS + sketchUrl);
            }

        } catch (JSONException e) {

        }
    }

    // 下载图片
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap tempBitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                tempBitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return tempBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            setProgressBarIndeterminateVisibility(Boolean.FALSE);

            exteriorPaintPreviewView.init(result, new ArrayList<PosEntity>());
            exteriorPaintPreviewView.setAlpha(1.0f);
            exteriorPaintPreviewView.invalidate();
        }
    }
}


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
import com.df.paintview.InteriorPaintPreviewView;
import com.df.service.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.df.service.Helper.setTextView;

public class CarReportInteriorActivity extends Activity {

    private InteriorPaintPreviewView interiorPaintPreviewView;
    private List<PosEntity> posEntities;
    private JSONObject conditions;
    private JSONObject photos;
    private Dialog mPictureDialog;
    private View view;
    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_car_report_interior);

        posEntities = new ArrayList<PosEntity>();

        // 初始化预览界面
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        Bitmap previewViewBitmap = BitmapFactory.decodeFile(sdcardPath + "/.cheyipai/d4s4", options);

        interiorPaintPreviewView = (InteriorPaintPreviewView) findViewById(R.id.in_base_image_preview);
        interiorPaintPreviewView.init(previewViewBitmap, new ArrayList<PosEntity>());
        interiorPaintPreviewView.setAlpha(0.4f);
        interiorPaintPreviewView.setOnTouchListener(new View.OnTouchListener() {
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
                                    Toast.makeText(CarReportInteriorActivity.this, "该缺陷点没有照片！",
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

        mPictureDialog = new Dialog(CarReportInteriorActivity.this,
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
            JSONObject interior = photos.getJSONObject("interior");

            String comment = conditions.getJSONObject("interior").getString("comment");
            setTextView(getWindow().getDecorView(), R.id.comment_text, comment);

            String sealingStrip = conditions.getJSONObject("interior").getString("sealingStrip");
            setTextView(getWindow().getDecorView(), R.id.sealingStrip_text, sealingStrip);

            if(!interior.isNull("fault")) {
                JSONArray fault = interior.getJSONArray("fault");

                for(int i = 0; i < fault.length(); i++) {
                    JSONObject jsonObject = fault.getJSONObject(i);

                    PosEntity posEntity = new PosEntity(jsonObject.getInt("type"));
                    posEntity.setStart(jsonObject.getInt("startX"), jsonObject.getInt("startY"));
                    posEntity.setEnd(jsonObject.getInt("endX"), jsonObject.getInt("endY"));
                    posEntity.setMaxX(jsonObject.getInt("endX"));
                    posEntity.setMaxY(jsonObject.getInt("endY"));
                    posEntity.setImageFileName(jsonObject.getString("photo"));
                    posEntities.add(posEntity);
                }

                JSONObject sketch = interior.getJSONObject("sketch");

                if(sketch != null) {
                    String sketchUrl = sketch.getString("photo");
                    new DownloadImageTask().execute(Common.PICTURE_ADDRESS + sketchUrl);
                }
            } else {
                interiorPaintPreviewView.setAlpha(1.0f);
                interiorPaintPreviewView.invalidate();
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

            interiorPaintPreviewView.init(result, new ArrayList<PosEntity>());
            interiorPaintPreviewView.setAlpha(1.0f);
            interiorPaintPreviewView.invalidate();
        }
    }
}


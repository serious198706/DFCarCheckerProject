package com.df.dfcarchecker.CarReport;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.df.dfcarchecker.R;
import com.df.entry.PosEntity;
import com.df.paintview.FramePaintPreviewView;
import com.df.service.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import static com.df.service.Helper.setTextView;

public class CarReportFrameFragment extends Fragment {
    private static View rootView;
    private LayoutInflater inflater;

    private String jsonData;
    private ArrayList<PosEntity> posEntitiesFront;
    private ArrayList<PosEntity> posEntitiesRear;
    private Bitmap previewBitmapFront;
    private FramePaintPreviewView framePaintPreviewViewFront;
    private Bitmap previewBitmapRear;
    private FramePaintPreviewView framePaintPreviewViewRear;
    private JSONObject frames;

    private Bitmap tempBitmap;

    private Dialog mPictureDialog;
    private ImageView image;
    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_report_frame, container, false);

        posEntitiesFront = new ArrayList<PosEntity>();
        posEntitiesRear = new ArrayList<PosEntity>();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String sdcardPath = Environment.getExternalStorageDirectory().toString();
        previewBitmapFront = BitmapFactory.decodeFile(sdcardPath + "/.cheyipai/d4_f", options);
        framePaintPreviewViewFront = (FramePaintPreviewView)rootView.findViewById(R.id.structure_base_image_preview_front);
        framePaintPreviewViewFront.init(previewBitmapFront, new ArrayList<PosEntity>());
        framePaintPreviewViewFront.setAlpha(0.4f);
        framePaintPreviewViewFront.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();

                        for(int i = 0; i < posEntitiesFront.size(); i++) {
                            if(inArea(new Point((int)x, (int)y),
                                    posEntitiesFront.get(i).getStartX(),
                                    posEntitiesFront.get(i).getStartY(),
                                    posEntitiesFront.get(i).getStartX(),
                                    posEntitiesFront.get(i).getStartY())) {

                                if(posEntitiesFront.get(i).getImageFileName().equals(""))
                                    Toast.makeText(rootView.getContext(), "该缺陷点没有照片！",
                                            Toast.LENGTH_SHORT).show();
                                else
                                    loadPhoto(Common.PICTURE_ADDRESS + posEntitiesFront.get(i)
                                            .getImageFileName(), 0, 0);
                            }
                        }

                        break;
                }

                return true;
            }
        });

        previewBitmapRear = BitmapFactory.decodeFile(sdcardPath + "/.cheyipai/d4_r", options);
        framePaintPreviewViewRear = (FramePaintPreviewView)rootView.findViewById(R.id.structure_base_image_preview_rear);
        framePaintPreviewViewRear.init(previewBitmapRear, new ArrayList<PosEntity>());
        framePaintPreviewViewRear.setAlpha(0.4f);
        framePaintPreviewViewRear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        float x = motionEvent.getX();
                        float y = motionEvent.getY();

                        for(int i = 0; i < posEntitiesRear.size(); i++) {
                            if(inArea(new Point((int)x, (int)y),
                                    posEntitiesRear.get(i).getStartX(),
                                    posEntitiesRear.get(i).getStartY(),
                                    posEntitiesRear.get(i).getStartX(),
                                    posEntitiesRear.get(i).getStartY())) {

                                if(posEntitiesRear.get(i).getImageFileName().equals(""))
                                    Toast.makeText(rootView.getContext(), "该缺陷点没有照片！",
                                            Toast.LENGTH_SHORT).show();
                                else
                                    loadPhoto(Common.PICTURE_ADDRESS + posEntitiesRear.get(i)
                                            .getImageFileName(), 0, 0);
                            }
                        }

                        break;
                }

                return true;
            }
        });

        parsJsonData();
        updateUi();

        view = CarReportFrameFragment.this.inflater.inflate(R.layout.picture_popup,
                (ViewGroup) rootView.findViewById(R.id.layout_root));

        image = (ImageView) view.findViewById(R.id.fullimage);

        mPictureDialog = new Dialog(rootView.getContext(),
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        mPictureDialog.setContentView(view);
        mPictureDialog.setCancelable(true);

        return rootView;
    }

    private boolean inArea(Point touch, int startX, int startY, int endX, int endY) {
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

    public CarReportFrameFragment(String jsonData) {
        this.jsonData = jsonData;
    }

    private void parsJsonData() {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(jsonData);

            // 结构检查结果
            frames = jsonObject.getJSONObject("frames");

            // 结构检查照片与缺陷
            JSONObject photos = jsonObject.getJSONObject("photos");
            JSONObject frame = photos.getJSONObject("frame");

            // 结构缺陷位置 - 前视角

            if(!frame.isNull("front")) {
                JSONArray frontPosArray = frame.getJSONArray("front");

                for(int i = 0; i < frontPosArray.length(); i++) {
                    JSONObject temp = frontPosArray.getJSONObject(i);

                    PosEntity posEntity = new PosEntity(Common.COLOR_DIFF);
                    posEntity.setStart(temp.getInt("x"), temp.getInt("y"));
                    posEntity.setEnd(0, 0);
                    posEntity.setImageFileName(temp.getString("photo"));
                    posEntitiesFront.add(posEntity);
                }
            } else {
                framePaintPreviewViewFront.setAlpha(1.0f);
                framePaintPreviewViewFront.invalidate();
            }

            // 结构缺陷位置 - 后视角
            if(!frame.isNull("rear")) {
                JSONArray rearPosArray = frame.getJSONArray("rear");

                for(int i = 0; i < rearPosArray.length(); i++) {
                    JSONObject temp = rearPosArray.getJSONObject(i);

                    PosEntity posEntity = new PosEntity(Common.COLOR_DIFF);
                    posEntity.setStart(temp.getInt("x"), temp.getInt("y"));
                    posEntity.setEnd(0, 0);
                    posEntity.setImageFileName(temp.getString("photo"));
                    posEntitiesRear.add(posEntity);
                }
            } else {
                framePaintPreviewViewRear.setAlpha(1.0f);
                framePaintPreviewViewRear.invalidate();
            }


            // 结构草图 - 前视角
            JSONObject fSketch = frame.getJSONObject("fSketch");

            if(fSketch != null) {
                String fSketchUrl = fSketch.getString("photo");
                new DownloadImageTask("front").execute(Common.PICTURE_ADDRESS + fSketchUrl);
            }

            // 结构草图 - 后视角
            JSONObject rSketch = frame.getJSONObject("rSketch");

            if(fSketch != null) {
                String rSketchUrl = rSketch.getString("photo");
                new DownloadImageTask("rear").execute(Common.PICTURE_ADDRESS + rSketchUrl);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUi() {
        try {
            setTextView(rootView, R.id.comment_text, frames.getString("comment"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 下载图片
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private String sight;

        public DownloadImageTask(String sight) {
            this.sight = sight;
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
            if(result == null)
                return;
            if(sight.equals("front")) {
                framePaintPreviewViewFront.init(result, new ArrayList<PosEntity>());
                framePaintPreviewViewFront.setAlpha(1.0f);
                framePaintPreviewViewFront.invalidate();
            } else {
                framePaintPreviewViewRear.init(result, new ArrayList<PosEntity>());
                framePaintPreviewViewRear.setAlpha(1.0f);
                framePaintPreviewViewRear.invalidate();
            }
        }
    }
}
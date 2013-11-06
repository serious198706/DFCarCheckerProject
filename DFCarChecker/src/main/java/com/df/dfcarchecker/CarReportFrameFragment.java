package com.df.dfcarchecker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.df.entry.PosEntity;
import com.df.paintview.FramePaintPreviewView;
import com.df.service.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);

        previewBitmapRear = BitmapFactory.decodeFile(sdcardPath + "/.cheyipai/d4_r", options);
        framePaintPreviewViewRear = (FramePaintPreviewView)rootView.findViewById(R.id.structure_base_image_preview_rear);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);

        parsJsonData();
        updateUi();

        return rootView;
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
            JSONArray frontPosArray = frame.getJSONArray("front");

            for(int i = 0; i < frontPosArray.length(); i++) {
                JSONObject temp = frontPosArray.getJSONObject(i);

                PosEntity posEntity = new PosEntity(Common.COLOR_DIFF);
                posEntity.setStart(temp.getInt("x"), temp.getInt("y"));
                posEntity.setEnd(0, 0);
                // TODO: 处理一下没有图片的情况
                posEntity.setImageFileName(temp.getString("photo"));
                posEntitiesFront.add(posEntity);
            }

            // 结构缺陷位置 - 后视角
            JSONArray rearPosArray = frame.getJSONArray("rear");

            for(int i = 0; i < rearPosArray.length(); i++) {
                JSONObject temp = rearPosArray.getJSONObject(i);

                PosEntity posEntity = new PosEntity(Common.COLOR_DIFF);
                posEntity.setStart(temp.getInt("x"), temp.getInt("y"));
                posEntity.setEnd(0, 0);
                posEntity.setImageFileName(temp.getString("photo"));
                posEntitiesRear.add(posEntity);
            }

            // 结构草图 - 前视角
            JSONObject fSketch = frame.getJSONObject("fSketch");
            String fSketchUrl = fSketch.getString("photo");

            new DownloadImageTask("front").execute("http://cy198706.com/kanmeizhi/recommend/1.jpg");

            // 结构草图 - 后视角
            JSONObject rSketch = frame.getJSONObject("rSketch");
            String rSketchUrl = rSketch.getString("photo");

            new DownloadImageTask("rear").execute("http://cy198706.com/kanmeizhi/recommend/2.jpg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUi() {
        try {
            setTextView(rootView, R.id.comment_edit, frames.getString("comment"));
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
            if(sight.equals("front")) {
                framePaintPreviewViewFront.init(result, posEntitiesFront);
                framePaintPreviewViewFront.invalidate();
            } else {
                framePaintPreviewViewRear.init(result, posEntitiesRear);
                framePaintPreviewViewRear.invalidate();
            }
        }
    }
}
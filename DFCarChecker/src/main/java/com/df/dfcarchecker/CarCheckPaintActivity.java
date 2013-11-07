package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import com.df.entry.PosEntity;
import com.df.entry.PhotoEntity;
import com.df.paintview.ExteriorPaintView;
import com.df.paintview.FramePaintView;
import com.df.paintview.InteriorPaintView;
import com.df.paintview.PaintView;
import com.df.service.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CarCheckPaintActivity extends Activity {
    private ExteriorPaintView exteriorPaintView;
    private InteriorPaintView interiorPaintView;
    private FramePaintView framePaintView;
    private String currentPaintView;

    // 用来截图的View
    private View targetView;

    // 草图保存线程
    private SaveSketchImageTask mSaveSketchImageTask;

    // 保存草图
    public static List<PhotoEntity> sketchPhotoEntities;

    // 当前绘图类型
    private int currentType = 0;

    // 结构检查的视角
    private String sight;

    // 一个HashMap
    private Map<String, View> map = new HashMap<String, View>();

    // 绘图类的父类
    PaintView paintView;

    // 当用户退出时，进行的选择
    boolean choise = false;

    public enum PaintType {
        FRAME_PAINT, EX_PAINT, IN_PAINT, NOVALUE;

        public static PaintType paintType(String str)
        {
            try {
                return valueOf(str);
            }
            catch (Exception ex) {
                return NOVALUE;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentPaintView = extras.getString("PAINT_TYPE");

            switch (PaintType.paintType(currentPaintView)) {
                // 外观检查绘图
                case EX_PAINT:
                    setExPaintLayout();
                    break;
                // 内饰检查绘图
                case IN_PAINT:
                    setInPaintLayout();
                    break;
                // 结构检查绘图
                case FRAME_PAINT:
                    sight = extras.getString("PAINT_SIGHT");
                    setFramePaintLayout(sight);
                    break;
            }
        }

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 截图需要
        targetView = findViewById(R.id.titleLy);

        sketchPhotoEntities = CarCheckBasicInfoFragment.sketchPhotoEntities;

        map.put("EX_PAINT", exteriorPaintView);
        map.put("IN_PAINT", interiorPaintView);
        map.put("FRAME_PAINT", framePaintView);
    }

    private void setInPaintLayout() {
        setContentView(R.layout.activity_car_check_interior_paint);
        setTitle(R.string.in);

        // 根据CarSettings的figure设定图片
        int figure = Integer.parseInt(CarCheckBasicInfoFragment.mCarSettings.getFigure());
        Bitmap bitmap = getBitmapFromFigure(figure, "IN");

        // 初始化绘图View
        interiorPaintView = (InteriorPaintView) findViewById(R.id.tile);
        interiorPaintView.init(bitmap, CarCheckInteriorActivity.posEntities);

        // 选择当前绘图类型
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.in_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                switch (i) {
                    case R.id.in_dirty_radio:
                        currentType = Common.DIRTY;
                        break;
                    case R.id.in_broken_radio:
                        currentType = Common.BROKEN;
                        break;
                }

                interiorPaintView.setType(currentType);
            }
        });
    }

    private void setExPaintLayout() {
        setContentView(R.layout.activity_car_check_exterior_paint);
        setTitle(R.string.out);

        // 根据CarSettings的figure设定图片
        int figure = Integer.parseInt(CarCheckBasicInfoFragment.mCarSettings.getFigure());
        Bitmap bitmap = getBitmapFromFigure(figure, "EX");

        // 初始化绘图View
        exteriorPaintView = (ExteriorPaintView) findViewById(R.id.tile);
        exteriorPaintView.init(bitmap, CarCheckExteriorActivity.posEntities);

        // 选择当前绘图类型
        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.out_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                currentType = 0;

                switch (i) {
                    case R.id.out_color_diff_radio:
                        currentType = Common.COLOR_DIFF;
                        break;
                    case R.id.out_scratch_radio:
                        currentType = Common.SCRATCH;
                        break;
                    case R.id.out_trans_radio:
                        currentType = Common.TRANS;
                        break;
                    case R.id.out_scrape_radio:
                        currentType = Common.SCRAPE;
                        break;
                    case R.id.out_other_radio:
                        currentType = Common.OTHER;
                        break;
                }

                exteriorPaintView.setType(currentType);
            }
        });
    }


    private void setFramePaintLayout(String sight) {
        setContentView(R.layout.activity_car_check_frame_paint);
        setTitle(R.string.structure);

        // 初始化绘图View
        framePaintView = (FramePaintView) findViewById(R.id.tile);

        if(sight.equals("FRONT")) {
            framePaintView.init(CarCheckFrameFragment.previewBitmapFront,
                    CarCheckFrameFragment.posEntitiesFront);
        } else {
            framePaintView.init(CarCheckFrameFragment.previewBitmapRear,
                    CarCheckFrameFragment.posEntitiesRear);
        }

        // 选择当前绘图类型（结构检查只有一个）
        framePaintView.setType(Common.COLOR_DIFF);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car_check_out_side_paint, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        paintView = (PaintView)map.get(currentPaintView);

        switch (item.getItemId()) {
            case android.R.id.home:
                // 用户确认返回上一层
                alertUser(R.string.out_cancel_confirm);
                return true;
            case R.id.action_done:
                // 提交数据
                captureResultImage();
                finish();
                break;
            case R.id.action_cancel:
                // 用户确认放弃更改
                alertUser(R.string.out_cancel_confirm);
                break;
            case R.id.action_clear:
                // 用户确认清除数据
                alertUser(R.string.out_clear_confirm);

                if(choise) {
                    paintView.clear();
                }
                break;
            case R.id.action_undo:
                // 回退
                paintView.undo();
                break;
            case R.id.action_redo:
                // 重做
                paintView.redo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 截图并保存
    private void captureResultImage(){
        paintView = (PaintView)map.get(currentPaintView);

        // 如果没有缺陷点，则不要保存草图了
        if(paintView.getPosEntity() != null) {
            mSaveSketchImageTask = new SaveSketchImageTask();
            mSaveSketchImageTask.execute((Void) null);
        }
    }

    // 提醒用户
    private void alertUser(int msgId) {
        paintView = (PaintView)map.get(currentPaintView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.alert_title);
        builder.setMessage(msgId);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 退出
                paintView.cancel();
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String group = "";
        int startX, startY, endX, endY;
        int radius = 0;
        long currentTimeMillis = 0;

        // 获取绘图父类实体
        paintView = (PaintView)map.get(currentPaintView);

        switch (resultCode) {
            // 拍摄完成后，各种组织
            case Activity.RESULT_OK:
                // 获取当前文件名
                currentTimeMillis = paintView.getCurrentTimeMillis();
                break;
            case Activity.RESULT_CANCELED:
                // 获取当前文件名
                currentTimeMillis = 0;
                break;
            default:
                Log.d("DFCarChecker", "拍摄故障！！");
                break;
        }

        // 照片集合
        List<PhotoEntity> photoEntities = paintView.getPhotoEntities();

        // 获取坐标们
        PosEntity posEntity = paintView.getPosEntity();
        startX = posEntity.getStartX();
        startY = posEntity.getStartY();
        endX = posEntity.getEndX();
        endY = posEntity.getEndY();

        // 如果是“变形”，即圆
        if(currentType == 3) {
            // 计算半径
            int dx = Math.abs(endX - startX);
            int dy = Math.abs(endY- startY);
            int dr = (int)Math.sqrt(dx * dx + dy * dy);

            // 计算圆心
            int x0 = (startX + endX) / 2;
            int y0 = (startY + endY) / 2;

            startX = x0;
            startY = y0;
            endX = endY = 0;
            radius = dr;
        }

        // 如果是结构，则要特殊处理 ----- 艹，结构老是要特殊处理
        if(currentPaintView.equals("FRAME_PAINT")) {
            photoEntities = paintView.getPhotoEntities(sight);
        }

        // 组织JsonString
        JSONObject jsonObject = new JSONObject();

        try {
            JSONObject photoJsonObject = new JSONObject();

            jsonObject.put("Group", paintView.getGroup());

            // 如果是结构检查
            if(currentPaintView.equals("FRAME_PAINT")) {
                jsonObject.put("Part", sight.toLowerCase());
                photoJsonObject.put("x", startX);
                photoJsonObject.put("y", startY);
            } else {
                jsonObject.put("Part", "fault");
                photoJsonObject.put("type", paintView.getType());
                photoJsonObject.put("startX", startX);
                photoJsonObject.put("startY", startY);
                photoJsonObject.put("endX", endX);
                photoJsonObject.put("endY", endY);
                photoJsonObject.put("radius", radius);
            }

            jsonObject.put("PhotoData", photoJsonObject);
            jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
            jsonObject.put("UserId", LoginActivity.userInfo.getId());
            jsonObject.put("Key", LoginActivity.userInfo.getKey());
        } catch (Exception e) {
            Log.d("DFCarChecker", "Json组织错误：" + e.getMessage());
        }

        // 如果文件名为0，则表示此点无照片
        String fileName = (currentTimeMillis == 0 ? "" : Long.toString(currentTimeMillis) + "" +
                ".jpg");

        // 组建PhotoEntity
        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName(fileName);
        photoEntity.setJsonString(jsonObject.toString());

        // 暂时不加入照片池，只放入各自的List，等保存时再提交
        photoEntities.add(photoEntity);
    }

    // 保存草图的线程
    public class SaveSketchImageTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean success = false;

            Bitmap b = Bitmap.createBitmap(targetView.getWidth(),targetView.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            targetView.draw(c);

            String path = Environment.getExternalStorageDirectory().getPath();
            path += "/Pictures/DFCarChecker/";

            File file = new File(path);
            file.mkdirs();// 创建文件夹

            String filename = "";
            String group = "";

            switch (PaintType.paintType(currentPaintView)) {
                case EX_PAINT:
                    filename = "sketch_o";
                    group = "exterior";
                    break;
                case IN_PAINT:
                    filename = "sketch_i";
                    group = "interior";
                    break;
                case FRAME_PAINT:
                    if(sight.equals("FRONT"))
                        filename = "sketch_sf";
                    else
                        filename = "sketch_sr";
                    group = "frame";
                    break;
            }

            try {
                FileOutputStream out = new FileOutputStream(path + filename);
                b.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 组织jsonString
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("Group", group);

                // 如果是结构检查
                if(currentPaintView.equals("FRAME_PAINT")) {
                    if(sight.equals("FRONT"))
                        jsonObject.put("Part", "fSketch");
                    else
                        jsonObject.put("Part", "rSketch");
                }
                // 其他类型的检查
                else {
                    jsonObject.put("Part", "sketch");
                }

                JSONObject photoData = new JSONObject();
                photoData.put("height", targetView.getHeight());
                photoData.put("width", targetView.getWidth());

                jsonObject.put("PhotoData", photoData);
                jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
                jsonObject.put("UserId", LoginActivity.userInfo.getId());
                jsonObject.put("Key", LoginActivity.userInfo.getKey());
            } catch (JSONException e) {

            }

            PhotoEntity photoEntity = new PhotoEntity();
            photoEntity.setFileName(filename);
            photoEntity.setJsonString(jsonObject.toString());

            // TODO: 先保存，等最后提交时再上传
            // 此图只传一次，所以如果已存在，则不必再保存，只更新图片即可
            if(!sketchPhotoEntities.contains(photoEntity)) {
                sketchPhotoEntities.add(photoEntity);
            }

            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveSketchImageTask = null;
        }

        @Override
        protected void onCancelled() {
            mSaveSketchImageTask = null;
        }
    }

    // 根据不同的检测步骤和figure，返回不同的图片
    private Bitmap getBitmapFromFigure(int figure, String step) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String path = Environment.getExternalStorageDirectory().toString();
        path += "/.cheyipai/";
        String name = "";

        if(step.equals("EX")) {
            // 外观图
            switch (figure) {
                case 0:
                case 1:
                    name = "r3d4";
                    break;
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
        } else {
            // 内饰图
            switch (figure) {
                case 0:
                case 1:
                    name = "d4s4";
                    break;
                case 2:
                    name = "d2s4";
                    break;
                case 3:
                    name = "d2s4";
                    break;
                case 4:
                    name = "d4s4";
                    break;
                case 5:
                    name = "van_i";
                    break;
            }
        }

        return BitmapFactory.decodeFile(path + name, options);
    }

    @Override
    public void onBackPressed() {
        alertUser(R.string.out_cancel_confirm);
    }

}

package com.df.dfcarchecker.CarCheck;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.df.dfcarchecker.MainActivity;
import com.df.dfcarchecker.PopupActivity;
import com.df.dfcarchecker.R;
import com.df.entry.PosEntity;
import com.df.entry.PhotoEntity;
import com.df.paintview.ExteriorPaintPreviewView;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.ImageUploadQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.df.service.Helper.setEditText;
import static com.df.service.Helper.setSpinnerSelectionWithString;

public class CarCheckExteriorActivity extends Activity implements View.OnClickListener {
    private int currentShotPart;
    private EditText brokenEdit;
    private static Spinner smoothSpinner;
    private static EditText commentEdit;
    public static List<PosEntity> posEntities = CarCheckIntegratedFragment.exteriorPosEntities;
    public static List<PhotoEntity> photoEntities = CarCheckIntegratedFragment.exteriorPhotoEntities;
    private ExteriorPaintPreviewView exteriorPaintPreviewView;
    private TextView tip;
    private String brokenParts;

    private ImageUploadQueue imageUploadQueue;
    private long currentTimeMillis;
    private int[] photoShotCount = {0, 0, 0, 0, 0, 0, 0};

    private String jsonData = "";
    private JSONObject photos;
    private JSONObject conditions;

    private boolean saved;

    Bitmap previewViewBitmap;
    int figure;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check_exterior);

        // 点击图片进入绘制界面
        figure = Integer.parseInt(CarCheckBasicInfoFragment.mCarSettings.getFigure());
        previewViewBitmap = getBitmapFromFigure(figure);

        exteriorPaintPreviewView = (ExteriorPaintPreviewView) findViewById(R.id.out_base_image_preview);
        exteriorPaintPreviewView.init(previewViewBitmap, posEntities);
        exteriorPaintPreviewView.setOnClickListener(this);

        // 选择表面有破损的零部件
        Button brokenButton = (Button) findViewById(R.id.out_choose_broken_button);
        brokenButton.setOnClickListener(this);
        brokenEdit = (EditText) findViewById(R.id.out_broken_edit);

        // 拍摄外观组照片
        Button cameraButton = (Button) findViewById(R.id.out_start_camera_button);
        cameraButton.setOnClickListener(this);

        // 图片上的提示
        tip = (TextView)findViewById(R.id.tipOnPreview);

        // 车辆漆面光洁度
        smoothSpinner = (Spinner) findViewById(R.id.out_smooth_spinner);

        // 备注
        commentEdit = (EditText) findViewById(R.id.out_comment_edit);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String paintIndex = extras.getString("INDEX");
            if(paintIndex != null) {
                smoothSpinner.setSelection(Integer.parseInt(paintIndex));
            }

            String comment = extras.getString("COMMENT");
            if(comment != null) {
                commentEdit.setText(comment);
            }

            photoShotCount = extras.getIntArray("PHOTO_COUNT");

            if(extras.containsKey("JSONDATA")) {
                jsonData = extras.getString("JSONDATA");
            }

            if(extras.containsKey("SAVED")) {
                saved = extras.getBoolean("SAVED");

                if(saved)
                {
                    exteriorPaintPreviewView.setOnClickListener(null);
                }
            }
        }

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(!posEntities.isEmpty()) {
            exteriorPaintPreviewView.setAlpha(1f);
            exteriorPaintPreviewView.invalidate();
            tip.setVisibility(View.GONE);
        }

        imageUploadQueue = ImageUploadQueue.getInstance();

        if(!jsonData.equals("")) {
            letsEnterModifyMode();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save_discard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                // 保存数据
                saveResult();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void discardResult() {
        // 创建结果意图和包括地址
        Intent intent = new Intent();
        intent.putExtra("PHOTO_COUNT", photoShotCount);

        // 关闭activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.out_choose_broken_button:
                ChooseBroken();
                break;
            case R.id.out_start_camera_button:
                out_start_camera();
                break;
            case R.id.out_base_image_preview:
                StartPaint();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        saveResult();
    }


    public void ChooseBroken() {
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("POPUP_TYPE", "OUT_BROKEN");
        intent.putExtra("BROKEN_PARTS", brokenParts);
        startActivityForResult(intent, Common.CHOOSE_OUT_BROKEN);
    }

    public void out_start_camera() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String[] itemArray = getResources().getStringArray(R.array
                .out_camera_cato_item);

        for(int i = 0; i < itemArray.length; i++) {
            itemArray[i] += " (";
            itemArray[i] += Integer.toString(photoShotCount[i]);
            itemArray[i] += ") ";
        }

        builder.setTitle(R.string.out_camera);
        builder.setItems(itemArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentShotPart = i;

                String group = getResources().getStringArray(R.array.out_camera_cato_item)[currentShotPart];

                Toast.makeText(CarCheckExteriorActivity.this, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                currentTimeMillis = System.currentTimeMillis();
                Uri fileUri = Helper.getOutputMediaFileUri(currentTimeMillis); // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                startActivityForResult(intent, Common.PHOTO_FOR_OUTSIDE_GROUP);
            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void StartPaint() {
        Intent intent = new Intent(this, CarCheckPaintActivity.class);
        intent.putExtra("PAINT_TYPE", "EX_PAINT");
        startActivityForResult(intent, Common.EX_PAINT);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Common.CHOOSE_OUT_BROKEN:
                // 查找成功
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String brokenPart = bundle.getString(Common.OUT_BROKEN_RESULT);
                            if(brokenPart != null) {
                                brokenEdit.setText(brokenPart);
                            }

                            // 记录从选择破损部件页面返回的序号
                            String brokenParts = bundle.getString("BROKEN_PARTS");
                            this.brokenParts = brokenParts;
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
            case Common.PHOTO_FOR_OUTSIDE_GROUP:
                if(resultCode == Activity.RESULT_OK) {
                    // 组织JsonString
                    JSONObject jsonObject = new JSONObject();

                    try {
                        JSONObject photoJsonObject = new JSONObject();
                        String currentPart = "";

                        switch (currentShotPart) {
                            case 0:
                                currentPart = "leftFront45";
                                break;
                            case 1:
                                currentPart = "rightFront45";
                                break;
                            case 2:
                                currentPart = "left";
                                break;
                            case 3:
                                currentPart = "right";
                                break;
                            case 4:
                                currentPart = "leftRear45";
                                break;
                            case 5:
                                currentPart = "rightRear45";
                                break;
                            case 6:
                                currentPart = "other";
                                break;
                        }

                        photoJsonObject.put("part", currentPart);

                        jsonObject.put("Group", "exterior");
                        jsonObject.put("Part", "standard");
                        jsonObject.put("PhotoData", photoJsonObject);
                        jsonObject.put("UserId", MainActivity.userInfo.getId());
                        jsonObject.put("Key", MainActivity.userInfo.getKey());
                        jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
                    } catch (JSONException e) {

                    }

                    PhotoEntity photoEntity = new PhotoEntity();
                    photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
                    photoEntity.setJsonString(jsonObject.toString());

                    // 立刻上传
                    imageUploadQueue.addImage(photoEntity);

                    photoShotCount[currentShotPart]++;

                    out_start_camera();
                } else {
                    Toast.makeText(CarCheckExteriorActivity.this,
                            "相机打开错误", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case Common.EX_PAINT:
                // 如果有点，则将图片设为不透明，去掉提示文字
                if(!posEntities.isEmpty()) {
                    exteriorPaintPreviewView.setAlpha(1f);
                    exteriorPaintPreviewView.invalidate();
                    tip.setVisibility(View.GONE);
                }
                // 如果没点，则将图片设为半透明，添加提示文字
                else {
                    exteriorPaintPreviewView.setAlpha(0.3f);
                    exteriorPaintPreviewView.invalidate();
                    tip.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void saveResult() {
        // 如果还未保存，则提示用户
        if(!saved) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.alert_title)
                    .setMessage("保存后将无法进行缺陷点修改，确定保存？")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 创建结果意图和包括地址
                            Intent intent = new Intent();
                            intent.putExtra("INDEX", Integer.toString(smoothSpinner.getSelectedItemPosition()));
                            intent.putExtra("COMMENT", commentEdit.getText().toString());
                            intent.putExtra("PHOTO_COUNT", photoShotCount);
                            saved = true;
                            intent.putExtra("SAVED", saved);
                            addPhotosToQueue();

                            // 关闭activity
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).create();

            dialog.show();
        } else {
            // 创建结果意图和包括地址
            Intent intent = new Intent();
            intent.putExtra("INDEX", Integer.toString(smoothSpinner.getSelectedItemPosition()));
            intent.putExtra("COMMENT", commentEdit.getText().toString());
            intent.putExtra("PHOTO_COUNT", photoShotCount);
            saved = true;
            intent.putExtra("SAVED", saved);
            //addPhotosToQueue();

            // 关闭activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    // 只有在保存时才提交照片
    private void addPhotosToQueue() {
        ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

        // 如果草图队列为空
        if(CarCheckPaintActivity.sketchPhotoEntities == null) {
            CarCheckPaintActivity.sketchPhotoEntities = new ArrayList<PhotoEntity>();

            PhotoEntity photoEntity = getSketchPhotoEntity();
            CarCheckPaintActivity.sketchPhotoEntities.add(photoEntity);
        } else {
            // 如果有缺陷点，表示外观草图已经生成，则不需要再添加了
            if(posEntities.isEmpty()) {
                PhotoEntity photoEntity = getSketchPhotoEntity();
                CarCheckPaintActivity.sketchPhotoEntities.add(photoEntity);
            }
        }

        // 将草图队列里所有的草图全部放入照片池
        for(int i = 0; i < CarCheckPaintActivity.sketchPhotoEntities.size(); i++) {
            imageUploadQueue.addImage(CarCheckPaintActivity.sketchPhotoEntities.get(i));
        }

        while(!CarCheckPaintActivity.sketchPhotoEntities.isEmpty()) {
            CarCheckPaintActivity.sketchPhotoEntities.remove(0);
        }


        for(int i = 0; i < photoEntities.size(); i++) {
            imageUploadQueue.addImage(photoEntities.get(i));
        }

        // 加入照片池后，将本身的photoEntities删除，以免重复上传
        while(!photoEntities.isEmpty()) {
            photoEntities.remove(0);
        }
    }

    private PhotoEntity getSketchPhotoEntity() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" +
                ".cheyipai/" + getNameFromFigure(figure));
        File dst = new File(Environment.getExternalStorageDirectory().getPath() +
                "/Pictures/DFCarChecker/" + "sketch_o");

        try {
            copy(file, dst);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        // 组织jsonString
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("Group", "exterior");
            jsonObject.put("Part", "sketch");

            JSONObject photoData = new JSONObject();
            photoData.put("height", bitmap.getHeight());
            photoData.put("width", bitmap.getWidth());

            jsonObject.put("PhotoData", photoData);
            jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
        } catch (JSONException e) {

        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("sketch_o");
        photoEntity.setJsonString(jsonObject.toString());

        return photoEntity;
    }

    private Bitmap getBitmapFromFigure(int figure) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(getBitmapNameFromFigure(figure), options);
    }

    private String getBitmapNameFromFigure(int figure) {
        String path = Environment.getExternalStorageDirectory().toString();
        path += "/.cheyipai/";

        return path + getNameFromFigure(figure);
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private String getNameFromFigure(int figure) {
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

        return name;
    }

    public static JSONObject generateExteriorJsonObject() {
        JSONObject exterior = new JSONObject();

        try {
            exterior.put("smooth", smoothSpinner.getSelectedItem().toString());
            exterior.put("comment", commentEdit.getText().toString());
        } catch (Exception e) {
            return null;
        }

        return exterior;
    }


    private void letsEnterModifyMode() {
        TableLayout cameraArea = (TableLayout) findViewById(R.id.cameraArea);
        cameraArea.setVisibility(View.GONE);

        exteriorPaintPreviewView.setOnClickListener(null);
        tip.setOnClickListener(null);
        parsJsonData();
        updateUi();
    }

    private void parsJsonData() {
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
            setEditText(getWindow().getDecorView(), R.id.out_comment_edit, comment);

            String smooth = conditions.getJSONObject("exterior").getString("smooth");
            setSpinnerSelectionWithString(getWindow().getDecorView(), R.id.out_smooth_spinner, smooth);

//            JSONArray fault = exterior.getJSONArray("fault");
//
//            for(int i = 0; i < fault.length(); i++) {
//                JSONObject jsonObject = fault.getJSONObject(i);
//
//                PosEntity posEntity = new PosEntity(jsonObject.getInt("type"));
//                posEntity.setStart(jsonObject.getInt("startX"), jsonObject.getInt("startY"));
//                posEntity.setEnd(jsonObject.getInt("endX"), jsonObject.getInt("endY"));
//
//                posEntities.add(posEntity);
//            }

            JSONObject sketch = exterior.getJSONObject("sketch");
            String sketchUrl = sketch.getString("photo");

            new DownloadImageTask().execute(Common.PICUTRE_ADDRESS + sketchUrl);
        } catch (JSONException e) {

        }
    }

    // 下载图片
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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
            if(result == null) {
                Toast.makeText(CarCheckExteriorActivity.this, "下载图片失败",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            exteriorPaintPreviewView.init(result, posEntities);
            exteriorPaintPreviewView.invalidate();
            exteriorPaintPreviewView.setAlpha(1.0f);
            tip.setVisibility(View.GONE);
        }
    }
}

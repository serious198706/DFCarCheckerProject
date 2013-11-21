package com.df.dfcarchecker.CarCheck;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.df.dfcarchecker.MainActivity;
import com.df.dfcarchecker.R;
import com.df.entry.PosEntity;
import com.df.entry.PhotoEntity;
import com.df.paintview.InteriorPaintPreviewView;
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
import java.util.List;

import static com.df.service.Helper.setEditText;
import static com.df.service.Helper.setPhotoSize;
import static com.df.service.Helper.setSpinnerSelectionWithString;
import static com.df.service.Helper.showView;

public class CarCheckInteriorActivity extends Activity implements View.OnClickListener  {
    private int currentShotPart;

    public static List<PosEntity> posEntities = CarCheckIntegratedFragment.interiorPosEntities;
    public static List<PhotoEntity> photoEntities = CarCheckIntegratedFragment.exteriorPhotoEntities;

    private InteriorPaintPreviewView interiorPaintPreviewView;
    private TextView tip;

    private static Spinner sealSpinner;
    private static EditText commentEdit;
    private long currentTimeMillis;
    private ImageUploadQueue imageUploadQueue;

    private int[] photoShotCount = {0, 0, 0, 0, 0, 0, 0};
    private JSONObject photos;
    private JSONObject conditions;

    private String jsonData = "";
    static int figure;
    private boolean saved;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check_interior);

        Button cameraButton = (Button) findViewById(R.id.in_start_camera_button);
        cameraButton.setOnClickListener(this);

        // 点击图片进入绘制界面
        figure = Integer.parseInt(CarCheckBasicInfoFragment.mCarSettings.getFigure());
        Bitmap previewViewBitmap = getBitmapFromFigure(figure);

        interiorPaintPreviewView = (InteriorPaintPreviewView) findViewById(R.id.in_base_image_preview);
        interiorPaintPreviewView.init(previewViewBitmap, posEntities);
        interiorPaintPreviewView.setOnClickListener(this);

        tip = (TextView) findViewById(R.id.tipOnPreview);
        tip.setOnClickListener(this);

        sealSpinner = (Spinner) findViewById(R.id.in_sealingStrip_spinner);
        commentEdit = (EditText) findViewById(R.id.in_comment_edit);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String index = extras.getString("INDEX");
            if(index != null) {
                sealSpinner.setSelection(Integer.parseInt(index));
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
                    interiorPaintPreviewView.setOnClickListener(null);
                }
            }
        }

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(!posEntities.isEmpty()) {
            interiorPaintPreviewView.setAlpha(1f);
            interiorPaintPreviewView.invalidate();
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

    @Override
    public void onBackPressed() {
        saveResult();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.in_start_camera_button:
                in_start_camera();
                break;
            case R.id.in_base_image_preview:
            case R.id.tipOnPreview:
                StartPaint();
                break;

        }
    }

    public void StartPaint() {
        Intent intent = new Intent(this, CarCheckPaintActivity.class);
        intent.putExtra("PAINT_TYPE", "IN_PAINT");
        startActivityForResult(intent, Common.IN_PAINT);
    }

    public void in_start_camera() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String[] itemArray = getResources().getStringArray(R.array
                .in_camera_cato_item);

        for(int i = 0; i < itemArray.length; i++) {
            itemArray[i] += " (";
            itemArray[i] += Integer.toString(photoShotCount[i]);
            itemArray[i] += ") ";
        }


        builder.setTitle(R.string.in_camera);
        builder.setItems(itemArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentShotPart = i;
                String group = getResources().getStringArray(R.array.in_camera_cato_item)[currentShotPart];

                Toast.makeText(CarCheckInteriorActivity.this, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                currentTimeMillis = System.currentTimeMillis();
                Uri fileUri = Helper.getOutputMediaFileUri(currentTimeMillis); // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                startActivityForResult(intent, Common.PHOTO_FOR_INTERIOR_STANDARD);
            }
        });
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //builder.setView(inflater.inflate(R.layout.bi_camera_cato_dialog, null));

        //builder.setMessage(R.string.ci_attention_content).setTitle(R.string.ci_attention);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Common.PHOTO_FOR_INTERIOR_STANDARD:
                if(resultCode == Activity.RESULT_OK) {
                    // 组织JsonString
                    JSONObject jsonObject = new JSONObject();

                    try {
                        JSONObject photoJsonObject = new JSONObject();
                        String currentPart = "";

                        switch (currentShotPart) {
                            case 0:
                                currentPart = "workbench";
                                break;
                            case 1:
                                currentPart = "steeringWheel";
                                break;
                            case 2:
                                currentPart = "dashboard";
                                break;
                            case 3:
                                currentPart = "leftDoor+steeringWheel";
                                break;
                            case 4:
                                currentPart = "rearSeats";
                                break;
                            case 5:
                                currentPart = "coDriverSeat";
                                break;
                            case 6:
                                currentPart = "other";
                                break;
                        }

                        photoJsonObject.put("part", currentPart);

                        jsonObject.put("Group", "interior");
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

                    setPhotoSize(currentTimeMillis, 800);

                    // 立刻上传
                    imageUploadQueue.addImage(photoEntity);

                    photoShotCount[currentShotPart]++;

                    in_start_camera();
                }
                break;
            case Common.IN_PAINT:
                if(!posEntities.isEmpty()) {
                    interiorPaintPreviewView.setAlpha(1f);
                    interiorPaintPreviewView.invalidate();
                    tip.setVisibility(View.GONE);
                }
                // 如果没点，则将图片设为半透明，添加提示文字
                else {
                    interiorPaintPreviewView.setAlpha(0.3f);
                    interiorPaintPreviewView.invalidate();
                    tip.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void saveResult() {
        // 如果是修改模式，则直接保存退出
        if(!jsonData.equals("")) {
            // 创建结果意图和包括地址
            Intent intent = new Intent();
            intent.putExtra("INDEX", Integer.toString(sealSpinner.getSelectedItemPosition()));
            intent.putExtra("COMMENT", commentEdit.getText().toString());
            intent.putExtra("PHOTO_COUNT", photoShotCount);
            intent.putExtra("SAVED", saved);
            //addPhotosToQueue();

            // 关闭activity
            setResult(Activity.RESULT_OK, intent);
            finish();

            return;
        }

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
                            intent.putExtra("INDEX", Integer.toString(sealSpinner.getSelectedItemPosition()));
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
            intent.putExtra("INDEX", Integer.toString(sealSpinner.getSelectedItemPosition()));
            intent.putExtra("COMMENT", commentEdit.getText().toString());
            intent.putExtra("PHOTO_COUNT", photoShotCount);
            intent.putExtra("SAVED", saved);
            //addPhotosToQueue();

            // 关闭activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private void discardResult() {
        // 创建结果意图和包括地址
        Intent intent = new Intent();
        intent.putExtra("PHOTO_COUNT", photoShotCount);

        // 关闭activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // 只有在保存时才提交照片
    private void addPhotosToQueue() {
        ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

        // 将缺陷点照片加入照片池
        for(int i = 0; i < photoEntities.size(); i++) {
            imageUploadQueue.addImage(photoEntities.get(i));
        }

        // 加入照片池后，将本身的photoEntities删除，以免重复上传
        while(!photoEntities.isEmpty()) {
            photoEntities.remove(0);
        }
    }

    public static void generateSketchPhotoEntity() {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" +
                ".cheyipai/" + getNameFromFigure(figure));
        File dst = new File(Environment.getExternalStorageDirectory().getPath() +
                "/Pictures/DFCarChecker/" + "interior");

        try {
            copy(file, dst);
        } catch (IOException e) {
            Log.d(Common.TAG, "拷贝" + getNameFromFigure(figure) + "错误");
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        // 组织jsonString
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("Group", "interior");
            jsonObject.put("Part", "sketch");

            JSONObject photoData = new JSONObject();
            photoData.put("height", bitmap.getHeight());
            photoData.put("width", bitmap.getWidth());

            jsonObject.put("PhotoData", photoData);
            jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
            jsonObject.put("UserId", MainActivity.userInfo.getId());
            jsonObject.put("Key", MainActivity.userInfo.getKey());
        } catch (JSONException e) {
            Log.d(Common.TAG, "json组织错误, " + getNameFromFigure(figure));
        }

        PhotoEntity photoEntity = new PhotoEntity();
        photoEntity.setFileName("interior");
        photoEntity.setJsonString(jsonObject.toString());

        // 将草图放入viewpager的草图队列
        CarCheckViewPagerActivity.sketchPhotoEntities.put("interior", photoEntity);
        Log.d(Common.TAG, "内饰草图生成！");
    }

    //  1 - d4s4,       2 - d2s4,       3 - d2s4,       4 - d4s4,       5 - van_i
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

    public static void copy(File src, File dst) throws IOException {
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

    private static String getNameFromFigure(int figure) {
        // 默认为三厢四门图
        String name = "d4s4";

        switch (figure) {
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

        return name;
    }

    public static JSONObject generateInteriorJsonObject() {
        JSONObject interior = new JSONObject();

        try {
            interior.put("sealingStrip", sealSpinner.getSelectedItem().toString());
            interior.put("comment", commentEdit.getText().toString());
        } catch (Exception e) {
            return null;
        }

        return interior;
    }

    private void letsEnterModifyMode() {
        showView(false, getWindow().getDecorView(), R.id.in_camera_1);
        showView(false, getWindow().getDecorView(), R.id.in_camera_2);
        showView(false, getWindow().getDecorView(), R.id.in_camera_3);

        interiorPaintPreviewView.setOnClickListener(null);
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
            JSONObject interior = photos.getJSONObject("interior");

            String comment = conditions.getJSONObject("interior").getString("comment");
            setEditText(getWindow().getDecorView(), R.id.in_comment_edit, comment);

            String sealingStrip = conditions.getJSONObject("interior").getString("sealingStrip");
            setSpinnerSelectionWithString(getWindow().getDecorView(), R.id.in_sealingStrip_spinner, sealingStrip);

            JSONObject sketch = interior.getJSONObject("sketch");
            String sketchUrl = sketch.getString("photo");

            new DownloadImageTask().execute(Common.PICTURE_ADDRESS + sketchUrl);
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
                Toast.makeText(CarCheckInteriorActivity.this, "下载图片失败",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            interiorPaintPreviewView.init(result, posEntities);
            interiorPaintPreviewView.invalidate();
            interiorPaintPreviewView.setAlpha(1.0f);
            tip.setVisibility(View.GONE);
        }
    }
}

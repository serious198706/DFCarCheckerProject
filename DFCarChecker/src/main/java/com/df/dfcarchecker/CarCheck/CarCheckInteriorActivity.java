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

import com.df.dfcarchecker.LoginActivity;
import com.df.dfcarchecker.PopupActivity;
import com.df.dfcarchecker.R;
import com.df.entry.PosEntity;
import com.df.entry.PhotoEntity;
import com.df.paintview.InteriorPaintPreviewView;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.ImageUploadQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

import static com.df.service.Helper.setEditText;
import static com.df.service.Helper.setSpinnerSelectionWithString;
import static com.df.service.Helper.setTextView;

public class CarCheckInteriorActivity extends Activity implements View.OnClickListener  {
    private int currentShotPart;

    public static List<PosEntity> posEntities = CarCheckIntegratedFragment.interiorPaintEntities;
    public static List<PhotoEntity> photoEntities = CarCheckIntegratedFragment.exteriorPhotoEntities;

    private String brokenParts;
    private String dirtyParts;

    private InteriorPaintPreviewView interiorPaintPreviewView;
    private TextView tip;

    private static Spinner sealSpinner;
    private static EditText commentEdit;
    private long currentTimeMillis;
    private ImageUploadQueue imageUploadQueue;

    private int[] photoShotCount = {0, 0, 0, 0, 0, 0, 0};
    private JSONObject photos;
    private JSONObject conditions;

    private String jsonData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check_interior);

        Button brokenButton = (Button) findViewById(R.id.in_choose_broken_button);
        brokenButton.setOnClickListener(this);
        Button dirtyButton = (Button) findViewById(R.id.in_choose_dirty_button);
        dirtyButton.setOnClickListener(this);
        Button cameraButton = (Button) findViewById(R.id.in_start_camera_button);
        cameraButton.setOnClickListener(this);

        // 点击图片进入绘制界面
        int figure = Integer.parseInt(CarCheckBasicInfoFragment.mCarSettings.getFigure());
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
            case R.id.action_discard:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.in_choose_broken_button:
                ChooseBroken();
                break;
            case R.id.in_choose_dirty_button:
                ChooseDirty();
                break;
            case R.id.in_start_camera_button:
                in_start_camera();
                break;
            case R.id.in_base_image_preview:
            case R.id.tipOnPreview:
                StartPaint();
                break;

        }
    }

    public void ChooseBroken() {
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("POPUP_TYPE", "IN_BROKEN");

        // 如果该部位已破损，则无需再设置为脏污，所以将脏污及破损部位一起传入
        intent.putExtra("BROKEN_PARTS", brokenParts);
        intent.putExtra("DIRTY_PARTS", dirtyParts);

        startActivityForResult(intent, Common.CHOOSE_IN_BROKEN);
    }

    public void ChooseDirty() {
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("POPUP_TYPE", "IN_DIRTY");

        // 如果该部位已脏污，则无需再设置为破损，所以将脏污及破损部位一起传入
        intent.putExtra("BROKEN_PARTS", brokenParts);
        intent.putExtra("DIRTY_PARTS", dirtyParts);

        startActivityForResult(intent, Common.CHOOSE_IN_DIRTY);
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

                startActivityForResult(intent, Common.PHOTO_FOR_INSIDE_GROUP);
            }
        });
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        //builder.setView(inflater.inflate(R.layout.bi_camera_cato_dialog, null));

        //builder.setMessage(R.string.ci_attention_content).setTitle(R.string.ci_attention);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Common.CHOOSE_IN_BROKEN:
                // 查找成功
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String brokenParts = bundle.getString(Common.IN_BROKEN_RESULT);
                            if(brokenParts != null) {
                                EditText editText = (EditText) findViewById(R.id.in_broken_parts_edit);
                                editText.setText(brokenParts);
                            }

                            this.brokenParts = brokenParts;
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
            case Common.CHOOSE_IN_DIRTY:
                // 查找成功
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String dirtyParts = bundle.getString(Common.IN_DIRTY_RESULT);
                            if(dirtyParts != null) {
                                EditText editText = (EditText) findViewById(R.id.in_dirty_parts_edit);
                                editText.setText(dirtyParts);
                            }

                            this.dirtyParts = dirtyParts;
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
            case Common.PHOTO_FOR_INSIDE_GROUP:
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
                        jsonObject.put("UserId", LoginActivity.userInfo.getId());
                        jsonObject.put("Key", LoginActivity.userInfo.getKey());
                        jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
                    } catch (JSONException e) {

                    }

                    PhotoEntity photoEntity = new PhotoEntity();
                    photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
                    photoEntity.setJsonString(jsonObject.toString());

                    // 立刻上传
                    imageUploadQueue.addImage(photoEntity);

                    photoShotCount[currentShotPart]++;

                    in_start_camera();
                }  else {
                    Toast.makeText(CarCheckInteriorActivity.this,
                            "error occured during opening camera", Toast.LENGTH_SHORT)
                            .show();
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
        // 创建结果意图和包括地址
        // TODO: 保存已拍摄的照片数量
        Intent intent = new Intent();
        intent.putExtra("INDEX", Integer.toString(sealSpinner.getSelectedItemPosition()));
        intent.putExtra("COMMENT", commentEdit.getText().toString());
        intent.putExtra("PHOTO_COUNT", photoShotCount);

        addPhotosToQueue();

        // 关闭activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // 只有在保存时才提交照片
    private void addPhotosToQueue() {
        ImageUploadQueue imageUploadQueue = ImageUploadQueue.getInstance();

        for(int i = 0; i < photoEntities.size(); i++) {
            imageUploadQueue.addImage(photoEntities.get(i));
        }

        // 加入照片池后，将本身的photoEntities删除，以免重复上传
        while(!photoEntities.isEmpty()) {
            photoEntities.remove(0);
        }

        if(CarCheckPaintActivity.sketchPhotoEntities != null) {
            for(int i = 0; i < CarCheckPaintActivity.sketchPhotoEntities.size(); i++) {
                imageUploadQueue.addImage(CarCheckPaintActivity.sketchPhotoEntities.get(i));
            }

            while(!CarCheckPaintActivity.sketchPhotoEntities.isEmpty()) {
                CarCheckPaintActivity.sketchPhotoEntities.remove(0);
            }
        }
    }

    //  1 - d4s4,       2 - d2s4,       3 - d2s4,       4 - d4s4,       5 - van_i
    private Bitmap getBitmapFromFigure(int figure) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String path = Environment.getExternalStorageDirectory().toString();
        path += "/.cheyipai/";

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

        return BitmapFactory.decodeFile(path + name, options);
    }

    public static JSONObject generateInteriorJsonObject() {
        JSONObject interior = new JSONObject();

        try {
            interior.put("sealingStrip", sealSpinner.getSelectedItem().toString());
            interior.put("comment", commentEdit.getText().toString());
        } catch (JSONException e) {

        }

        return interior;
    }

    private void letsEnterModifyMode() {
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
            setEditText(getWindow().getDecorView(), R.id.it_comment_edit, comment);

            String sealingStrip = conditions.getJSONObject("interior").getString("sealingStrip");
            setSpinnerSelectionWithString(getWindow().getDecorView(), R.id.in_sealingStrip_spinner, sealingStrip);

//            JSONArray fault = interior.getJSONArray("fault");
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

            JSONObject sketch = interior.getJSONObject("sketch");
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
            interiorPaintPreviewView.init(result, posEntities);
            interiorPaintPreviewView.invalidate();
            interiorPaintPreviewView.setAlpha(1.0f);
            tip.setVisibility(View.GONE);
        }
    }
}

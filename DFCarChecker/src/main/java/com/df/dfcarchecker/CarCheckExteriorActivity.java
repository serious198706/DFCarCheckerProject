package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.df.entry.PosEntity;
import com.df.entry.PhotoEntity;
import com.df.paintview.ExteriorPaintPreviewView;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.ImageUploadQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CarCheckExteriorActivity extends Activity implements View.OnClickListener {
    private int currentShotPart;
    private EditText brokenEdit;
    private static Spinner smoothSpinner;
    private static EditText commentEdit;
    public static List<PosEntity> posEntities = CarCheckIntegratedFragment.outsidePaintEntities;
    public static List<PhotoEntity> photoEntities = CarCheckIntegratedFragment.outsidePhotoEntities;
    private ExteriorPaintPreviewView exteriorPaintPreviewView;
    private TextView tip;
    private String brokenParts;

    private ImageUploadQueue imageUploadQueue;
    private long currentTimeMillis;
    private int[] photoShotCount = {0, 0, 0, 0, 0, 0, 0};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_car_check_exterior);

        // 点击图片进入绘制界面
        int figure = Integer.parseInt(CarCheckBasicInfoFragment.mCarSettings.getFigure());
        Bitmap previewViewBitmap = getBitmapFromFigure(figure);

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
        }

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(!posEntities.isEmpty()) {
            exteriorPaintPreviewView.setAlpha(1f);
            exteriorPaintPreviewView.invalidate();
            tip.setVisibility(View.GONE);
        }

        imageUploadQueue = ImageUploadQueue.getInstance();
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

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
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
        // 创建结果意图和包括地址
        Intent intent = new Intent();
        intent.putExtra("INDEX", Integer.toString(smoothSpinner.getSelectedItemPosition()));
        intent.putExtra("COMMENT", commentEdit.getText().toString());

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

            // 加入照片池后，将本身的photoEntities删除，以免重复上传
            while(!photoEntities.isEmpty()) {
                photoEntities.remove(0);
            }
        }

        // 如果用户并未进入绘图界面就点击了“保存”
        if(CarCheckPaintActivity.sketchPhotoEntities != null) {
            for(int i = 0; i < CarCheckPaintActivity.sketchPhotoEntities.size(); i++) {
                imageUploadQueue.addImage(CarCheckPaintActivity.sketchPhotoEntities.get(i));
            }

            while(!CarCheckPaintActivity.sketchPhotoEntities.isEmpty()) {
                CarCheckPaintActivity.sketchPhotoEntities.remove(0);
            }
        }
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

    public static JSONObject generateExteriorJsonObject() {
        JSONObject exterior = new JSONObject();

        try {
            exterior.put("smooth", smoothSpinner.getSelectedItem().toString());
            exterior.put("comment", commentEdit.getText().toString());
        } catch (JSONException e) {

        }

        return exterior;
    }
}

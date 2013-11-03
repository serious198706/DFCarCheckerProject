package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.df.entry.FaultPhotoEntity;
import com.df.entry.PhotoEntity;
import com.df.paintview.InsidePaintPreviewView;
import com.df.service.Common;
import com.df.service.ImageUploadQueue;

import java.util.List;

public class CarCheckInsideActivity extends Activity implements View.OnClickListener  {
    private int currentGroup;

    public static List<FaultPhotoEntity> posEntities = CarCheckIntegratedFragment.insidePaintEntities;
    public static List<PhotoEntity> photoEntities = CarCheckIntegratedFragment.outsidePhotoEntities;

    private String brokenParts;
    private String dirtyParts;

    private InsidePaintPreviewView insidePaintPreviewView;
    private TextView tip;

    private Spinner sealSpinner;
    private EditText commentEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_car_check_inside);

        Button brokenButton = (Button) findViewById(R.id.in_choose_broken_button);
        brokenButton.setOnClickListener(this);
        Button dirtyButton = (Button) findViewById(R.id.in_choose_dirty_button);
        dirtyButton.setOnClickListener(this);
        Button cameraButton = (Button) findViewById(R.id.in_start_camera_button);
        cameraButton.setOnClickListener(this);

        // 点击图片进入绘制界面
        int figure = Integer.parseInt(CarCheckBasicInfoFragment.mCarSettings.getFigure());
        Bitmap previewViewBitmap = getBitmapFromFigure(figure);

        insidePaintPreviewView = (InsidePaintPreviewView) findViewById(R.id.in_base_image_preview);
        insidePaintPreviewView.init(previewViewBitmap, posEntities);
        insidePaintPreviewView.setOnClickListener(this);

        tip = (TextView) findViewById(R.id.tipOnPreview);
        tip.setOnClickListener(this);

        sealSpinner = (Spinner) findViewById(R.id.in_seal_spinner);
        commentEdit = (EditText) findViewById(R.id.commentEdit);

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
        }

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(!posEntities.isEmpty()) {
            insidePaintPreviewView.setAlpha(1f);
            insidePaintPreviewView.invalidate();
            tip.setVisibility(View.GONE);
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

        builder.setTitle(R.string.in_camera);
        builder.setItems(R.array.in_camera_cato_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentGroup = i;
                String group = getResources().getStringArray(R.array.in_camera_cato_item)[currentGroup];

                Toast.makeText(CarCheckInsideActivity.this, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
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
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    //img.setImageBitmap(image);
                } else {
                    Toast.makeText(CarCheckInsideActivity.this,
                            "error occured during opening camera", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case Common.IN_PAINT:
                if(!posEntities.isEmpty()) {
                    insidePaintPreviewView.setAlpha(1f);
                    insidePaintPreviewView.invalidate();
                    tip.setVisibility(View.GONE);
                }
                // 如果没点，则将图片设为半透明，添加提示文字
                else {
                    insidePaintPreviewView.setAlpha(0.3f);
                    insidePaintPreviewView.invalidate();
                    tip.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void saveResult() {
        // 创建结果意图和包括地址
        Intent intent = new Intent();
        intent.putExtra("INDEX", Integer.toString(sealSpinner.getSelectedItemPosition()));
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
}

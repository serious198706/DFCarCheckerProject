package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.df.paintview.OutsidePaintPreviewView;
import com.df.service.Common;
import com.df.service.PosEntity;

import java.util.ArrayList;
import java.util.List;

public class CarCheckOutsideActivity extends Activity implements View.OnClickListener {
    private int currentShotGroup;
    private EditText brokenEdit;
    private Spinner paintSpinner;
    private EditText commentEdit;
    public static List<PosEntity> posEntities;
    private OutsidePaintPreviewView outsidePaintPreviewView;
    private TextView tip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_car_check_outside);

        // 点击图片进入绘制界面
        outsidePaintPreviewView = (OutsidePaintPreviewView) findViewById(R.id.out_base_image_preview);
        outsidePaintPreviewView.setOnClickListener(this);

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
        paintSpinner = (Spinner) findViewById(R.id.out_paint_spinner);

        // 备注
        commentEdit = (EditText) findViewById(R.id.out_comment_edit);

        // 坐标们
        posEntities = new ArrayList<PosEntity>();

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // 当用户保存时，将数据保存，以便再次进入时查看
        savedInstanceState.putString("brokenEdit", brokenEdit.getText().toString());
        savedInstanceState.putInt("paintSpinnerPosition", paintSpinner.getSelectedItemPosition());
        savedInstanceState.putString("comment", commentEdit.getText().toString());
        //savedInstanceState.putParcelable("entities", posEntitiesFront);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        brokenEdit.setText(savedInstanceState.getString("brokenEdit"));
        paintSpinner.setSelection(savedInstanceState.getInt("paintSpinnerPosition"));
        commentEdit.setText(savedInstanceState.getString("comment"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car_check_frame, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_done:
                // TODO 提交数据，并保存当前Activity

                break;
            case R.id.action_cancel:
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
        startActivityForResult(intent, Common.CHOOSE_OUT_BROKEN);
    }

    public void out_start_camera() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.out_camera);
        builder.setItems(R.array.out_camera_cato_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentShotGroup = i;
                String group = getResources().getStringArray(R.array.out_camera_cato_item)[currentShotGroup];

                Toast.makeText(CarCheckOutsideActivity.this, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
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
        intent.putExtra("PAINT_TYPE", "OUT_PAINT");
        startActivityForResult(intent, Common.OUT_PAINT);
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
                        }
                    }
                    catch(NullPointerException ex) {

                    }
                }
                break;
            case Common.PHOTO_FOR_OUTSIDE_GROUP:
                if(resultCode == Activity.RESULT_OK) {
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    //img.setImageBitmap(image);
                } else {
                    Toast.makeText(CarCheckOutsideActivity.this,
                            "相机打开错误", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case Common.OUT_PAINT:
                // 如果有点，则将图片设为不透明，去掉提示文字
                if(!posEntities.isEmpty()) {
                    outsidePaintPreviewView.setAlpha(1f);
                    outsidePaintPreviewView.invalidate();
                    tip.setVisibility(View.GONE);
                }
                // 如果没点，则将图片设为半透明，添加提示文字
                else {
                    outsidePaintPreviewView.setAlpha(0.3f);
                    outsidePaintPreviewView.invalidate();
                    tip.setVisibility(View.VISIBLE);
                }
                break;
        }
    }


}

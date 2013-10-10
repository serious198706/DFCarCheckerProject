package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.df.service.Common;
import com.df.service.PosEntity;

import java.util.ArrayList;
import java.util.List;

public class CarCheckInsideActivity extends Activity implements View.OnClickListener  {
    private int currentGroup;

    public static List<PosEntity> posEntities;

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
        Button startPaintButton = (Button) findViewById(R.id.in_start_paint_button);
        startPaintButton.setOnClickListener(this);

        posEntities = new ArrayList<PosEntity>();

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
                // TODO 提交数据
                finish();
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
            case R.id.in_choose_broken_button:
                ChooseBroken(v);
                break;
            case R.id.in_choose_dirty_button:
                ChooseDirty(v);
                break;
            case R.id.in_start_camera_button:
                in_start_camera(v);
                break;
            case R.id.in_start_paint_button:
                StartPaint(v);
                break;
        }
    }


    public void ChooseBroken(View v) {
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("POPUP_TYPE", "IN_BROKEN");
        startActivityForResult(intent, Common.CHOOSE_IN_BROKEN);
    }

    public void ChooseDirty(View v) {
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("POPUP_TYPE", "IN_DIRTY");
        startActivityForResult(intent, Common.CHOOSE_IN_DIRTY);
    }

    public void StartPaint(View v) {
        Intent intent = new Intent(this, CarCheckOutSidePaintActivity.class);
        intent.putExtra("PAINT_TYPE", "IN_PAINT");
        startActivityForResult(intent, Common.IN_PAINT);
    }

    public void in_start_camera(View v) {
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
                            String brokenPart = bundle.getString(Common.IN_BROKEN_RESULT);
                            if(brokenPart != null) {
                                EditText editText = (EditText) findViewById(R.id.in_broken_parts_edit);
                                editText.setText(brokenPart);
                            }
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
                            String dirtyPart = bundle.getString(Common.IN_DIRTY_RESULT);
                            if(dirtyPart != null) {
                                EditText editText = (EditText) findViewById(R.id.in_dirty_parts_edit);
                                editText.setText(dirtyPart);
                            }
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
                Toast.makeText(CarCheckInsideActivity.this, "aa", Toast.LENGTH_LONG).show();
                break;
        }
    }

}
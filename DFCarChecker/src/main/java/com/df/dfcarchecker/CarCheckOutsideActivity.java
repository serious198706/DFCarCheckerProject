package com.df.dfcarchecker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.df.service.Common;
import com.df.service.PosEntity;

import java.util.ArrayList;
import java.util.List;

public class CarCheckOutsideActivity extends Activity {
    private int currentGroup;

    public static List<PosEntity> posEntities;

    private OutsidePaintPreviewView outsidePaintPreviewView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_car_check_outside);

        outsidePaintPreviewView = (OutsidePaintPreviewView) findViewById(R.id.out_base_image_preview);

        Button brokenButton = (Button) findViewById(R.id.out_choose_broken_button);
        brokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseBroken();
            }
        });

        Button cameraButton = (Button) findViewById(R.id.out_start_camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                out_start_camera();
            }
        });
        Button startPaintButton = (Button) findViewById(R.id.out_start_paint_button);
        startPaintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartPaint();
            }
        });

        posEntities = new ArrayList<PosEntity>();
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
                currentGroup = i;
                String group = getResources().getStringArray(R.array.out_camera_cato_item)[currentGroup];

                Toast.makeText(CarCheckOutsideActivity.this, "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, Common.PHOTO_FOR_OUTSIDE_GROUP);
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

    private void StartPaint() {
        Intent intent = new Intent(this, CarCheckOutSidePaintActivity.class);
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
                                EditText editText = (EditText) findViewById(R.id.out_broken_edit);
                                editText.setText(brokenPart);
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
                            "error occured during opening camera", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case Common.OUT_PAINT:
                outsidePaintPreviewView.invalidate();
                break;
        }
    }


}

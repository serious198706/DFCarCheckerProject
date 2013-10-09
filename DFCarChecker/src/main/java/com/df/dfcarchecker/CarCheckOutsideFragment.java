package com.df.dfcarchecker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.df.service.Common;
import com.df.service.PosEntity;

import java.util.ArrayList;
import java.util.List;

public class CarCheckOutsideFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private List<Integer> brokenArrayIndex;
    private LayoutInflater inflater;
    private int currentGroup;

    public static List<PosEntity> posEntities;

    private OutsidePaintPreviewView outsidePaintPreviewView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_outside, container, false);
        outsidePaintPreviewView = (OutsidePaintPreviewView) rootView.findViewById(R.id.out_base_image_preview);

        Button brokenButton = (Button)rootView.findViewById(R.id.out_choose_broken_button);
        brokenButton.setOnClickListener(this);
        Button cameraButton = (Button)rootView.findViewById(R.id.out_start_camera_button);
        cameraButton.setOnClickListener(this);
        Button startPaintButton = (Button)rootView.findViewById(R.id.out_start_paint_button);
        startPaintButton.setOnClickListener(this);

        posEntities = new ArrayList<PosEntity>();

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.out_choose_broken_button:
                ChooseBroken(v);
                break;
            case R.id.out_start_camera_button:
                out_start_camera(v);
                break;
            case R.id.out_start_paint_button:
                StartPaint(v);
                break;
        }
    }

    public void ChooseBroken(View v) {
        Intent intent = new Intent(rootView.getContext(), PopupActivity.class);
        intent.putExtra("POPUP_TYPE", "OUT_BROKEN");
        startActivityForResult(intent, Common.CHOOSE_OUT_BROKEN);
    }

    public void out_start_camera(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

        // Get the layout inflater
        LayoutInflater inflater = this.inflater;

        builder.setTitle(R.string.out_camera);
        builder.setItems(R.array.out_camera_cato_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentGroup = i;
                String group = getResources().getStringArray(R.array.out_camera_cato_item)[currentGroup];

                Toast.makeText(rootView.getContext(), "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

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

    private void StartPaint(View v) {
        Intent intent = new Intent(rootView.getContext(), CarCheckOutSidePaintActivity.class);
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
                                EditText editText = (EditText)rootView.findViewById(R.id.out_broken_edit);
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
                    Toast.makeText(rootView.getContext(),
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

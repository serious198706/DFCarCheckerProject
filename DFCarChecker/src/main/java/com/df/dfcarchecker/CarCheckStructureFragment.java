package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.df.service.Common;
import com.df.service.PosEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 岩 on 13-10-8.
 */
public class CarCheckStructureFragment extends Fragment implements View.OnClickListener  {
    private static View rootView;
    private static ScrollView root;
    private LayoutInflater inflater;
    private int currentGroup;

    public static List<PosEntity> posEntities;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_structure, container, false);

        // TODO:
        Button cameraButton = (Button)rootView.findViewById(R.id.structure_start_camera_button);
        cameraButton.setOnClickListener(this);
        Button startPaintButton = (Button)rootView.findViewById(R.id.structure_start_paint_button);
        startPaintButton.setOnClickListener(this);
        posEntities = new ArrayList<PosEntity>();

        root = (ScrollView)rootView.findViewById(R.id.root);
        root.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.structure_start_paint_button:
                StartPaint(v);
                break;
            case R.id.structure_start_camera_button:
                structure_start_camera(v);
                break;
        }
    }

    public static void ShowContent() {
        root.setVisibility(View.VISIBLE);
    }

    public void StartPaint(View v) {
        Intent intent = new Intent(rootView.getContext(), CarCheckOutSidePaintActivity.class);
        intent.putExtra("PAINT_TYPE", "STRUCTURE_PAINT");
        startActivityForResult(intent, Common.STURCTURE_PAINT);
    }

    public void structure_start_camera(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

        // Get the layout inflater
        LayoutInflater inflater = this.inflater;

        builder.setTitle(R.string.structure_camera);
        builder.setItems(R.array.structure_camera_cato_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentGroup = i;
                String group = getResources().getStringArray(R.array.structure_camera_cato_item)[currentGroup];

                Toast.makeText(rootView.getContext(), "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

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
            case Common.PHOTO_FOR_STRUCTURE_GROUP:
                if(resultCode == Activity.RESULT_OK) {
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    //img.setImageBitmap(image);
                } else {
                    Toast.makeText(rootView.getContext(),
                            "error occured during opening camera", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case Common.STURCTURE_PAINT:
                Toast.makeText(rootView.getContext(), "aa", Toast.LENGTH_LONG).show();
                break;
        }
    }

}
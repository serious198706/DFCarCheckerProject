package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.df.paintview.StructurePaintPreviewView;
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

    public static List<PosEntity> posEntitiesFront;
    public static List<PosEntity> posEntitiesRear;

    private StructurePaintPreviewView structurePaintPreviewViewFront;
    private StructurePaintPreviewView structurePaintPreviewViewRear;
    private TextView tipFront;
    private TextView tipRear;

    public static Bitmap previewBitmapFront;
    public static Bitmap previewBitmapRear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_structure, container, false);

        // TODO:
        Button cameraButton = (Button)rootView.findViewById(R.id.structure_start_camera_button);
        cameraButton.setOnClickListener(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        posEntitiesFront = new ArrayList<PosEntity>();
        posEntitiesRear = new ArrayList<PosEntity>();

        String sdcardPath = Environment.getExternalStorageDirectory().toString();

        previewBitmapFront = BitmapFactory.decodeFile(sdcardPath + "/cheyipai/st_f.png", options);
        structurePaintPreviewViewFront = (StructurePaintPreviewView)rootView.findViewById(R.id.structure_base_image_preview_front);
        structurePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);
        structurePaintPreviewViewFront.setOnClickListener(this);

        tipFront = (TextView)rootView.findViewById(R.id.tipOnPreviewFront);
        tipFront.setOnClickListener(this);

        previewBitmapRear = BitmapFactory.decodeFile(sdcardPath + "/cheyipai/st_r.png", options);
        structurePaintPreviewViewRear = (StructurePaintPreviewView)rootView.findViewById(R.id.structure_base_image_preview_rear);
        structurePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
        structurePaintPreviewViewRear.setOnClickListener(this);

        tipRear = (TextView)rootView.findViewById(R.id.tipOnPreviewRear);
        tipRear.setOnClickListener(this);

        root = (ScrollView)rootView.findViewById(R.id.root);
        root.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.tipOnPreviewFront:
            case R.id.structure_base_image_preview_front:
                StartPaint("FRONT");
                break;
            case R.id.tipOnPreviewRear:
            case R.id.structure_base_image_preview_rear:
                StartPaint("REAR");
                break;
            case R.id.structure_start_camera_button:
                structure_start_camera(v);
                break;
        }
    }

    public static void ShowContent() {
        root.setVisibility(View.VISIBLE);
    }

    public void StartPaint(String frontOrRear) {
        Intent intent = new Intent(rootView.getContext(), CarCheckPaintActivity.class);
        intent.putExtra("PAINT_TYPE", "STRUCTURE_PAINT");

        // 设定视角
        intent.putExtra("PAINT_SIGHT", frontOrRear);
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
                // 前视角
                // 如果有点，则将图片设为不透明，去掉提示文字
                if(!posEntitiesFront.isEmpty()) {
                    structurePaintPreviewViewFront.setAlpha(1f);
                    structurePaintPreviewViewFront.setPosEntities(this.posEntitiesFront);
                    structurePaintPreviewViewFront.invalidate();
                    tipFront.setVisibility(View.GONE);
                }
                // 如果没点，则将图片设为半透明，添加提示文字
                else {
                    structurePaintPreviewViewFront.setAlpha(0.3f);
                    structurePaintPreviewViewFront.invalidate();
                    tipFront.setVisibility(View.VISIBLE);
                }

                // 后视角
                if(!posEntitiesRear.isEmpty()) {
                    structurePaintPreviewViewRear.setAlpha(1f);
                    structurePaintPreviewViewRear.setPosEntities(this.posEntitiesRear);
                    structurePaintPreviewViewRear.invalidate();
                    tipRear.setVisibility(View.GONE);
                } else {
                    structurePaintPreviewViewRear.setAlpha(0.3f);
                    structurePaintPreviewViewRear.invalidate();
                    tipRear.setVisibility(View.VISIBLE);
                }

                break;
        }
    }

}
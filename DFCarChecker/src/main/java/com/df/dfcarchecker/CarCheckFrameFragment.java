package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.df.entry.PosEntity;
import com.df.entry.PhotoEntity;
import com.df.paintview.FramePaintPreviewView;
import com.df.service.Common;
import com.df.service.Helper;
import com.df.service.ImageUploadQueue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.df.service.Helper.getEditText;

/**
 * Created by 岩 on 13-10-8.
 */
public class CarCheckFrameFragment extends Fragment implements View.OnClickListener  {
    private static View rootView;
    private static ScrollView root;
    private LayoutInflater inflater;
    private int currentShotPart;

    public static List<PosEntity> posEntitiesFront;
    public static List<PosEntity> posEntitiesRear;

    public static List<PhotoEntity> photoEntitiesFront;
    public static List<PhotoEntity> photoEntitiesRear;

    private static FramePaintPreviewView framePaintPreviewViewFront;
    private static FramePaintPreviewView framePaintPreviewViewRear;
    private TextView tipFront;
    private TextView tipRear;

    public static Bitmap previewBitmapFront;
    public static Bitmap previewBitmapRear;

    private long currentTimeMillis;

    private ImageUploadQueue imageUploadQueue;

    private int photoShotCount[] = {0, 0, 0, 0};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_frame, container, false);

        Button cameraButton = (Button)rootView.findViewById(R.id.structure_start_camera_button);
        cameraButton.setOnClickListener(this);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        posEntitiesFront = new ArrayList<PosEntity>();
        posEntitiesRear = new ArrayList<PosEntity>();

        photoEntitiesFront = new ArrayList<PhotoEntity>();
        photoEntitiesRear = new ArrayList<PhotoEntity>();

        String sdcardPath = Environment.getExternalStorageDirectory().toString();
        previewBitmapFront = BitmapFactory.decodeFile(sdcardPath + "/.cheyipai/d4_f", options);
        framePaintPreviewViewFront = (FramePaintPreviewView)rootView.findViewById(R.id.structure_base_image_preview_front);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);
        framePaintPreviewViewFront.setOnClickListener(this);

        tipFront = (TextView)rootView.findViewById(R.id.tipOnPreviewFront);
        tipFront.setOnClickListener(this);

        previewBitmapRear = BitmapFactory.decodeFile(sdcardPath + "/.cheyipai/d4_r", options);
        framePaintPreviewViewRear = (FramePaintPreviewView)rootView.findViewById(R.id.structure_base_image_preview_rear);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
        framePaintPreviewViewRear.setOnClickListener(this);

        tipRear = (TextView)rootView.findViewById(R.id.tipOnPreviewRear);
        tipRear.setOnClickListener(this);

        root = (ScrollView)rootView.findViewById(R.id.root);
        root.setVisibility(View.GONE);

        imageUploadQueue = ImageUploadQueue.getInstance();

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

    public static void showContent() {
        root.setVisibility(View.VISIBLE);
    }

    // 设定要显示的图
    public static void setFigureImage(int figure) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        String path = Environment.getExternalStorageDirectory().toString();
        path += "/.cheyipai/";

        // 默认为4门图
        String front = "d4_f";
        String rear = "d4_r";

        // 只有当figure为2、3时才是2门图
        switch (figure) {
            case 2:
            case 3:
                front = "d2_f";
                rear = "d2_r";
                break;
        }

        previewBitmapFront = BitmapFactory.decodeFile(path + front, options);
        framePaintPreviewViewFront.init(previewBitmapFront, posEntitiesFront);

        previewBitmapRear = BitmapFactory.decodeFile(path + rear, options);
        framePaintPreviewViewRear.init(previewBitmapRear, posEntitiesRear);
    }

    public static JSONObject generateFrameJsonObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("comment", getEditText(rootView, R.id.comment_edit));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
    public void StartPaint(String frontOrRear) {
        Intent intent = new Intent(rootView.getContext(), CarCheckPaintActivity.class);
        intent.putExtra("PAINT_TYPE", "FRAME_PAINT");

        // 设定视角
        intent.putExtra("PAINT_SIGHT", frontOrRear);
        startActivityForResult(intent, Common.STURCTURE_PAINT);
    }

    public void structure_start_camera(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

        // Get the layout inflater
        LayoutInflater inflater = this.inflater;

        builder.setTitle(R.string.structure_camera);

        String[] itemArray = rootView.getResources().getStringArray(R.array
                .structure_camera_cato_item);

        for(int i = 0; i < itemArray.length; i++) {
            itemArray[i] += " (";
            itemArray[i] += Integer.toString(photoShotCount[i]);
            itemArray[i] += ") ";
        }

        builder.setItems(itemArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentShotPart = i;

                String group = getResources().getStringArray(R.array.structure_camera_cato_item)[currentShotPart];

                Toast.makeText(rootView.getContext(), "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                currentTimeMillis = System.currentTimeMillis();
                Uri fileUri = Helper.getOutputMediaFileUri(currentTimeMillis); // create a file to save the image
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                startActivityForResult(intent, Common.PHOTO_FOR_STRUCTURE_GROUP);
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
                    // 组织JsonString
                    JSONObject jsonObject = new JSONObject();

                    try {
                        JSONObject photoJsonObject = new JSONObject();

                        String currentPart = "";

                        switch (currentShotPart) {
                            case 0:
                                currentPart = "overview";
                                break;
                            case 1:
                                currentPart = "front";
                                break;
                            case 2:
                                currentPart = "rear";
                                break;
                            case 3:
                                currentPart = "other";
                                break;
                        }

                        photoJsonObject.put("part", currentPart);

                        jsonObject.put("Group", "engineRoom");
                        jsonObject.put("PhotoData", photoJsonObject);
                        jsonObject.put("UserId", LoginActivity.userInfo.getId());
                        jsonObject.put("Key", LoginActivity.userInfo.getKey());
                        jsonObject.put("UniqueId", CarCheckBasicInfoFragment.uniqueId);
                    } catch (JSONException e) {

                    }

                    PhotoEntity photoEntity = new PhotoEntity();
                    photoEntity.setFileName(Long.toString(currentTimeMillis) + ".jpg");
                    photoEntity.setJsonString(jsonObject.toString());

                    photoShotCount[currentShotPart]++;

                    // 拍摄完成后立刻上传
                    imageUploadQueue.addImage(photoEntity);
                } else {
                    Toast.makeText(rootView.getContext(),
                            "相机打开错误！", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case Common.STURCTURE_PAINT:
                // 前视角
                // 如果有点，则将图片设为不透明，去掉提示文字
                if(!posEntitiesFront.isEmpty()) {
                    framePaintPreviewViewFront.setAlpha(1f);
                    framePaintPreviewViewFront.setPosEntities(this.posEntitiesFront);
                    framePaintPreviewViewFront.invalidate();
                    tipFront.setVisibility(View.GONE);
                }
                // 如果没点，则将图片设为半透明，添加提示文字
                else {
                    framePaintPreviewViewFront.setAlpha(0.3f);
                    framePaintPreviewViewFront.invalidate();
                    tipFront.setVisibility(View.VISIBLE);
                }

                // 后视角
                if(!posEntitiesRear.isEmpty()) {
                    framePaintPreviewViewRear.setAlpha(1f);
                    framePaintPreviewViewRear.setPosEntities(this.posEntitiesRear);
                    framePaintPreviewViewRear.invalidate();
                    tipRear.setVisibility(View.GONE);
                } else {
                    framePaintPreviewViewRear.setAlpha(0.3f);
                    framePaintPreviewViewRear.invalidate();
                    tipRear.setVisibility(View.VISIBLE);
                }

                break;
        }
    }
}
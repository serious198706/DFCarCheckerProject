package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.df.service.Common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarCheckAccidentFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;
    private List<Integer> glassArrayIndex;
    private List<Integer> screwArrayIndex;
    private Switch[] switchButtons;
    private ImageView[] imageViewButtons;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private int currentGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_accident, container, false);

        Button glassButton = (Button)rootView.findViewById(R.id.ac_choose_glass_button);
        glassButton.setOnClickListener(this);
        Button screwButton = (Button)rootView.findViewById(R.id.ac_choose_screw_button);
        screwButton.setOnClickListener(this);
        Button cameraButton = (Button) rootView.findViewById(R.id.ac_start_camera_button);
        cameraButton.setOnClickListener(this);
        Button collectDataButton = (Button) rootView.findViewById(R.id.ac_collect_data_button);
        collectDataButton.setOnClickListener(this);

        glassArrayIndex = new ArrayList<Integer>();
        screwArrayIndex = new ArrayList<Integer>();


        // 为所有Switch绑定事件
        HandleSwitchButtons();

        // 为所有ImageView初始化为不可见，并绑定事件
        HandleImageViewButtons();

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ac_choose_glass_button:
                ChooseGlass(v);
                break;
            case R.id.ac_choose_screw_button:
                ChooseScrew(v);
                break;
            case R.id.ac_start_camera_button:
                ac_start_camera(v);
                break;
            case R.id.ac_collect_data_button:
                CollectData(v);
                break;
        }
    }

    private void HandleSwitchButtons() {
        switchButtons = new Switch[20];
        for (int i = 0; i < 20; i++) {
            int id = getResources().getIdentifier("ac_prob" + (i + 1) + "_switch", "id", rootView.getContext().getPackageName());
            switchButtons[i] = (Switch) rootView.findViewById(id);
            switchButtons[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        ImageView imageView = (ImageView)rootView.findViewById(buttonView.getId() + 1);
                        imageView.setVisibility(View.VISIBLE);
                    } else {
                        ImageView imageView = (ImageView)rootView.findViewById(buttonView.getId() + 1);
                        imageView.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    private void HandleImageViewButtons() {
        imageViewButtons = new ImageView[20];
        for (int i = 0; i < 20; i++) {
            int id = getResources().getIdentifier("ac_prob" + (i + 1) + "_image", "id", rootView.getContext().getPackageName());
            imageViewButtons[i] = (ImageView) rootView.findViewById(id);
            imageViewButtons[i].setVisibility(View.INVISIBLE);
            imageViewButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // 创建一个文件，用来存储相片
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image

                    // 如果没有存储设备，就不拍照了
                    if(fileUri == null) {
                        Toast.makeText(rootView.getContext(), "没有检测到存储设备", Toast.LENGTH_LONG).show();
                        return;
                    }

                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileUri);

                    // 开始拍照
                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            });
        }
    }


    public void CollectData(View view) {
        Intent intent = new Intent(rootView.getContext(), CarCheckCollectDataActivity.class);
        startActivity(intent);
    }

    public void ChooseGlass(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
        LayoutInflater inflater = this.inflater;

        builder.setTitle(R.string.ac_glass);
        builder.setMultiChoiceItems(R.array.ac_glass_item, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            glassArrayIndex.add(which);
                        } else if (glassArrayIndex.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            glassArrayIndex.remove(Integer.valueOf(which));
                        }
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 确定
                String[] glassArray = rootView.getResources().getStringArray(R.array.ac_glass_item);
                String glassText = "";

                for(int j = 0; j < glassArrayIndex.size(); j++) {
                    glassText += glassArray[glassArrayIndex.get(j)];
                    glassText += "，";
                }

                if(glassText != "")
                    glassText = glassText.substring(0, glassText.length() - 1);

                EditText editText = (EditText)rootView.findViewById(R.id.ac_glass_edit);
                editText.setText(glassText);

                glassArrayIndex.clear();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void ChooseScrew(View view) {
        Intent intent = new Intent(rootView.getContext(), PopupActivity.class);
        intent.putExtra("POPUP_TYPE", "OUT_SCREW");
        startActivityForResult(intent, Common.CHOOSE_OUT_SCREW);
    }

    public void ac_start_camera(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

        // Get the layout inflater
        LayoutInflater inflater = this.inflater;

        builder.setTitle(R.string.ac_camera);
        builder.setItems(R.array.ac_camera_cato_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentGroup = i;
                String group = getResources().getStringArray(R.array.ac_camera_cato_item)[currentGroup];

                Toast.makeText(rootView.getContext(), "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, Common.PHOTO_FOR_ENGINE_GROUP);
            }
        });
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Common.CHOOSE_OUT_SCREW:
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String brokenPart = bundle.getString(Common.OUT_SCREW_RESULT);
                            if(brokenPart != null) {
                                EditText editText = (EditText)rootView.findViewById(R.id.ac_screw_edit);
                                editText.setText(brokenPart);
                            }
                        }
                    }
                    catch(NullPointerException ex) {
                        Log.w("CHOOSE_OUT_SCREW", "choosed nothing..\n");
                    }
                }
                break;
            case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    // 照片成功拍摄，并存储成功
                    Toast.makeText(rootView.getContext(), "拍摄成功！", Toast.LENGTH_LONG).show();

                    // TODO 在这里应该拿到照片并做相应处理（也许不用？？）
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // 用户取消照片拍摄
                } else {
                    // 拍摄照片失败
                }

                break;
            case Common.PHOTO_FOR_ENGINE_GROUP:
                if(resultCode == Activity.RESULT_OK) {
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    //img.setImageBitmap(image);
                } else {
                    Toast.makeText(rootView.getContext(),"error occured during opening camera", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public static final int MEDIA_TYPE_IMAGE = 1;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // 先检查是否有可存储设备
        if( Environment.getExternalStorageState() == null) {
            Log.d("DFCarChecker", "没有存储设备！！！");
            return null;
        }

        // 选取存储设备上的存储路径 TODO 可修改
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "DFCarChecker");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d("DFCarChecker", "创建目录失败！！！");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}

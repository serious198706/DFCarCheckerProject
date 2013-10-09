package com.df.dfcarchecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.df.service.Common;

public class CarCheckBasicInfoFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;
    private int currentGroup;
    private ImageView img;
    private TableLayout tableLayout;
    private LinearLayout brand;
    private EditText vin_edit;
    private Button brandOkButton;
    private Button brandSelectButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.fragment_car_check_basic_info, container, false);

        img = (ImageView) rootView.findViewById(R.id.image);

        tableLayout = (TableLayout) rootView.findViewById(R.id.bi_content_table);
        tableLayout.setVisibility(View.GONE);

        brand = (LinearLayout) rootView.findViewById(R.id.brand_input);

        Button button = (Button) rootView.findViewById(R.id.cbi_start_camera_button);
        button.setOnClickListener(this);

        Button vinButton = (Button) rootView.findViewById(R.id.bi_vin_button);
        vinButton.setOnClickListener(this);
        brandOkButton = (Button) rootView.findViewById(R.id.bi_brand_ok_button);
        brandOkButton.setEnabled(false);
        brandOkButton.setOnClickListener(this);
        brandSelectButton = (Button) rootView.findViewById(R.id.bi_brand_select_button);
        brandSelectButton.setEnabled(false);
        brandSelectButton.setOnClickListener(this);

        vin_edit = (EditText) rootView.findViewById(R.id.bi_vin_edit);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cbi_start_camera_button:
                cbi_start_camera(v);
                break;
            case R.id.bi_vin_button:
                bi_brand_show();
                break;
            case R.id.bi_brand_ok_button:
                bi_content_show();
        }
    }

    public void cbi_start_camera(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());

        // Get the layout inflater
        LayoutInflater inflater = this.inflater;

        builder.setTitle(R.string.cbi_camera);
        builder.setItems(R.array.cbi_camera_cato_item, new DialogInterface.OnClickListener() {
            // 点击某个组时，记录当前组别
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                currentGroup = i;
                String group = getResources().getStringArray(R.array.cbi_camera_cato_item)[currentGroup];

                Toast.makeText(rootView.getContext(), "正在拍摄" + group + "组", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, Common.PHOTO_FOR_OTHER_GROUP);
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

    private void bi_brand_show() {
        if(vin_edit.getText() != null) {
            brand.setVisibility(View.VISIBLE);

            // TODO: Get brand from server
            EditText brand_edit = (EditText) rootView.findViewById(R.id.bi_brand_edit);
            brand_edit.setText("大众甲壳虫 1.6 MT");
            brandOkButton.setEnabled(true);
            brandSelectButton.setEnabled(true);
        }
    }

    private void bi_content_show() {
        tableLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Common.PHOTO_FOR_OTHER_GROUP:
                if(resultCode == Activity.RESULT_OK) {
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    img.setImageBitmap(image);
                } else {
                    Toast.makeText(rootView.getContext(),
                            "error occured during opening camera", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }
    
}

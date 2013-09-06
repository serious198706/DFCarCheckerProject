package com.df.dfcarchecker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.net.NoRouteToHostException;
import java.util.ArrayList;
import java.util.List;

public class CarCheckAccidentFragment extends Fragment implements View.OnClickListener {
    private static View rootView;
    private LayoutInflater inflater;
    private List<Integer> glassArrayIndex;
    private List<Integer> screwArrayIndex;
    private List<Integer> switchButtonIDs;
    private List<Integer> imageButtonIDs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.inflater = inflater;
        rootView = inflater.inflate(R.layout.activity_car_check_accident, container, false);

        Button glassButton = (Button)rootView.findViewById(R.id.ac_choose_glass_button);
        glassButton.setOnClickListener(this);
        Button screwButton = (Button)rootView.findViewById(R.id.ac_choose_screw_button);
        screwButton.setOnClickListener(this);
        Button cameraButton = (Button) rootView.findViewById(R.id.ac_start_camera_button);
        cameraButton.setOnClickListener(this);

        glassArrayIndex = new ArrayList<Integer>();
        screwArrayIndex = new ArrayList<Integer>();
        switchButtonIDs = new ArrayList<Integer>();
        imageButtonIDs = new ArrayList<Integer>();

        for(int i = 0; i < 20; i++) {
            int id = 0x7f0a0004 + i * 2;
            switchButtonIDs.add(id);
            Switch switchButton = (Switch)rootView.findViewById(id);
            switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        ImageView imageView = (ImageView)rootView.findViewById(compoundButton.getId() + 1);
                        imageView.setVisibility(View.VISIBLE);
                    } else {
                        ImageView imageView = (ImageView)rootView.findViewById(compoundButton.getId() + 1);
                        imageView.setVisibility(View.INVISIBLE);
                    }

                }
            });
        }

        for(int i = 0; i < 20; i++) {
            int id = 0x7f0a0005 + i * 2;
            imageButtonIDs.add(id);
            ImageView imageView = (ImageView)rootView.findViewById(id);
            imageView.setVisibility(View.INVISIBLE);
        }



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
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
        LayoutInflater inflater = this.inflater;

        builder.setTitle(R.string.ac_screw);
        builder.setView(inflater.inflate(R.layout.screw_dialog, null));
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 取消
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 确定
                String[] glassArray = rootView.getResources().getStringArray(R.array.ac_screw_item);
                String screwText = "";

                for(int j = 0; j < screwArrayIndex.size(); j++) {
                    screwText += glassArray[screwArrayIndex.get(j)];
                    screwText += "，";
                }

                if(screwText != "")
                    screwText = screwText.substring(0, screwText.length() - 1);

                EditText editText = (EditText)rootView.findViewById(R.id.ac_screw_edit);
                editText.setText(screwText);

                screwArrayIndex.clear();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
                Toast.makeText(rootView.getContext(), String.format("%s", i), Toast.LENGTH_SHORT).show();
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
}

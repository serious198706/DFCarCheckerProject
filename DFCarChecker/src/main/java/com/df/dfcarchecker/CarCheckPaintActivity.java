package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.df.paintview.InsidePaintView;
import com.df.paintview.OutsidePaintView;
import com.df.paintview.StructurePaintView;
import com.df.service.Common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



public class CarCheckPaintActivity extends Activity {
    private LinearLayout root;
    private OutsidePaintView outsidePaintView;
    private InsidePaintView insidePaintView;
    private StructurePaintView structurePaintView;
    private String currentPaintView;

    public enum PaintType {
        STRUCTURE_PAINT, OUT_PAINT, IN_PAINT, NOVALUE;

        public static PaintType paintType(String str)
        {
            try {
                return valueOf(str);
            }
            catch (Exception ex) {
                return NOVALUE;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("PAINT_TYPE");

            switch (PaintType.paintType(value)) {
                case STRUCTURE_PAINT:
                    String sight = extras.getString("PAINT_SIGHT");
                    SetStructurePaintLayout(sight);
                    break;
                case OUT_PAINT:
                    SetOutPaintLayout();
                    break;
                case IN_PAINT:
                    SetInPaintLayout();
                    break;
            }

//            if(value.equals("OUT_PAINT")) {
//                SetOutPaintLayout();
//            } else if(value.equals("IN_PAINT")) {
//                SetInPaintLayout();
//            } else if(value.equals("STRUCTURE_PAINT")) {
//                String sight = extras.getString("PAINT_SIGHT");
//                SetStructurePaintLayout(sight);
//            }

        }

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void SetInPaintLayout() {
        setContentView(R.layout.activity_car_check_inside_paint);
        setTitle(R.string.in);

        insidePaintView = (InsidePaintView) findViewById(R.id.tile);

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.in_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int type = 0;

                switch (i) {
                    case R.id.in_dirty_radio:
                        type = Common.DIRTY;
                        break;
                    case R.id.in_broken_radio:
                        type = Common.BROKEN;
                        break;
                }

                insidePaintView.setType(type);
            }
        });

        currentPaintView = "IN_PAINT";
    }

    private void SetOutPaintLayout() {
        setContentView(R.layout.activity_car_check_outside_paint);
        setTitle(R.string.out);

        outsidePaintView = (OutsidePaintView) findViewById(R.id.tile);

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.out_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int type = 0;

                switch (i) {
                    case R.id.out_color_diff_radio:
                        type = Common.COLOR_DIFF;
                        break;
                    case R.id.out_scratch_radio:
                        type = Common.SCRATCH;
                        break;
                    case R.id.out_trans_radio:
                        type = Common.TRANS;
                        break;
                    case R.id.out_scrape_radio:
                        type = Common.SCRAPE;
                        break;
                }

                outsidePaintView.setType(type);
            }
        });

        currentPaintView = "OUT_PAINT";
    }


    private void SetStructurePaintLayout(String sight) {
        setContentView(R.layout.activity_car_check_structure_paint);
        setTitle(R.string.structure);
        structurePaintView = (StructurePaintView) findViewById(R.id.tile);

        if(sight.equals("FRONT")) {
            structurePaintView.init(CarCheckStructureFragment.previewBitmapFront, CarCheckStructureFragment.posEntitiesFront);
        } else {
            structurePaintView.init(CarCheckStructureFragment.previewBitmapRear, CarCheckStructureFragment.posEntitiesRear);
        }

        structurePaintView.setType(Common.COLOR_DIFF);

        currentPaintView = "STRUCTURE_PAINT";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.car_check_out_side_paint, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_done:
                // 提交数据

                finish();
                break;
            case R.id.action_cancel:
                if(currentPaintView.equals("OUT_PAINT")) {
                    outsidePaintView.cancel();
                } else if(currentPaintView.equals("IN_PAINT")) {
                    insidePaintView.cancel();
                } else if(currentPaintView.equals("STRUCTURE_PAINT")) {
                    structurePaintView.cancel();
                }
                finish();
                break;
            case R.id.action_clear:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.alert_title);
                builder.setMessage(R.string.out_clear_confirm);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 取消
                    }
                });
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 退出
                        if(currentPaintView.equals("OUT_PAINT")) {
                            outsidePaintView.clear();
                        } else if(currentPaintView.equals("IN_PAINT")) {
                            insidePaintView.clear();
                        } else if(currentPaintView.equals("STRUCTURE_PAINT")) {
                            structurePaintView.Clear();
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.action_undo:
                if(currentPaintView.equals("OUT_PAINT")) {
                    outsidePaintView.undo();
                } else if(currentPaintView.equals("IN_PAINT")) {
                    insidePaintView.undo();
                } else if(currentPaintView.equals("STRUCTURE_PAINT")) {
                    structurePaintView.Undo();
                }
                break;
            case R.id.action_redo:
                if(currentPaintView.equals("OUT_PAINT")) {
                    outsidePaintView.redo();
                } else if(currentPaintView.equals("IN_PAINT")) {
                    insidePaintView.redo();
                } else if(currentPaintView.equals("STRUCTURE_PAINT")) {
                    structurePaintView.Redo();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                if(data==null){
                    return;
                }

                String sdStatus = Environment.getExternalStorageState();

                // 检测sd是否可用
                if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(this, "SD卡不可用", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                if (bitmap == null) {
                    return;
                }

                File file = new File(Environment.getExternalStorageDirectory().getPath()+"/cyp/");
                file.mkdirs();// 创建文件夹
                String fileName = Environment.getExternalStorageDirectory().getPath()+"/cyp/"+System.currentTimeMillis()+".jpg";
                FileOutputStream b = null;
                try {
                    b = new FileOutputStream(fileName);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (b != null) {
                            b.flush();
                            b.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(currentPaintView.equals("OUT_PAINT")) {
                    Toast.makeText(this, "图片保存路径"+fileName, 0).show();
                    outsidePaintView.getPosEntity().setImage(fileName);
                } else if(currentPaintView.equals("IN_PAINT")) {
                    Toast.makeText(this, "图片保存路径"+fileName, 0).show();
                    insidePaintView.getPosEntity().setImage(fileName);
                } else if(currentPaintView.equals("STRUCTURE_PAINT")) {
                    Toast.makeText(this, "图片保存路径"+fileName, 0).show();
                    structurePaintView.getPosEntity().setImage(fileName);
                }

                break;
            case Activity.RESULT_CANCELED:
                break;
            default:
                Log.d("DFCarChecker", "拍摄故障！！");
                break;
        }
    }
}

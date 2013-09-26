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

import com.df.service.Common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CarCheckOutSidePaintActivity extends Activity {
    private LinearLayout root;
    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check_outside_paint);

        root = (LinearLayout) findViewById(R.id.titleLy);
        paintView = (PaintView) findViewById(R.id.tile);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

                paintView.setType(type);
            }
        });
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
                Toast.makeText(this, "W:" + Integer.toString(root.getWidth()) + ", " + "H:" + Integer.toString(root.getHeight()), Toast.LENGTH_LONG).show();
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
                        paintView.Clear();
                        paintView.invalidate();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
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
                Toast.makeText(this, "图片保存路径"+fileName, 0).show();
                paintView.getPosEntity().setImage(fileName);

                break;
            case Activity.RESULT_CANCELED:
                break;
            default:
                Log.d("DFCarChecker", "拍摄故障！！");
                break;
        }
    }
}

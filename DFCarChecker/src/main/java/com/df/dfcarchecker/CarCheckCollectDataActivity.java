package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.df.service.BluetoothDeviceListActivity;
import com.df.service.BluetoothService;
import com.df.service.CodeFormat;


public class CarCheckCollectDataActivity extends Activity {
    private EditText[] ets = null;
    private static final String TAG = "BluetoothChat";
    private static boolean D = true;

    // 为BluetoothService处理程序定义的消息类型
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // 从BluetoothService处理程序收到的键名字
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // 29个部位的名称
    private static String[] partOfName = { "右前翼子板", "引擎盖", "左前翼子板", "左A柱",
            "左前门", "左B柱", "左后门", "左C柱", "左后翼子板", "行李箱盖", "右后翼子板", "右C柱", "右后门",
            "右B柱", "右前门", "右A柱", "车顶", "右前减震器", "左前减震器", "右前翼子板内衬", "左前翼子板内衬",
            "右后门锁下方", "左后门锁下方", "右前纵梁", "左前纵梁", "水箱上支架", "钱防火墙", "引擎盖内侧",
            "行李箱盖内侧" };

    // private static final UUID MY_UUID = UUID
    // .fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Intent需要 编码
    public static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static final int MESSAGE_BACK_RESQUES = 10;
    public static String RESQUEST_STR;
    public static boolean HAS_RESQUEST_STR;
    // 布局控件
    // private TextView mTitle;
    // private EditText mOutEditText;
    private Button breakButton;

    // 名字的连接装置
    private String mConnectedDeviceName = null;
    // 传出消息的字符串缓冲区
    private StringBuffer mOutStringBuffer;
    // 蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter = null;
    // 蓝牙服务
    private BluetoothService mBluetoothService = null;
    // 设置标识符，选择用户接受的数据格式
    private boolean first;
    private boolean isBreak;
    // 第一次输入加入-->变量
    private int sum = 1;
    // private int UTF = 1;
    // 名社民党记录当创建服务器套接字
    String mmsg = "";
    private Thread t = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tooth);
        initET();
        breakButton = (Button) findViewById(R.id.no_2_2_18_btn_break);
        isBreak = false;
        D = false;
        first = true;

        // 得到当地的蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
            // 隐藏软键盘
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        // 初始化Socket
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_LONG)
                    .show();
            finish();
            return;
        }

        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, CarCheckFrameActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // 否则，设置聊天会话
        } else {
            if (mBluetoothService == null)
                setupChat();
        }
    }

    // 连接按键响应函数
    public void onConnectButtonClicked(View v) {

        if (breakButton.getText().equals("连接")
                || breakButton.getText().equals("connect")) {
            Intent serverIntent = new Intent(this, BluetoothDeviceListActivity.class); // 跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // 设置返回宏定义

        } else {
            // 关闭连接socket
            try {
                isBreak = false;
                // 关闭蓝牙
                breakButton.setText(R.string.button_break);
                mBluetoothService.stop();

            } catch (Exception e) {
            }
        }
        return;
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D)
            Log.e(TAG, "+ ON RESUME +");

        if (mBluetoothService != null) {
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                mBluetoothService.start();
            }
        }
    }

    private void setupChat() {

        // 初始化BluetoothService执行蓝牙连接
        mBluetoothService = new BluetoothService(this, mHandler);

        // 缓冲区初始化传出消息
        mOutStringBuffer = new StringBuffer("");
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.no_2_2_18_btn_break:
                onConnectButtonClicked(breakButton);
                break;
            case R.id.no_2_2_18_btn_cancle:
                mBluetoothService.stop();
                finish();
                break;
            case R.id.no_2_2_18_btn_really:

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBluetoothService != null)
            mBluetoothService.stop();

    }

    private void sendResquestMessage(String message) {
        // 检查我们实际上在任何连接
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // 检查实际上有东西寄到
        if (!TextUtils.isEmpty(message) && message.length() > 0) {
            // 得到消息字节和告诉BluetoothService写
            byte[] send = CodeFormat.hexStr2Bytes(message);
            mBluetoothService.write(send);

        }
    }

    // 处理程序，获取信息的BluetoothService回来
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            for (int i = 0; i < 17; i++) {
                                ets[i].setText("");
                            }
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            // mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                            break;
                        case BluetoothService.STATE_NONE:
                            // mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // 构建一个字符串缓冲区
                    String writeMessage = new String(writeBuf);
                    sum = 1;
                    mmsg += writeMessage;

                    break;
                case MESSAGE_READ:
                    String array[] = (String[]) msg.obj;
                    if (!TextUtils.isEmpty(array[1])
                            && !TextUtils.isEmpty(array[0])) {

                        int name = Integer.parseInt(array[0], 16);
                        if (partOfName.length >= name) {
                            ets[name - 1].append(array[1]);
                        }
                    }

                    break;
                case MESSAGE_DEVICE_NAME:
                    // 保存该连接装置的名字
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "已连接 " + mConnectedDeviceName, Toast.LENGTH_SHORT)
                            .show();
                    breakButton.setText(R.string.duankai);
                    if (!isBreak) {
                        if (t == null) {
                            t = new Thread() {
                                public void run() {
                                    try {
                                        sleep(700);
                                        Message msg = Message.obtain();
                                        msg.what = MESSAGE_BACK_RESQUES;
                                        sendMessage(msg);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                };
                            };
                            t.start();
                        }
                    }
                    isBreak = true;
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    HAS_RESQUEST_STR = false;
                    if (isBreak) {
                        t = null;
                        breakButton.performClick();
                    }
                    first = true;
                    t = null;
                    isBreak = false;
                    RESQUEST_STR = null;
                    break;
                case MESSAGE_BACK_RESQUES:
                    if (first) {
                        sendResquestMessage("aa057f012e");
                        first = false;
                    } else
                        sendResquestMessage(RESQUEST_STR);
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // 当devicelistactivity返回连接装置
                if (resultCode == Activity.RESULT_OK) {
                    // 获得设备地址
                    String address = data.getExtras().getString(
                            BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // 把蓝牙设备对象
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    // 试图连接到装置
                    mBluetoothService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // 当请求启用蓝牙返回
                if (resultCode == Activity.RESULT_OK) {
                    // 蓝牙已启用，所以建立一个聊天会话
                    setupChat();
                } else {
                    // 用户未启用蓝牙或发生错误
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    // 初始化17个EditText
    private void initET() {
        ets = new EditText[17];
        for (int i = 0; i < 17; i++) {
            int id = getResources().getIdentifier("no_2_2_" + (i + 1) + "_et", "id", getPackageName());
            ets[i] = (EditText) findViewById(id);

        }
    }
}
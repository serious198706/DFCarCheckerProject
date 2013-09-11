package com.df.dfcarchecker;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
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
    public static final int MESSAGE_READ_OVER = 6;
    public static final int MESSAGE_GET_SERIAL = 10;

    // 从BluetoothService处理程序收到的键名字
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // 29个部位的名称
    private static String[] partOfName;

    // 提供给系统蓝牙activity的标志值
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    // 要写入设备的字符串
    public static String requestCmd;

    public static boolean hasRequestCmd;
    public static String getSerialCmd = "aa057f012e";

    // 蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter = null;

    // 蓝牙服务
    private BluetoothService mBluetoothService = null;

    // 是否需要获取序列号
    private boolean hasSerial;
    private boolean isConnected;

    // 当前是否可以读取
    private boolean canRead;

    // 名社民党记录当创建服务器套接字
    private Thread t = null;

    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_check_collect_data);

        initEditTextCtrls();

        isConnected = false;
        D = false;
        hasSerial = false;
        partOfName = getResources().getStringArray(R.array.ac_part_name);

        // set action bar
        final ActionBar actionBar = getActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // 使用默认蓝牙适配器
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
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.car_check_collect_data, menu);
        super.onCreateOptionsMenu(menu);

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_done:
                finish();
                break;
            case R.id.action_show_devices:
                onConnectButtonClicked();
                break;
            case R.id.action_transfer_data:
                clearEditTexts();
                sendRequestMessage(requestCmd);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UpdateShowDevicesMenu(boolean b) {
        MenuItem showDevicesMenuItem = menu.findItem(R.id.action_show_devices);

        if(b) {
            if(showDevicesMenuItem != null) {
                showDevicesMenuItem.setIcon(R.drawable.disconnect);
                showDevicesMenuItem.setTitle(R.string.action_disconnect);
            }
        } else {
            if(showDevicesMenuItem != null) {
                showDevicesMenuItem.setIcon(R.drawable.show_device_list);
                showDevicesMenuItem.setTitle(R.string.action_connect);
            }
        }
    }

    private void UpdateTransferDataMenuItem(boolean b) {
        MenuItem transferDataMenuItem = menu.findItem(R.id.action_transfer_data);

        if(transferDataMenuItem != null)
            transferDataMenuItem.setVisible(b);
    }

    private void EnableMenuItem(int id, boolean b) {
        MenuItem item = menu.findItem(id);
        item.setEnabled(b);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else {
            if (mBluetoothService == null)
                setupChat();
        }
    }


    public void onConnectButtonClicked() {
        if(!isConnected) {
            Intent serverIntent = new Intent(this, BluetoothDeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        } else {
            isConnected = false;
            UpdateShowDevicesMenu(false);
            UpdateTransferDataMenuItem(false);

            try {
                // 关闭蓝牙
                mBluetoothService.stop();
            } catch (Exception e) {
                if (D)
                    Log.d(TAG, "bluetooth stop() failed");
            }
        }
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

    // 初始化BluetoothService
    private void setupChat() {
        mBluetoothService = new BluetoothService(this, mHandler);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
    }

    private void sendRequestMessage(String message) {
        // 是否已经连接到设备
        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // 发送数据
        if (!TextUtils.isEmpty(message) && message.length() > 0) {
            byte[] send = CodeFormat.hexStr2Bytes(message);

            // 向设备写数据
            mBluetoothService.write(send);
            EnableMenuItem(R.id.action_show_devices, false);
            EnableMenuItem(R.id.action_transfer_data, false);
        }
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 状态发生改变
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            clearEditTexts();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            break;
                        case BluetoothService.STATE_LISTEN:
                            break;
                        case BluetoothService.STATE_NONE:
                            break;
                        case BluetoothService.STATE_CONNECTION_LOST:
                            isConnected = false;
                            UpdateShowDevicesMenu(false);
                            UpdateTransferDataMenuItem(false);
                            // 关闭蓝牙
                            mBluetoothService.stop();
                            break;
                    }
                    break;
                // 写数据
                case MESSAGE_WRITE:
                    break;
                // 读数据
                case MESSAGE_READ:
                    String array[] = (String[]) msg.obj;

                    // 将接受的数据填入EditText
                    if (!TextUtils.isEmpty(array[1]) && !TextUtils.isEmpty(array[0])) {
                        int name = Integer.parseInt(array[0], 16);
                        if (partOfName.length >= name) {
                            ets[name - 1].setText(array[1]);
                        }
                    }
                    break;
                // 数据读取完毕
                case MESSAGE_READ_OVER:
                    Toast.makeText(getApplicationContext(), "数据传输完毕", Toast.LENGTH_SHORT).show();

                    EnableMenuItem(R.id.action_show_devices, true);
                    EnableMenuItem(R.id.action_transfer_data, true);
                    break;
                // 获取设备名称
                case MESSAGE_DEVICE_NAME:
                    // 保存设备的名称
                    String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "已连接 " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    UpdateShowDevicesMenu(true);
                    UpdateTransferDataMenuItem(true);
                    EnableMenuItem(R.id.action_show_devices, true);
                    EnableMenuItem(R.id.action_transfer_data, true);

                    if (!isConnected) {
                        if (t == null) {
                            t = new Thread() {
                                public void run() {
                                    try {
                                        sleep(700);
                                        Message msg = Message.obtain();
                                        msg.what = MESSAGE_GET_SERIAL;
                                        sendMessage(msg);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                }
                            };
                            t.start();
                        }
                    }

                    isConnected = true;
                    break;
                // 显示失败信息
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();

                    hasRequestCmd = false;
                    hasSerial = false;
                    t = null;
                    isConnected = false;
                    UpdateTransferDataMenuItem(false);
                    UpdateShowDevicesMenu(false);
                    EnableMenuItem(R.id.action_show_devices, true);
                    EnableMenuItem(R.id.action_transfer_data, true);
                    break;
                // 获取序列号
                case MESSAGE_GET_SERIAL:
                    if (!hasSerial) {
                        sendRequestMessage(getSerialCmd);
                        hasSerial = true;
                    }
                    // 已经获取了序列号
                    else {
                        UpdateShowDevicesMenu(true);
                        UpdateTransferDataMenuItem(true);
                        EnableMenuItem(R.id.action_show_devices, true);
                        EnableMenuItem(R.id.action_transfer_data, true);
                    }

                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // 查找成功
                if (resultCode == Activity.RESULT_OK) {
                    try{
                        Bundle bundle = data.getExtras();
                        if(bundle != null) {
                            String address = bundle.getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
                            if(address != null) {
                                // 根据设备地址连接设备
                                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                                mBluetoothService.connect(device);
                            }
                        }
                    }
                    catch(NullPointerException ex) {
                        if (D)
                            Log.d(TAG, "address error");
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                // 如果允许打开蓝牙
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                }
                // 不允许打开蓝牙（真是有病。。）
                else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    // 初始化所有EditText
    private void initEditTextCtrls() {
        ets = new EditText[17];
        for (int i = 0; i < 17; i++) {
            int id = getResources().getIdentifier("ac_area_" + (i + 1) + "_edit", "id", getPackageName());
            ets[i] = (EditText) findViewById(id);
        }
    }

    // 清除所有EditText的数据
    private void clearEditTexts() {
        for (int i = 0; i < 17; i++) {
            ets[i].setText("");
        }
    }
}
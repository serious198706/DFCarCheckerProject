package com.df.service;

import android.util.SparseArray;

import com.df.dfcarchecker.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by 岩 on 13-9-11.
 */
public class Common {
    // 启动Activity时的查询代码
    public static final int CHOOSE_OUT_GLASS = 1;
    public static final int CHOOSE_OUT_SCREW = 2;
    public static final int CHOOSE_OUT_BROKEN = 3;
    public static final int CHOOSE_IN_BROKEN = 4;
    public static final int CHOOSE_IN_DIRTY = 5;
    public static final int IT_OUT = 6;
    public static final int IT_IN = 7;
    public static final int IT_CHASSIS = 8;
    public static final int IT_WATER = 9;
    public static final int EX_PAINT = 10;
    public static final int IN_PAINT = 11;
    public static final int STURCTURE_PAINT = 12;

    // 用来获取Activity结果的查询代码
    public static final String OUT_GLASS_RESULT = "OUT_GLASS_RESULT";
    public static final String OUT_SCREW_RESULT = "OUT_SCREW_RESULT";
    public static final String OUT_BROKEN_RESULT = "OUT_BROKEN_RESULT";
    public static final String IN_BROKEN_RESULT = "IN_BROKEN_RESULT";
    public static final String IN_DIRTY_RESULT = "IN_DIRTY_RESULT";
    public static final String IT_OUT_RESULT = "IT_OUT_RESULT";
    public static final String IT_IN_RESULT = "IT_IN_RESULT";
    public static final String IT_CHASSIS_RESULT = "IT_CHASSIS_RESULT";
    public static final String IT_WATER_RESULT = "IT_WATER_RESULT";
    public static final String IT_TIRE_RESULT = "IT_TIRE_RESULT";

    // 拍摄组别代码
    public static final int PHOTO_FOR_OTHER_GROUP = 0;
    public static final int PHOTO_FOR_ENGINE_GROUP = 1;
    public static final int PHOTO_FOR_OUTSIDE_GROUP = 2;
    public static final int PHOTO_FOR_INSIDE_GROUP = 3;
    public static final int PHOTO_FOR_STRUCTURE_GROUP = 4;

    // 绘图类型代码
    public static final int COLOR_DIFF = 1; // 色差
    public static final int SCRATCH = 2;    // 划痕
    public static final int TRANS = 3;      // 变形
    public static final int SCRAPE = 4;     // 刮蹭
    public static final int OTHER = 5;      // 其它
    public static final int DIRTY = 6;      // 脏污
    public static final int BROKEN = 7;     // 破损

    // 每个组最少拍摄张数
    public static final int[] photoLeastCount = {1, 1, 1};

    // 日志的TAG
    public static final String TAG = "DFCarChecker";

    // WebService地址
    //public static final String SERVER_ADDRESS = "http://192.168.8.33:801/";
    //public static final String SERVER_ADDRESS = "http://wcf.268v.com:8008/";
    public static final String SERVER_ADDRESS = "http://192.168.100.6:50/";

    public static final String USER_MANAGE_SERVICE = "userManageService.svc";
    public static final String REPORT_SERVICE = "ReportService.svc";

    // 图片地址
    //public static final String PICUTRE_ADDRESS = "http://i.268v.com/";
    public static final String PICUTRE_ADDRESS = "http://192.168.100.6:8006/";


    public static final int[][] carSettingsSpinnerMap = {
            {R.id.csi_driveType_spinner, 0, R.array.csi_driveType_item},
            {R.id.csi_transmission_spinner, 0, R.array.csi_transmission_item},
            {R.id.csi_airbag_spinner, R.id.it_airBag_spinner, R.array.csi_airbag_number},
            {R.id.csi_abs_spinner, R.id.it_abs_spinner, R.array.existornot},
            {R.id.csi_powerSteering_spinner, 0, R.array.existornot},
            {R.id.csi_powerWindows_spinner, R.id.it_powerWindows_spinner, R.array.csi_powerWindows_items},
            {R.id.csi_sunroof_spinner, R.id.it_sunroof_spinner, R.array.csi_sunroof_items},
            {R.id.csi_airConditioning_spinner, R.id.it_airConditioning_spinner, R.array.csi_airConditioning_items},
            {R.id.csi_leatherSeats_spinner, 0, R.array.csi_leatherSeats_items},
            {R.id.csi_powerSeats_spinner, R.id.it_powerSeats_spinner, R.array.csi_powerSeats_items},
            {R.id.csi_powerMirror_spinner, R.id.it_powerMirror_spinner, R.array.csi_powerMirror_items},
            {R.id.csi_reversingRadar_spinner, R.id.it_reversingRadar_spinner, R.array.csi_reversingRadar_items},
            {R.id.csi_reversingCamera_spinner, R.id.it_reversingCamera_spinner, R.array.csi_reversingCamera_items},
            {R.id.csi_ccs_spinner, 0, R.array.csi_ccs_items},
            {R.id.csi_softCloseDoors_spinner, R.id.it_softCloseDoors_spinner, R.array.existornot},
            {R.id.csi_rearPowerSeats_spinner, R.id.it_rearPowerSeats_spinner, R.array.existornot},
            {R.id.csi_ahc_spinner, R.id.it_ahc_spinner, R.array.existornot},
            {R.id.csi_parkAssist_spinner, R.id.it_parkAssist_spinner, R.array.existornot},
            {R.id.csi_clapboard_spinner, 0, R.array.existornot}
    };

    public static final SparseArray<String> carSettingsSpinnerStringSparseArray = new
            SparseArray<String>() {{
        put(R.id.csi_airbag_spinner, "airBags");
        put(R.id.csi_abs_spinner, "abs");
        put(R.id.csi_powerSteering_spinner, "powerSteering");
        put(R.id.csi_powerWindows_spinner, "powerWindows");
        put(R.id.csi_sunroof_spinner, "sunroof");
        put(R.id.csi_airConditioning_spinner, "airConditioning");
        put(R.id.csi_leatherSeats_spinner, "leatherSeats");
        put(R.id.csi_powerSeats_spinner, "powerSeats");
        put(R.id.csi_powerMirror_spinner, "powerMirror");
        put(R.id.csi_reversingRadar_spinner, "reversingRadar");
        put(R.id.csi_reversingCamera_spinner, "reversingCamera");
        put(R.id.csi_ccs_spinner, "ccs");
        put(R.id.csi_softCloseDoors_spinner, "softCloseDoors");
        put(R.id.csi_rearPowerSeats_spinner, "rearPowerSeats");
        put(R.id.csi_ahc_spinner, "ahc");
        put(R.id.csi_parkAssist_spinner, "parkAssist");
        put(R.id.csi_clapboard_spinner, "clapBoard");
    }};
}

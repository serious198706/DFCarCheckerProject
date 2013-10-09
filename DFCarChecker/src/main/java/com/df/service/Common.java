package com.df.service;

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
    public static final int IT_WATER = 6;
    public static final int IT_TIRE = 7;
    public static final int IT_FIRE = 8;
    public static final int IT_ELECTRICITY = 9;
    public static final int OUT_PAINT = 10;
    public static final int IN_PAINT = 11;
    public static final int STURCTURE_PAINT = 12;

    // 用来获取Activity结果的查询代码
    public static final String OUT_GLASS_RESULT = "OUT_GLASS_RESULT";
    public static final String OUT_SCREW_RESULT = "OUT_SCREW_RESULT";
    public static final String OUT_BROKEN_RESULT = "OUT_BROKEN_RESULT";
    public static final String IN_BROKEN_RESULT = "IN_BROKEN_RESULT";
    public static final String IN_DIRTY_RESULT = "IN_DIRTY_RESULT";
    public static final String IT_WATER_RESULT = "IT_WATER_RESULT";
    public static final String IT_TIRE_RESULT = "IT_TIRE_RESULT";
    public static final String IT_FIRE_RESULT = "IT_FIRE_RESULT";
    public static final String IT_ELECTRICITY_RESULT = "IT_ELECTRICITY_RESULT";

    // 拍摄组别代码
    public static final int PHOTO_FOR_OTHER_GROUP = 0;
    public static final int PHOTO_FOR_ENGINE_GROUP = 1;
    public static final int PHOTO_FOR_OUTSIDE_GROUP = 2;
    public static final int PHOTO_FOR_INSIDE_GROUP = 3;
    public static final int PHOTO_FOR_STRUCTURE_GROUP = 4;

    // 绘图类型代码
    public static final int COLOR_DIFF = 1;
    public static final int SCRATCH = 2;
    public static final int TRANS = 3;
    public static final int SCRAPE = 4;
    public static final int DIRTY = 5;
    public static final int BROKEN = 6;

    // TODO 加入事故检查绘图类型代码

}

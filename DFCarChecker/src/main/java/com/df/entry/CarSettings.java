package com.df.entry;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by 岩 on 13-10-16.
 */
public class CarSettings {
    private Country country;
    private Brand brand;
    private Manufacturer manufacturer;
    private Series series;

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    private Model model;

    private String brandString;
    private String displacement;
    private String category;
    private String driveType;
    private String transmission;
    private String airbag;
    private String abs;
    private String powerSteering;
    private String powerWindows;
    private String sunroof;
    private String airConditioning;
    private String leatherSeats;
    private String powerSeats;
    private String powerMirror;
    private String reversingRadar;
    private String reversingCamera;
    private String ccs;
    private String softCloseDoors;
    private String rearPowerSeats;
    private String ahc;
    private String parkAssist;
    private String clapBoard;

    // 1：三厢四门车、2：三厢两门车、3：两厢两门车、4：两厢四门车:、5：面包车

    // inside:      1 - d4s4,       2 - d2s4,       3 - d2s4,       4 - d4s4,       5 - van_i
    // outside:     1 - r3d4,       2 - r3d2,       3 - r2d2,       4 - r2d4,       5 - van_o
    // structure:   1 - d4_f/d4_r,  2 - d2_f/d2_r,  3 - d2_f/d2_r,  4 - d4_f/d4_r,  5 - d4_f/d4_r
    private String figure;

    private String exist = "有";
    private String notExist = "无";

    public CarSettings() {
        brandString = "";
        displacement = "";
        category = "";
        figure = "";
        driveType = "";
        transmission = "";
        airbag = "";
        abs = "";
        powerSteering = "";
        powerWindows = "";
        sunroof = "";
        airConditioning = "";
        leatherSeats = "";
        powerSeats = "";
        powerMirror = "";
        reversingRadar = "";
        reversingCamera = "";
        ccs = "";
        softCloseDoors = "";
        rearPowerSeats = "";
        ahc = "";
        parkAssist = "";
        clapBoard = "";
    }

    private void clearSettings() {
        airbag = "";
        abs = "";
        powerSteering = "";
        powerWindows = "";
        sunroof = "";
        airConditioning = "";
        leatherSeats = "";
        powerSeats = "";
        powerMirror = "";
        reversingRadar = "";
        reversingCamera = "";
        ccs = "";
        softCloseDoors = "";
        rearPowerSeats = "";
        ahc = "";
        parkAssist = "";
        clapBoard = "";
        figure = "";
    }

    // 厂牌型号
    public void setBrandString(String brandString) {
        this.brandString = brandString;
    }
    public String getBrandString() {
        return brandString;
    }

    // 排量
    public void setDisplacement(String displacement) {
        this.displacement = displacement;
    }
    public String getDisplacement() {
        return displacement;
    }

    // 车辆类型
    public void setCategory(String category) {
        this.category = category;
    }
    public String getCategory() {
        return category;
    }

    // 驱动方式
    public void setDriveType(String driveType) {
        this.driveType = driveType;
    }
    public String getDriveType() {
        if(driveType.equals("四驱"))
            return "1";
        else
            return "0";
    }

    public String getFigure() {
        return figure;
    }

    public void setFigure(String figure) {
        this.figure = figure;
    }

    // 变速器形式
    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }
    public String getTransmission() {
        if(transmission.equals("MT"))
            return "1";
        else if(transmission.equals("AMT"))
            return "2";
        else if(transmission.equals("A/MT"))
            return "3";
        else if(transmission.equals("CVT"))
            return "4";
        else
            return "0";
    }
    public String getTransmissionText() {
        return transmission;
    }

    // 气囊
    public void setAirbag(String airbag) {
        this.airbag = airbag;
    }
    public String getAirbag() {
        if(!airbag.equals(notExist))
            return "0";
        else
            return "5";
    }

    // ABS
    public void setAbs(String abs) {
        this.abs = abs;
    }
    public String getAbs() {
        if(abs.equals(exist))
            return "0";
        else
            return "1";
    }

    // 转向助力
    public void setPowerSteering(String powerSteering) {
        this.powerSteering = powerSteering;
    }
    public String getPowerSteering() {
        if(powerSteering.equals(exist))
            return "0";
        else
            return "1";
    }

    // 电动车窗
    public void setPowerWindows(String powerWindows) {
        this.powerWindows = powerWindows;
    }
    public String getPowerWindows() {
        if(powerWindows.equals(exist))
            return "0";
        else
            return "1";
    }

    // 天窗
    public void setSunroof(String sunroof) {
        this.sunroof = sunroof;
    }
    public String getSunroof() {
        if(sunroof.equals(exist))
            return "0";
        else
            return "1";
    }

    // 空调
    public void setAirConditioning(String airConditioning) {
        this.airConditioning = airConditioning;
    }
    public String getAirConditioning() {
        if(airConditioning.equals(exist))
            return "0";
        else
            return "1";
    }

    // 真皮座椅
    public void setLeatherSeats(String leatherSeats) {
        this.leatherSeats = leatherSeats;
    }
    public String getLeatherSeats() {
        if(leatherSeats.equals(exist))
            return "0";
        else
            return "1";
    }

    // 电动座椅
    public void setPowerSeats(String powerSeats) {
        this.powerSeats = powerSeats;
    }
    public String getPowerSeats() {
        if(powerSeats.equals(exist))
            return "0";
        else
            return "1";
    }

    // 电动反光镜
    public void setPowerMirror(String powerMirror) {
        this.powerMirror = powerMirror;
    }
    public String getPowerMirror() {
        if(powerMirror.equals(exist))
            return "0";
        else
            return "1";
    }

    // 倒车雷达
    public void setReversingRadar(String reversingRadar) {
        this.reversingRadar = reversingRadar;
    }
    public String getReversingRadar() {
        if(reversingRadar.equals(exist))
            return "0";
        else
            return "1";
    }

    // 倒车影像
    public void setReversingCamera(String reversingCamera) {
        this.reversingCamera = reversingCamera;
    }
    public String getReversingCamera() {
        if(reversingCamera.equals(exist))
            return "0";
        else
            return "1";
    }

    // 定速巡航
    public void setCcs(String ccs) {
        this.ccs = ccs;
    }
    public String getCcs() {
        if(ccs.equals(exist))
            return "0";
        else
            return "1";
    }

    // 电吸门
    public void setSoftCloseDoors(String softCloseDoors) {
        this.softCloseDoors = softCloseDoors;
    }
    public String getSoftCloseDoors() {
        if(softCloseDoors.equals(exist))
            return "0";
        else
            return "1";
    }

    // 后排电动座椅
    public void setRearPowerSeats(String rearPowerSeats) {
        this.rearPowerSeats = rearPowerSeats;
    }
    public String getRearPowerSeats() {
        if(rearPowerSeats.equals(exist))
            return "0";
        else
            return "1";
    }

    // 底盘升降
    public void setAhc(String ahc) {
        this.ahc = ahc;
    }
    public String getAhc() {
        if(ahc.equals(exist))
            return "0";
        else
            return "1";
    }

    // 自动泊车
    public void setParkAssist(String parkAssist) {
        this.parkAssist = parkAssist;
    }
    public String getParkAssist() {
        if(parkAssist.equals(exist))
            return "0";
        else
            return "1";
    }

    // 隔物帘
    public void setClapBoard(String clapBoard) {
        this.clapBoard = clapBoard;
    }
    public String getClapBoard() {
        if(clapBoard.equals(exist))
            return "0";
        else
            return "1";
    }

    // 通过Json数据填写各成员变量
    public void setCarSettings(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            brandString = "一汽奥迪 100 1.6 MT";
            displacement = jsonObject.getString("displacement");
            category = jsonObject.getString("category");
            driveType = jsonObject.getString("driveType");
            transmission = jsonObject.getString("transmission");
            airbag = jsonObject.getString("airBags");
            abs = jsonObject.getString("abs");
            powerSteering = jsonObject.getString("powerSteering");
            powerWindows = jsonObject.getString("powerWindows");
            sunroof = jsonObject.getString("sunroof");
            airConditioning = jsonObject.getString("airConditioning");
            leatherSeats = jsonObject.getString("leatherSeats");
            powerSeats = jsonObject.getString("powerSeats");
            powerMirror = jsonObject.getString("powerMirror");
            reversingRadar = jsonObject.getString("reversingRadar");
            reversingCamera = jsonObject.getString("reversingCamera");
            ccs = jsonObject.getString("ccs");
            softCloseDoors = jsonObject.getString("softCloseDoors");
            rearPowerSeats = jsonObject.getString("rearPowerSeats");
            ahc = jsonObject.getString("ahc");
            parkAssist = jsonObject.getString("parkAssist");
            clapBoard = jsonObject.getString("clapBoard");

        } catch (Exception e) {
            Log.d("DFCarChecker", "Json Error: " + e.getMessage());
        }
    }

    public void setConfig(String config) {
        // 在每次设置配置信息时，要先将之前设置的配置信息清除
        clearSettings();

        String[] configs = config.split(",");

        String exist = "有";

        for(int i = 0; i < configs.length; i++) {
            if(configs[i].equals("category")) {
                setCategory(configs[i]);
            } else if(configs[i].equals("figure")) {
                setFigure(configs[i]);
            } else if(configs[i].equals("airBags")) {
                setAirbag(exist);
            } else if(configs[i].equals("abs")) {
                setAbs(exist);
            } else if(configs[i].equals("powerSteering")) {
                setPowerSteering(exist);
            } else if(configs[i].equals("powerWindows")) {
                setPowerWindows(exist);
            } else if(configs[i].equals("sunroof")) {
                setSunroof(exist);
            } else if(configs[i].equals("airConditioning")) {
                setAirConditioning(exist);
            } else if(configs[i].equals("leatherSeats")) {
                setLeatherSeats(exist);
            } else if(configs[i].equals("powerSeats")) {
                setPowerSeats(exist);
            } else if(configs[i].equals("powerMirror")) {
                setPowerMirror(exist);
            } else if(configs[i].equals("reversingRadar")) {
                setReversingRadar(exist);
            } else if(configs[i].equals("reversingCamera")) {
                setReversingCamera(exist);
            } else if(configs[i].equals("ccs")) {
                setCcs(exist);
            } else if(configs[i].equals("softCloseDoors")) {
                setSoftCloseDoors(exist);
            } else if(configs[i].equals("rearPowerSeats")) {
                setRearPowerSeats(exist);
            } else if(configs[i].equals("ahc")) {
                setAhc(exist);
            } else if(configs[i].equals("parkAssist")) {
                setParkAssist(exist);
            } else if(configs[i].equals("clapBoard")) {
                setClapBoard(exist);
            }
        }
    }

    public String getCarConfigs() {
        return getDriveType() + "," +
                getTransmission() + "," +
                getAirbag() + "," +
                getAbs() + "," +
                getPowerSteering() + "," +
                getPowerWindows() + "," +
                getSunroof() + "," +
                getAirConditioning() + "," +
                getLeatherSeats() + "," +
                getPowerSeats() + "," +
                getPowerMirror() + "," +
                getReversingRadar() + "," +
                getReversingCamera() + "," +
                getCcs() + "," +
                getSoftCloseDoors() + "," +
                getRearPowerSeats() + "," +
                getAhc() + "," +
                getParkAssist() + "," +
                getClapBoard();
    }
}

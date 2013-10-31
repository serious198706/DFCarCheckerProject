package com.df.entry;

import android.util.Log;

import com.df.dfcarchecker.R;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by 岩 on 13-10-16.
 */
public class CarSettings {
    private String brand;
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

    private String exist = "有";

    public CarSettings() {
        brand = "";
        displacement = "";
        category = "";
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

    // 厂牌型号
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getBrand() {
        return brand;
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

    // 气囊
    public void setAirbag(String airbag) {
        this.airbag = airbag;
    }
    public String getAirbag() {
        if(airbag.equals(exist))
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

            brand = "一汽奥迪 100 1.6 MT";
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
        String[] configs = config.split(",");

        String exist = "有";

        for(int i = 0; i < configs.length; i++) {
            // TODO: 车辆类型什么时候传来？
            if(configs[i].equals("category")) {
                setCategory(configs[i]);
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
}

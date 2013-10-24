package com.df.entry;

import android.util.Log;

import org.json.JSONObject;

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
    private String clapboard;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDisplacement() {
        return displacement;
    }

    public void setDisplacement(String displacement) {
        this.displacement = displacement;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDriveType() {
        return driveType;
    }

    public void setDriveType(String driveType) {
        this.driveType = driveType;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getAirbag() {
        return airbag;
    }

    public void setAirbag(String airbag) {
        this.airbag = airbag;
    }

    public String getAbs() {
        return abs;
    }

    public void setAbs(String abs) {
        this.abs = abs;
    }

    public String getPowerSteering() {
        return powerSteering;
    }

    public void setPowerSteering(String powerSteering) {
        this.powerSteering = powerSteering;
    }

    public String getPowerWindows() {
        return powerWindows;
    }

    public void setPowerWindows(String powerWindows) {
        this.powerWindows = powerWindows;
    }

    public String getSunroof() {
        return sunroof;
    }

    public void setSunroof(String sunroof) {
        this.sunroof = sunroof;
    }

    public String getAirConditioning() {
        return airConditioning;
    }

    public void setAirConditioning(String airConditioning) {
        this.airConditioning = airConditioning;
    }

    public String getLeatherSeats() {
        return leatherSeats;
    }

    public void setLeatherSeats(String leatherSeats) {
        this.leatherSeats = leatherSeats;
    }

    public String getPowerSeats() {
        return powerSeats;
    }

    public void setPowerSeats(String powerSeats) {
        this.powerSeats = powerSeats;
    }

    public String getPowerMirror() {
        return powerMirror;
    }

    public void setPowerMirror(String powerMirror) {
        this.powerMirror = powerMirror;
    }

    public String getReversingRadar() {
        return reversingRadar;
    }

    public void setReversingRadar(String reversingRadar) {
        this.reversingRadar = reversingRadar;
    }

    public String getReversingCamera() {
        return reversingCamera;
    }

    public void setReversingCamera(String reversingCamera) {
        this.reversingCamera = reversingCamera;
    }

    public String getCcs() {
        return ccs;
    }

    public void setCcs(String ccs) {
        this.ccs = ccs;
    }

    public String getSoftCloseDoors() {
        return softCloseDoors;
    }

    public void setSoftCloseDoors(String softCloseDoors) {
        this.softCloseDoors = softCloseDoors;
    }

    public String getRearPowerSeats() {
        return rearPowerSeats;
    }

    public void setRearPowerSeats(String rearPowerSeats) {
        this.rearPowerSeats = rearPowerSeats;
    }

    public String getAhc() {
        return ahc;
    }

    public void setAhc(String ahc) {
        this.ahc = ahc;
    }

    public String getParkAssist() {
        return parkAssist;
    }

    public void setParkAssist(String parkAssist) {
        this.parkAssist = parkAssist;
    }

    public String getClapboard() {
        return clapboard;
    }

    public void setClapboard(String clapboard) {
        this.clapboard = clapboard;
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
            clapboard = jsonObject.getString("clapboard");

        } catch (Exception e) {
            Log.d("DFCarChecker", "Json Error: " + e.getMessage());
        }
    }
}

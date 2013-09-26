package com.df.service;

/**
 * Created by 岩 on 13-9-26.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;

public class PosEntity implements Serializable {

    private static final long serialVersionUID = 2019904101824903278L;

    private int start_x, start_y;
    private int end_x, end_y;

    private int max_x, max_y;

    private int type ;  //1色差2划痕3变型4刮蹭

    private String image;

    private Bitmap bitmap = null;

    public PosEntity(int type) {
        this.type = type;
    }

    public void setStart(int x,int y){
        this.start_x = x;
        this.start_y = y;
    }

    public void setEnd(int x,int y){
        this.end_x = x;
        this.end_y = y;
    }

    public int getStartX() {
        return start_x;
    }

    public int getStartY() {
        return start_y;
    }

    public int getEndX() {
        return end_x > max_x ? max_x : end_x;
    }

    public int getEndY() {
        return end_y > max_y ? max_y : end_y;
    }

    public int getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setMaxX(int max_x) {
        this.max_x = max_x;
    }

    public void setMaxY(int max_y) {
        this.max_y = max_y;
    }
}

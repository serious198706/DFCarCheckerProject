package com.df.service;

/**
 * Created by 岩 on 13-10-9.
 */
import android.content.Context;
import android.graphics.Bitmap;

import org.ksoap2.serialization.SoapObject;

public interface ISoapService {
    boolean login(Context context, String jsonString);
    boolean communicateWithServer(Context context, String jsonString);
    boolean uploadPicture(Context context, Bitmap bitmap, String jsonString);
    public boolean sendIpAddress();
}

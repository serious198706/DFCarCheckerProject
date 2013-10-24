package com.df.service;

/**
 * Created by 岩 on 13-10-9.
 */
import android.content.Context;
import android.graphics.Bitmap;

import org.ksoap2.serialization.SoapObject;

public interface ISoapService {
    String login(Context context, String jsonString);
    String communicateWithServer(Context context, String jsonString);
    String uploadPicture(Context context, Bitmap bitmap, String jsonString);
}

package com.df.service;

/**
 * Created by 岩 on 13-10-9.
 */
import android.content.Context;

import org.ksoap2.serialization.SoapObject;

public interface ISoapService {
    UserInfo login(Context context, String jsonString);
    String communicateWithServer(Context context, String jsonString);
}

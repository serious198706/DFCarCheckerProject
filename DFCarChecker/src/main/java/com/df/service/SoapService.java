package com.df.service;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.net.ConnectException;


public class SoapService implements ISoapService {
    private static final String NAMESPACE = "http://cheyiju";
    private String errorMessage;

    private String url;
    private String soapAction;
    private String methodName;

    public SoapService() {}

    public void setUtils(String url, String soapAction, String methodName) {
        this.url = url;
        this.soapAction = soapAction;
        this.methodName = methodName;
    }

    public String getErrorMessage() { return errorMessage; }

    public UserInfo login(Context context, String jsonString) {
        errorMessage = "";

        // 建立soap请求
        SoapObject request = new SoapObject(NAMESPACE, this.methodName);

        // 添加soap参数
        request.addProperty("inputStringJson", jsonString);

        // 建立信封
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // ??
        envelope.bodyOut = request;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        // 建立http连接
        HttpTransportSE trans = new HttpTransportSE(this.url);

        try {
            //发送请求
            trans.call(this.soapAction, envelope);
        } catch (Exception e) {
            Log.d("DFCarChecker", e.getMessage());

            errorMessage = "无法连接到服务器！";

            return null;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();

        // JSON格式数据
        String jsonResult = soapObject.getProperty(1).toString();

        UserInfo userinfo = new UserInfo();;

        // 成功
        if(result.equals("0")) {
            try {
                errorMessage = "";
                JSONObject jsonObject = new JSONObject(jsonResult);

                userinfo.setId(jsonObject.getString("UserId").toString());
                userinfo.setKey(jsonObject.getString("Key").toString());

            } catch (Exception e) {
                Log.d("DFCarChecker", e.getMessage());
            }
        }
        // 失败
        else {
            errorMessage = jsonResult;
            Log.d("DFCarChecker", jsonResult);
        }

        return userinfo;
    }

    public String communicateWithServer(Context context, String jsonString) {
        errorMessage = "";

        SoapObject request = new SoapObject(NAMESPACE, this.methodName);
        request.addProperty("inputStringJson", jsonString);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // ??
        envelope.bodyOut = request;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE trans = new HttpTransportSE(this.url);

        try {
            trans.call(this.soapAction, envelope);
        } catch (Exception e) {
            Log.d("DFCarChecker", "IOException");
            e.printStackTrace();
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();

        // JSON格式数据
        String jsonResult = soapObject.getProperty(1).toString();

        // 成功
        if(result.equals("0")) {
            return jsonResult;
        }
        // 失败
        else {
            errorMessage = jsonResult;
            Log.d("DFCarChecker", jsonResult);
        }

        return errorMessage;
    }
}

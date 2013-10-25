package com.df.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SoapService implements ISoapService {
    private static final String NAMESPACE = "http://cheyiju";

    // TODO: 建立一个错误信息的变量，一个正确信息的变量
    // TODO: 有错误时将正确信息变成""，然后调用者就可以通过判断返回值是否为""来决定是否取错误信息
    private boolean success;
    private String errorMessage;
    private String resultMessage;

    private String url;
    private String soapAction;
    private String methodName;

    private UserInfo userInfo;

    public SoapService() {}

    public void setUtils(String url, String soapAction, String methodName) {
        this.url = url;
        this.soapAction = soapAction;
        this.methodName = methodName;
    }

    public String getErrorMessage() { return errorMessage; }
    public String getResultMessage() { return resultMessage; }

    public UserInfo getUserInfo() { return userInfo; }

    public boolean login(Context context, String jsonString) {
        errorMessage = "";
        resultMessage = "";

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
            resultMessage = "";
            return false;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String flag = soapObject.getProperty(0).toString();

        // JSON格式数据
        resultMessage = soapObject.getProperty(1).toString();

        userInfo = new UserInfo();

        // 成功
        if(flag.equals("0")) {


            errorMessage = "";
            return true;
        }
        // 失败
        else {
            errorMessage = resultMessage;
            Log.d("DFCarChecker", resultMessage);
            return false;
        }
    }

    public boolean communicateWithServer(Context context, String jsonString) {
        errorMessage = "";
        resultMessage = "";

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
        String flag = soapObject.getProperty(0).toString();

        // JSON格式数据
        resultMessage = soapObject.getProperty(1).toString();

        // 成功
        if(flag.equals("0")) {
            errorMessage = "";
            return true;
        }
        // 失败
        else {
            errorMessage = resultMessage;
            Log.d("DFCarChecker", resultMessage);

            return false;
        }
    }

    public boolean uploadPicture(Context context, Bitmap bitmap, String jsonString) {
        errorMessage = "";
        resultMessage = "";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // 将图片转换成流
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        byte[] newByteArray = null;

        // 在图片流后面加上分隔符 #:
        jsonString = "#:" + jsonString;

        // 将图片流复制到新的byte数组中
        int length = byteArray.length;

        newByteArray = new byte[length + 2 + jsonString.length()];
        System.arraycopy(byteArray, 0, newByteArray, 0, length);

        for(int i = 0; i < jsonString.length(); i++) {
            newByteArray[length + i] = jsonString.getBytes()[i];
        }

        // 各种配置
        SoapObject request = new SoapObject(NAMESPACE, this.methodName);
        request.addProperty("stream", newByteArray);

        SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        new MarshalBase64().register(envelope);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE trans = new HttpTransportSE(this.url);

        try {
            trans.call(this.soapAction, envelope);
            Log.d("DFCarChecker", "upload Successful!");
        } catch (IOException e) {
            Log.d("DFCarChecker", "IOException");
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.d("DFCarChecker", "XmlPullParserException");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("DFCarChecker", e.getMessage());
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();
        Log.d("DFCarChecker", result);

        // JSON格式数据
        resultMessage = soapObject.getPropertySafely("SaveCarPictureTagKeyResult", "").toString();

        // 成功
        if(result.equals("0")) {
            // JSON格式数据            resultMessage = soapObject.getProperty(1).toString();

            return true;
        }
        // 失败
        else {
            Log.d("DFCarChecker", resultMessage);
            return false;
        }
    }
}

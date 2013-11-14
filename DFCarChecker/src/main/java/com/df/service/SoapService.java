package com.df.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.df.entry.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SoapService implements ISoapService {
    private static final String NAMESPACE = "http://cheyipai";

    private boolean success;
    private String errorMessage;
    private String resultMessage;

    private String url;
    private String soapAction;
    private String methodName;

    private UserInfo userInfo;

    public SoapService() {}

    // 设置url, soapAction, methodName
    public void setUtils(String url, String soapAction, String methodName) {
        this.url = url;
        this.soapAction = soapAction;
        this.methodName = methodName;
    }

    // 获取错误信息
    public String getErrorMessage() { return errorMessage; }

    // 获取结果信息
    public String getResultMessage() { return resultMessage; }

    // 获取用户信息
    public UserInfo getUserInfo() { return userInfo; }

    // 登录
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
        } catch (IOException e) {
            if(e.getMessage() != null)
                Log.d(Common.TAG, e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！");

            errorMessage = "无法连接到服务器！";
            resultMessage = "";
            return false;
        } catch (XmlPullParserException e) {

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
            Log.d(Common.TAG, resultMessage);
            return false;
        }
    }

    // 通讯，如提交信息等
    public boolean communicateWithServer(String jsonString) {
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
            if(e.getMessage() != null)
                Log.d(Common.TAG, "无法连接到服务器：" + e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！");

            errorMessage = "无法连接到服务器！";
            resultMessage = "";
            e.printStackTrace();

            return false;
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
            Log.d(Common.TAG, resultMessage);

            return false;
        }
    }

    // 上传照片
    public boolean uploadPicture(Bitmap bitmap, String jsonString) {
        errorMessage = "";
        resultMessage = "";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] byteArray = null;
        byte[] newByteArray;

        // 将图片转换成流
        // 有可能有的缺陷没有照片
        if(bitmap != null) {
            try {
                // 如果是草图，则要以PNG的方式压缩并上传
                JSONObject jsonObject = new JSONObject(jsonString);

                if(jsonObject.has("Part")) {
                    String part = jsonObject.getString("Part");

                    if(part.contains("ketch")) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 95, stream);
                    } else {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                    }
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                }
            } catch (JSONException e) {

            }

            byteArray = stream.toByteArray();
        }
        else {
            // 如果没有图片，那还传个毛线
            errorMessage = "图片为空！";
            return false;
        }

        // 在图片流后面加上分隔符 #:
        jsonString = "#:" + jsonString;

        // 将图片流复制到新的byte数组中
        int length = byteArray.length;

        newByteArray = new byte[length + jsonString.length()];
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
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.d(Common.TAG, "无法连接到服务器：" + e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！");

            errorMessage = "无法连接到服务器！";

            return false;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();

        // JSON格式数据
        resultMessage = soapObject.getPropertySafely("SaveCarPictureTagKeyResult", "").toString();

        // 成功
        if(result.equals("0")) {
            // JSON格式数据
            errorMessage = "";
            return true;
        }
        // 失败
        else {
            Log.d(Common.TAG, resultMessage);
            errorMessage = resultMessage;
            return false;
        }
    }

    // 上传空照片
    public boolean uploadPicture(String jsonString) {
        errorMessage = "";
        resultMessage = "";

        byte[] byteArray = new byte[6];
        byte[] newByteArray;

        for(int i = 0; i < byteArray.length; i++) {
            byteArray[i] = 'F';
        }

        // 在图片流后面加上分隔符 #:
        jsonString = "#:" + jsonString;

        // 将图片流复制到新的byte数组中
        int length = byteArray.length;

        newByteArray = new byte[length + jsonString.length()];
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
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.d(Common.TAG, "无法连接到服务器：" + e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！");

            errorMessage = "无法连接到服务器！";

            return false;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();

        // JSON格式数据
        resultMessage = soapObject.getPropertySafely("SaveCarPictureTagKeyResult", "").toString();

        // 成功
        if(result.equals("0")) {
            // JSON格式数据
            errorMessage = "";
            return true;
        }
        // 失败
        else {
            Log.d(Common.TAG, resultMessage);
            errorMessage = resultMessage;
            return false;
        }
    }

    // 检查更新
    public boolean checkUpdate(Context context) {
        errorMessage = "";
        resultMessage = "";

        // 各种配置
        SoapObject request = new SoapObject(NAMESPACE, this.methodName);

        SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
        new MarshalBase64().register(envelope);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE trans = new HttpTransportSE(this.url);

        try {
            trans.call(this.soapAction, envelope);
        } catch (Exception e) {
            if(e.getMessage() != null)
                Log.d(Common.TAG, "无法连接到服务器：" + e.getMessage());
            else
                Log.d(Common.TAG, "无法连接到服务器！" );

            errorMessage = "无法连接到服务器！";
            resultMessage = "";

            return false;
        }

        // 收到的结果
        SoapObject soapObject = (SoapObject) envelope.bodyIn;

        // 成功失败标志位
        String result = soapObject.getProperty(0).toString();

        // 成功
        if(!result.equals("")) {
            resultMessage = result;
            return true;
        }
        // 失败
        else {
            Log.d(Common.TAG, "获取版本失败！");
            return false;
        }
    }

}

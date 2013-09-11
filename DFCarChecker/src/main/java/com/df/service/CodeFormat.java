package com.df.service;

import android.util.Log;

import com.df.dfcarchecker.CarCheckCollectDataActivity;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;



public class CodeFormat {
    static String dataOne;
    private static final String TAG = "BluetoothChat";
    private static boolean D = true;
    private static boolean readOver = false;

    private static final String P_START = "AA0A";
    private static final String P_PARA = "0111";
    /*
     * 16进制数字字符集
     */
    private static String hexString = "0123456789ABCDEF";

    /*
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     */
    public static String encode(String str) {
        dataOne = str;
        // 根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0) + " ");
        }

        return sb.toString();

    }

    /*
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     */
    public static String decode(String bytes) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());

    }

    public static String[] parsePackage(String str) {
        int count = 0;

        // data[0]为部位名称、data[1] 为数值 、data[2] 为长度
        String data[] = new String[3];
        int len = str.length();

        if (!CarCheckCollectDataActivity.hasRequestCmd && len >= 14) {
            String requestCmd = P_START;

            String serialNumber = GetSerialNumber(str);
            requestCmd += serialNumber;
            requestCmd += P_PARA;

            String checkSum = GetCheckSum(requestCmd);

            if(checkSum.length() < 4) {
                // 填充零位
                for(int i = 0; i < 4 - checkSum.length(); i++) {
                    checkSum = "0" + checkSum;
                }
            }

            requestCmd += checkSum;

            CarCheckCollectDataActivity.requestCmd = requestCmd;

            return null;
        }

        if (str.startsWith("AA") || str.startsWith("aa")) {
            if (str.length() >= 14) {
                // data[0]指的是什么部位数据
                data[0] = str.substring(12, 14);

                StringBuffer str1 = new StringBuffer(str.substring(14, len - 4));
                StringBuffer str2 = new StringBuffer();

                // 如果部位数据长度大于4,表示为真正的数据
                if (str1.length() >= 4) {
                    int arraylen = str1.length() / 4;
                    String[] array = new String[arraylen];
                    for (int i = 0; i < arraylen; i++) {
                        array[i] = str1.substring(i * 4, i * 4 + 4);
                    }
                    for (int i = 0; i < arraylen; i++) {
                        int a = Integer.parseInt(array[i], 16);
                        if (a > 32768) {
                            a = (a - 32768) / 10;
                        } else {
                            a = a / 10;
                        }
                        count++;
                        str2.append(String.valueOf(a) + "  ");
                    }

                    data[1] = String.valueOf(str2);
                    data[2] = String.valueOf(count);
                }
                // 如果部位数据的小于4，表示此为完结包，可以通知主界面更新界面了
                else {
                    data[1] = "";
                    data[2] = "";
                }
                return data;
            }
            return data;

        }
        return null;
    }

    private static String GetSerialNumber(String str) {
        return str.substring(4, 12); // 00830009
    }

    private static String GetCheckSum(String str) {
        int serial = 0;

        for(int i = 0; i < str.length(); i+=2) {
            serial += Integer.parseInt(str.substring(i, i + 2), 16);
        }

        return  Integer.toHexString(serial);
    }


    /**
     * 　　* Convert byte[] to hex
     * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     *
     * 　　* @param src byte[] data
     *
     * 　　* @return hex string
     *
     *
     */
    public static byte[] hexStr2Bytes(String paramString) {
        String str = paramString.trim().replace(" ", "").toUpperCase(Locale.US);
        int i = str.length() / 2;
        byte[] arrayOfByte = new byte[i];
        for (int j = 0;; ++j) {
            if (j >= i)
                return arrayOfByte;
            int k = 1 + j * 2;
            int l = k + 1;
            arrayOfByte[j] = (byte) (0xFF & Integer.decode(
                    "0x" + str.substring(j * 2, k) + str.substring(k, l))
                    .intValue());
        }
    }

    /** */
    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray
     * @return
     */
    public static final String bytesToHexString(byte[] bArray, int count) {
        StringBuffer sb = new StringBuffer(bArray.length);

        String sTemp;
        for (int i = 0; i < count; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
}

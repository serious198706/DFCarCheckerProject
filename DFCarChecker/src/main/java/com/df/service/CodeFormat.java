package com.df.service;

import com.df.dfcarchecker.CarCheckCollectDataActivity;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;



public class CodeFormat {
    static String dataOne;
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

    public static String[] strback(String str) {
        int nums = 0;
        // all[0]为部位名称、all[1] 为数值 、all[2] 为长度

        String all[] = new String[3];
        int len = str.length();
        if (!CarCheckCollectDataActivity.HAS_RESQUEST_STR && str.length() >= 14) {
            StringBuffer sb1 = new StringBuffer(str.substring(8, 10));
            StringBuffer sb2 = new StringBuffer(str.substring(10, 12));
            StringBuffer sb = new StringBuffer(str.substring(8, 12));
            int int_sb1 = Integer.parseInt(String.valueOf(sb1), 16);
            int int_sb2 = Integer.parseInt(String.valueOf(sb2), 16);
            int int_sb3 = Integer.parseInt(str.substring(4, 8), 16);
            String and = Integer
                    .toHexString((198 + int_sb1 + int_sb2 + int_sb3));
            if (and.length() < 4) {
                for (int i = 4; i > and.length(); i--) {
                    and = "0" + and;
                }
            }
            CarCheckCollectDataActivity.RESQUEST_STR = "aa0a" + str.substring(4, 8) + sb
                    + "0111" + and;
            return null;
        }
        if (str.startsWith("AA") || str.startsWith("aa")) {
            if (str.length() >= 14) {
                // all[0]指的是什么部位数据
                all[0] = str.substring(12, 14);
                StringBuffer str1 = new StringBuffer(str.substring(14, len - 4));
                StringBuffer str2 = new StringBuffer();
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
                        nums++;
                        str2.append(String.valueOf(a) + "  ");

                    }

                    all[1] = String.valueOf(str2);
                    all[2] = String.valueOf(nums);
                }
                return all;
            }
            return all;

        }
        return null;
    }

    public static String StringFilter(String str) throws PatternSyntaxException {
        // 只允许字母和数字
        // String regEx = "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
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

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < 20; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
                // System.out.println(stringBuilder);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /** */
    /**
     * 把字节数组转换成16进制字符串
     *
     * @param bArray
     * @return
     */
    public static final String bytesToHexStringTwo(byte[] bArray, int count) {
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

    // 分割字符串
    public static String Stringspace(String str) {

        String temp = "";
        String temp2 = "";
        for (int i = 0; i < str.length(); i++) {

            if (i % 2 == 0) {
                temp = str.charAt(i) + "";
                temp2 += temp;
                // System.out.println(temp);
            } else {
                temp2 += str.charAt(i) + " ";
            }

        }
        return temp2;
    }

    /**
     * Byte -> Hex
     *
     * @param bytes
     * @return
     */
    public static String byteToHex(byte[] bytes, int count) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex).append(" ");
        }
        return sb.toString();
    }

    /**
     * String -> Hex
     *
     * @param s
     * @return
     */
    public static String stringToHex(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            if (s4.length() == 1) {
                s4 = '0' + s4;
            }
            str = str + s4 + " ";
        }
        return str;
    }

    public static String changeCharset(String str, String newCharset)
            throws UnsupportedEncodingException {
        if (str != null) {
            // 用默认字符编码解码字符串。
            byte[] bs = str.getBytes();
            // 用新的字符编码生成字符串
            return new String(bs, newCharset);
        }
        return null;
    }
}

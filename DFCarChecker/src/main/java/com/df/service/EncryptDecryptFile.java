package com.df.service;

/**
 * Created by 岩 on 13-11-11.
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;

public class EncryptDecryptFile {

    private static Cipher ecipher;
    private static Cipher dcipher;

    // 8-byte initialization vector
    private static byte[] iv = {
            (byte)0xB2, (byte)0x12, (byte)0xD5, (byte)0xB2,
            (byte)0x44, (byte)0x21, (byte)0xC3, (byte)0xC3
    };

    public EncryptDecryptFile() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("DES");

            kg.init(56);

            SecretKey key = kg.generateKey();

            //SecretKey key = KeyGenerator.getInstance("DES").generateKey();

            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);

            ecipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            dcipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        }
        catch (InvalidAlgorithmParameterException e) {
            System.out.println("Invalid Alogorithm Parameter:" + e.getMessage());
            return;
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("No Such Algorithm:" + e.getMessage());
            return;
        }
        catch (NoSuchPaddingException e) {
            System.out.println("No Such Padding:" + e.getMessage());
            return;
        }
        catch (InvalidKeyException e) {
            System.out.println("Invalid Key:" + e.getMessage());
            return;
        }
    }

    public void encrypt(InputStream is, OutputStream os) {
        try {
            byte[] buf = new byte[1024];

            // 进行编码
            os = new CipherOutputStream(os, ecipher);

            // 读取到内存中
            int numRead = 0;
            while ((numRead = is.read(buf)) >= 0) {
                os.write(buf, 0, numRead);
            }

            os.close();
        }

        catch (IOException e) {
            System.out.println("I/O Error:" + e.getMessage());
        }
    }

    public void decrypt(InputStream is, OutputStream os) {
        try {
            byte[] buf = new byte[1024];

            // 进行解码
            CipherInputStream cis = new CipherInputStream(is, dcipher);

            // 读取到内存中
            int numRead = 0;
            while ((numRead = cis.read(buf)) >= 0) {
                os.write(buf, 0, numRead);
            }

            cis.close();
            is.close();
            os.close();
        }

        catch (IOException e) {
            System.out.println("I/O Error:" + e.getMessage());
        }
    }
}
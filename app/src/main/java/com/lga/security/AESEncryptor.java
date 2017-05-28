package com.lga.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Jay on 2017/5/27.
 */

public class AESEncryptor {

    private final static String HEX = "0123456789ABCDEF";

    /**
     * AES加密
     */
    public static String encrypt(String seed, String cleartext) {
        if (seed != null && cleartext != null) {
            byte[] rawKey = getRawKey(seed.getBytes());
            if (rawKey != null) {
                byte[] result = encrypt(rawKey, cleartext.getBytes());
                if (result != null) {
                    return toHex(result);
                }
            }
        }

        return null;
    }

    /**
     * AES解密
     */
    public static String decrypt(String seed, String encrypted) {
        if (seed != null && encrypted != null) {
            byte[] rawKey = getRawKey(seed.getBytes());
            if (rawKey != null) {
                byte[] enc = toByte(encrypted);
                if (enc != null) {
                    byte[] result = decrypt(rawKey, enc);
                    return new String(result);
                }
            }
        }

        return null;
    }

    private static byte[] getRawKey(byte[] seed) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
            sr.setSeed(seed);
            kgen.init(128, sr); // 192 and 256 bits may not be available
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();

            return raw;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }

        return null;
    }


    private static byte[] encrypt(byte[] raw, byte[] clear) {
        byte[] encrypted = null;
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            encrypted = cipher.doFinal(clear);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | BadPaddingException | IllegalBlockSizeException
                | InvalidKeyException e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) {
        byte[] decrypted = null;

        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            decrypted = cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException
                | InvalidKeyException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}
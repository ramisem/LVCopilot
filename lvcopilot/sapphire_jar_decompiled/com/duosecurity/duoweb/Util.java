/*
 * Decompiled with CFR 0.152.
 */
package com.duosecurity.duoweb;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Util {
    public static String hmacSign(String skey, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec key = new SecretKeySpec(skey.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);
        byte[] raw = mac.doFinal(data.getBytes());
        return Util.bytesToHex(raw);
    }

    public static String bytesToHex(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; ++i) {
            result = result + Integer.toString((b[i] & 0xFF) + 256, 16).substring(1);
        }
        return result;
    }
}


package com.manga.translator.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具类。
 * <p>
 * 用于百度翻译 API 签名计算。
 */
public class Md5Util {

    private Md5Util() {
        // 工具类，禁止实例化
    }

    /**
     * 计算字符串的 MD5 值。
     *
     * @param input 输入字符串
     * @return 32 位小写 MD5 哈希值
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }
}

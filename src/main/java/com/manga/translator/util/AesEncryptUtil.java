package com.manga.translator.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

/**
 * AES-128 加密工具类。
 * <p>
 * 用于 API Key 的本地加密存储。
 * 密钥由本地机器指纹派生，不硬编码在代码中。
 */
public class AesEncryptUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private AesEncryptUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 生成 AES-128 密钥。
     *
     * @return 密钥字节数组（16 字节）
     */
    public static byte[] generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();
            return secretKey.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES 算法不可用", e);
        }
    }

    /**
     * 使用密钥加密明文。
     *
     * @param data     明文数据
     * @param keyBytes 密钥字节数组（16 字节）
     * @return 加密后的字节数组
     */
    public static byte[] encrypt(byte[] data, byte[] keyBytes) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    /**
     * 使用密钥解密密文。
     *
     * @param encryptedData 密文数据
     * @param keyBytes      密钥字节数组（16 字节）
     * @return 解密后的字节数组
     */
    public static byte[] decrypt(byte[] encryptedData, byte[] keyBytes) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败", e);
        }
    }
}

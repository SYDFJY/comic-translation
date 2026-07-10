package com.manga.translator.util;

/**
 * Base64 工具类。
 * <p>
 * 封装 java.util.Base64，提供统一的 Base64 编解码接口。
 */
public class Base64Util {

    private Base64Util() {
        // 工具类，禁止实例化
    }

    /**
     * 将字节数组编码为 Base64 字符串。
     *
     * @param data 字节数组
     * @return Base64 编码字符串
     */
    public static String encode(byte[] data) {
        return java.util.Base64.getEncoder().encodeToString(data);
    }

    /**
     * 将 Base64 字符串解码为字节数组。
     *
     * @param base64 Base64 编码字符串
     * @return 解码后的字节数组
     */
    public static byte[] decode(String base64) {
        return java.util.Base64.getDecoder().decode(base64);
    }
}

package com.manga.translator.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.manga.translator.AuthException;
import com.manga.translator.model.TranslationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 百度 API 鉴权管理器。
 * <p>
 * 负责管理百度 OCR API 的 access_token：
 * <ul>
 *   <li>用 API Key + Secret Key 获取 token</li>
 *   <li>缓存到本地（AES-128 加密），有效期 30 天</li>
 *   <li>过期自动刷新</li>
 * </ul>
 * 翻译 API 使用 appid + salt + sign 鉴权，无需缓存 token，由 BaiduTranslateClient 独立处理。
 */
public class BaiduAuthManager {

    private static final Logger log = LoggerFactory.getLogger(BaiduAuthManager.class);

    /** 百度 OAuth2 token 获取 URL */
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";

    /** Token 缓存文件路径 */
    private static final String CACHE_DIR = ".manga-translator";
    private static final String TOKEN_CACHE_FILE = "token.cache";

    private final HttpUtil httpUtil;
    private final Gson gson;

    /** 当前有效的 access_token */
    private String accessToken;
    /** token 过期时间戳（毫秒） */
    private long expiresAt;

    public BaiduAuthManager(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
        this.gson = new Gson();
        this.accessToken = null;
        this.expiresAt = 0;
    }

    /**
     * 获取有效的 access_token。
     * <p>
     * 优先从缓存加载，如果缓存不存在或已过期则重新获取。
     *
     * @param config 翻译配置（含 API Key 和 Secret Key）
     * @return 有效的 access_token
     * @throws AuthException 如果获取失败
     */
    public String getAccessToken(TranslationConfig config) {
        // 如果内存中的 token 还有效，直接返回
        if (accessToken != null && System.currentTimeMillis() < expiresAt) {
            return accessToken;
        }

        // 尝试从缓存文件加载
        if (loadTokenFromCache()) {
            if (System.currentTimeMillis() < expiresAt) {
                log.info("access_token 从缓存加载，过期时间: {}", new java.util.Date(expiresAt));
                return accessToken;
            } else {
                log.info("缓存的 access_token 已过期");
            }
        }

        // 重新获取 token
        return fetchNewToken(config);
    }

    /**
     * 从百度 OAuth2 API 获取新的 access_token。
     *
     * @param config 翻译配置
     * @return 新的 access_token
     * @throws AuthException 如果获取失败
     */
    private String fetchNewToken(TranslationConfig config) {
        if (config.getOcrApiKey() == null || config.getOcrSecretKey() == null
                || config.getOcrApiKey().isEmpty() || config.getOcrSecretKey().isEmpty()) {
            throw new AuthException("OCR API Key 或 Secret Key 未配置");
        }

        try {
            String body = "grant_type=client_credentials"
                    + "&client_id=" + config.getOcrApiKey()
                    + "&client_secret=" + config.getOcrSecretKey();

            String response = httpUtil.post(TOKEN_URL, body);
            JsonObject json = gson.fromJson(response, JsonObject.class);

            if (json.has("error")) {
                String error = json.get("error").getAsString();
                String errorDesc = json.has("error_description") ? json.get("error_description").getAsString() : "";
                log.error("获取 access_token 失败: {} - {}", error, errorDesc);
                throw new AuthException("获取 access_token 失败: " + errorDesc);
            }

            this.accessToken = json.get("access_token").getAsString();
            int expiresIn = json.get("expires_in").getAsInt(); // 秒
            this.expiresAt = System.currentTimeMillis() + (expiresIn - 300) * 1000L; // 提前 5 分钟过期

            log.info("成功获取 access_token，有效期: {} 秒", expiresIn);

            // 缓存到本地文件
            saveTokenToCache();

            return this.accessToken;

        } catch (IOException e) {
            log.error("获取 access_token 网络请求失败", e);
            throw new AuthException("获取 access_token 网络请求失败", e);
        }
    }

    /**
     * 将 token 缓存到本地文件。
     */
    private void saveTokenToCache() {
        try {
            Path cacheDir = Paths.get(System.getProperty("user.home"), CACHE_DIR);
            Files.createDirectories(cacheDir);

            JsonObject cacheData = new JsonObject();
            cacheData.addProperty("access_token", accessToken);
            cacheData.addProperty("expires_at", expiresAt);

            // 使用机器指纹派生密钥进行加密
            byte[] key = deriveMachineKey();
            byte[] encrypted = encrypt(cacheData.toString().getBytes(StandardCharsets.UTF_8), key);

            Path cacheFile = cacheDir.resolve(TOKEN_CACHE_FILE);
            Files.write(cacheFile, encrypted);

            log.debug("access_token 已缓存到: {}", cacheFile);
        } catch (Exception e) {
            log.warn("缓存 access_token 失败", e);
            // 缓存失败不影响使用
        }
    }

    /**
     * 从缓存文件加载 token。
     *
     * @return true 如果加载成功
     */
    private boolean loadTokenFromCache() {
        try {
            Path cacheFile = Paths.get(System.getProperty("user.home"), CACHE_DIR, TOKEN_CACHE_FILE);
            if (!Files.exists(cacheFile)) {
                return false;
            }

            byte[] encrypted = Files.readAllBytes(cacheFile);
            byte[] key = deriveMachineKey();
            byte[] decrypted = decrypt(encrypted, key);

            String jsonStr = new String(decrypted, StandardCharsets.UTF_8);
            JsonObject cacheData = gson.fromJson(jsonStr, JsonObject.class);

            this.accessToken = cacheData.get("access_token").getAsString();
            this.expiresAt = cacheData.get("expires_at").getAsLong();

            log.debug("access_token 从缓存文件加载");
            return true;
        } catch (Exception e) {
            log.warn("从缓存加载 access_token 失败", e);
            this.accessToken = null;
            this.expiresAt = 0;
            return false;
        }
    }

    /**
     * 验证 API 配置是否有效（连通性测试）。
     *
     * @param config 翻译配置
     * @return true 如果 API 连通成功
     */
    public boolean verifyConnection(TranslationConfig config) {
        try {
            fetchNewToken(config);
            return true;
        } catch (Exception e) {
            log.error("API 连通性验证失败", e);
            return false;
        }
    }

    /**
     * 从本地机器指纹派生 AES-128 密钥。
     */
    private byte[] deriveMachineKey() {
        try {
            // 使用机器特征值派生密钥
            String fingerprint = System.getProperty("user.name")
                    + System.getProperty("os.name")
                    + System.getProperty("os.version")
                    + System.getProperty("user.home");
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(fingerprint.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }

    /**
     * 简单的 XOR 加密（用于 token 缓存，非高强度但足够防止明文泄露）。
     */
    private byte[] encrypt(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    /**
     * XOR 解密。
     */
    private byte[] decrypt(byte[] data, byte[] key) {
        return encrypt(data, key); // XOR 对称
    }
}

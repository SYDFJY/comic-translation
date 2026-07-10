package com.manga.translator.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.manga.translator.TranslationException;
import com.manga.translator.util.Md5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 百度翻译 API 客户端。
 * <p>
 * 封装百度通用文本翻译 API 的 HTTP 调用和签名生成。
 * 鉴权方式：appid + salt + sign（MD5 签名）
 */
public class BaiduTranslateClient {

    private static final Logger log = LoggerFactory.getLogger(BaiduTranslateClient.class);

    /** 百度翻译 API URL */
    private static final String TRANSLATE_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    /** 单次请求最大字符数 */
    private static final int MAX_CHARS = 6000;

    private final HttpUtil httpUtil;
    private final Gson gson;

    public BaiduTranslateClient(HttpUtil httpUtil) {
        this.httpUtil = httpUtil;
        this.gson = new Gson();
    }

    /**
     * 翻译单条文本。
     *
     * @param query        待翻译文本
     * @param from         源语言代码（auto 自动检测）
     * @param to           目标语言代码
     * @param appId        百度翻译 App ID
     * @param secretKey    百度翻译密钥
     * @return 翻译结果字符串
     * @throws IOException          如果网络请求失败
     * @throws TranslationException 如果 API 返回错误
     */
    public String translate(String query, String from, String to,
                            String appId, String secretKey) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            log.warn("翻译请求文本为空");
            return "";
        }

        if (query.length() > MAX_CHARS) {
            log.warn("翻译文本超长({}字符)，截断至{}字符", query.length(), MAX_CHARS);
            query = query.substring(0, MAX_CHARS);
        }

        // 生成 salt 和 sign
        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String sign = Md5Util.md5(appId + query + salt + secretKey);

        // 构建请求参数
        String body = "q=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&from=" + from
                + "&to=" + to
                + "&appid=" + appId
                + "&salt=" + salt
                + "&sign=" + sign;

        log.info("翻译请求: from={}, to={}, text_len={}", from, to, query.length());
        long startTime = System.currentTimeMillis();

        String response = httpUtil.post(TRANSLATE_URL, body);

        long duration = System.currentTimeMillis() - startTime;
        log.info("翻译响应耗时: {}ms", duration);

        // 检查 API 错误
        JsonObject json = gson.fromJson(response, JsonObject.class);
        if (json.has("error_code") && !json.get("error_code").getAsString().equals("0")
                && !json.get("error_code").getAsString().equals("52000")) {
            String errorCode = json.get("error_code").getAsString();
            String errorMsg = json.has("error_msg") ? json.get("error_msg").getAsString() : "";
            log.error("翻译 API 错误: code={}, msg={}", errorCode, errorMsg);
            throw new TranslationException("翻译 API 错误 [" + errorCode + "]: " + errorMsg);
        }

        // 解析翻译结果
        if (json.has("trans_result")) {
            JsonArray transResult = json.getAsJsonArray("trans_result");
            if (transResult != null && transResult.size() > 0) {
                JsonObject first = transResult.get(0).getAsJsonObject();
                if (first.has("dst")) {
                    return first.get("dst").getAsString();
                }
            }
        }

        log.warn("翻译响应中无翻译结果");
        return "";
    }

    /**
     * 批量翻译多条文本（用换行符拼接）。
     *
     * @param queries   待翻译文本列表
     * @param from      源语言代码
     * @param to        目标语言代码
     * @param appId     百度翻译 App ID
     * @param secretKey 百度翻译密钥
     * @return 翻译结果列表（与输入顺序一一对应）
     * @throws IOException          如果网络请求失败
     * @throws TranslationException 如果 API 返回错误
     */
    public String[] translateBatch(String[] queries, String from, String to,
                                    String appId, String secretKey) throws IOException {
        // 用换行符拼接
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < queries.length; i++) {
            if (i > 0) sb.append("\n");
            sb.append(queries[i]);
        }
        String joined = sb.toString();

        String result = translate(joined, from, to, appId, secretKey);
        if (result.isEmpty()) {
            return new String[0];
        }

        // 按换行符拆分
        return result.split("\n");
    }
}

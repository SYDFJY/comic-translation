package com.manga.translator.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.manga.translator.OcrException;
import com.manga.translator.model.OcrVersion;
import com.manga.translator.model.TextRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 百度 OCR API 客户端。
 * <p>
 * 封装百度通用文字识别 API 和高精度版 API 的 HTTP 调用。
 */
public class BaiduOcrClient {

    private static final Logger log = LoggerFactory.getLogger(BaiduOcrClient.class);

    /** OCR 通用版 API URL */
    private static final String GENERAL_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
    /** OCR 高精度版 API URL */
    private static final String ACCURATE_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic";

    /** 默认气泡框扩展量（px） */
    private static final int DEFAULT_EXPAND_MARGIN = 12;
    /** 最低置信度阈值 */
    public static final double MIN_PROBABILITY = 0.5;
    /** 低置信度警告阈值 */
    public static final double WARN_PROBABILITY = 0.7;

    private final HttpUtil httpUtil;
    private final BaiduAuthManager authManager;
    private final Gson gson;

    public BaiduOcrClient(HttpUtil httpUtil, BaiduAuthManager authManager) {
        this.httpUtil = httpUtil;
        this.authManager = authManager;
        this.gson = new Gson();
    }

    /**
     * 对图片执行 OCR 识别。
     *
     * @param imageBase64 图片的 Base64 编码
     * @param accessToken 有效的百度 access_token
     * @param ocrVersion  OCR 版本
     * @return 百度 API 返回的原始 JSON 字符串
     * @throws IOException  如果网络请求失败
     * @throws OcrException 如果 API 返回错误
     */
    public String recognize(String imageBase64, String accessToken, OcrVersion ocrVersion) throws IOException {
        String url = (ocrVersion == OcrVersion.ACCURATE_BASIC) ? ACCURATE_URL : GENERAL_URL;
        url += "?access_token=" + accessToken;

        String body = "image=" + java.net.URLEncoder.encode(imageBase64, "UTF-8")
                + "&language_type=JAP"
                + "&detect_direction=true"
                + "&detect_language=true"
                + "&paragraph=false"
                + "&probability=true"
                + "&vertex_location=true";

        log.info("OCR 请求: version={}, image_size={}bytes", ocrVersion, imageBase64.length());
        long startTime = System.currentTimeMillis();

        String response = httpUtil.post(url, body);

        long duration = System.currentTimeMillis() - startTime;
        log.info("OCR 响应耗时: {}ms", duration);

        // 检查 API 错误
        JsonObject json = gson.fromJson(response, JsonObject.class);
        if (json.has("error_code")) {
            int errorCode = json.get("error_code").getAsInt();
            String errorMsg = json.has("error_msg") ? json.get("error_msg").getAsString() : "";
            log.error("OCR API 错误: code={}, msg={}", errorCode, errorMsg);
            throw new OcrException("OCR API 错误 [" + errorCode + "]: " + errorMsg);
        }

        return response;
    }

    /**
     * 解析 OCR 响应 JSON 为 TextRegion 列表。
     *
     * @param responseJson OCR API 返回的 JSON 字符串
     * @param expandMargin 区域扩展边距
     * @return 文字区域列表（已过滤低置信度）
     */
    public List<TextRegion> parseResponse(String responseJson, int expandMargin) {
        List<TextRegion> regions = new ArrayList<>();
        JsonObject json = gson.fromJson(responseJson, JsonObject.class);

        if (!json.has("words_result")) {
            log.warn("OCR 响应中无文字区域: words_result 字段缺失");
            return regions;
        }

        JsonArray wordsResult = json.getAsJsonArray("words_result");
        if (wordsResult == null || wordsResult.size() == 0) {
            log.info("OCR 未检测到文字区域");
            return regions;
        }

        int margin = expandMargin > 0 ? expandMargin : DEFAULT_EXPAND_MARGIN;

        for (int i = 0; i < wordsResult.size(); i++) {
            JsonObject item = wordsResult.get(i).getAsJsonObject();

            // 提取置信度
            double probability = 0;
            boolean hasProbability = false;
            if (item.has("probability") && !item.get("probability").isJsonNull()) {
                JsonObject probObj = item.getAsJsonObject("probability");
                if (probObj.has("average")) {
                    probability = probObj.get("average").getAsDouble();
                    hasProbability = true;
                }
            }

            // 置信度门控：仅当 API 返回了置信度时才过滤
            if (hasProbability && probability < MIN_PROBABILITY) {
                log.debug("OCR 低置信度丢弃: prob={}", probability);
                continue;
            }

            // 提取文字
            String words = item.has("words") ? item.get("words").getAsString() : "";

            // 提取坐标
            if (item.has("location")) {
                JsonObject location = item.getAsJsonObject("location");
                int left = getIntSafe(location, "left");
                int top = getIntSafe(location, "top");
                int width = getIntSafe(location, "width");
                int height = getIntSafe(location, "height");

                // 提取方向
                int direction = item.has("direction") ? item.get("direction").getAsInt() : 0;

                // 提取语言
                String language = json.has("language") ? json.get("language").getAsString() : "";

                TextRegion region = new TextRegion();
                region.setId(i + 1);
                region.setLeft(left);
                region.setTop(top);
                region.setWidth(width);
                region.setHeight(height);
                region.setExpandLeft(left - margin);
                region.setExpandTop(top - margin);
                region.setExpandWidth(width + 2 * margin);
                region.setExpandHeight(height + 2 * margin);
                region.setOriginalText(words);
                region.setDirection(direction);
                region.setLanguage(language);
                region.setProbability(probability);

                regions.add(region);
            }
        }

        log.info("OCR 解析完成: 有效区域={}, 总检测={}", regions.size(), wordsResult.size());
        return regions;
    }

    /**
     * 从 JsonObject 安全获取 int 值。
     */
    private int getIntSafe(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsInt();
        }
        return 0;
    }
}

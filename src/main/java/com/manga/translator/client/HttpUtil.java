package com.manga.translator.client;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 请求工具类。
 * <p>
 * 封装 Apache HttpClient 5，提供统一的 POST 请求接口。
 * 所有百度 API 调用均通过此工具类发送请求。
 */
public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    /** 连接超时时间（秒） */
    private static final int CONNECT_TIMEOUT = 10;
    /** 读取超时时间（秒） */
    private static final int READ_TIMEOUT = 30;

    private final CloseableHttpClient httpClient;

    public HttpUtil() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(CONNECT_TIMEOUT, TimeUnit.SECONDS))
                .setResponseTimeout(Timeout.of(READ_TIMEOUT, TimeUnit.SECONDS))
                .setConnectionRequestTimeout(Timeout.of(CONNECT_TIMEOUT, TimeUnit.SECONDS))
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }

    /**
     * 发送 POST 请求（application/x-www-form-urlencoded）。
     *
     * @param url  请求 URL
     * @param body 请求体字符串
     * @return 响应体字符串
     * @throws IOException 如果请求失败
     */
    public String post(String url, String body) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(body, ContentType.APPLICATION_FORM_URLENCODED));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        log.debug("POST 请求: {}", url);
        long startTime = System.currentTimeMillis();

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            long duration = System.currentTimeMillis() - startTime;

            log.debug("响应状态: {}, 耗时: {}ms", statusCode, duration);

            if (statusCode != 200) {
                log.warn("HTTP 请求失败: status={}, url={}", statusCode, url);
                throw new IOException("HTTP " + statusCode + ": " + responseBody);
            }

            return responseBody;
        } catch (ParseException e) {
            throw new IOException("解析 HTTP 响应失败", e);
        }
    }

    /**
     * 发送 POST 请求（JSON body）。
     *
     * @param url  请求 URL
     * @param json JSON 请求体
     * @return 响应体字符串
     * @throws IOException 如果请求失败
     */
    public String postJson(String url, String json) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");

        log.debug("POST JSON 请求: {}", url);
        long startTime = System.currentTimeMillis();

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            long duration = System.currentTimeMillis() - startTime;

            log.debug("响应状态: {}, 耗时: {}ms", statusCode, duration);

            if (statusCode != 200) {
                log.warn("HTTP 请求失败: status={}, url={}", statusCode, url);
                throw new IOException("HTTP " + statusCode + ": " + responseBody);
            }

            return responseBody;
        } catch (ParseException e) {
            throw new IOException("解析 HTTP 响应失败", e);
        }
    }

    /**
     * 关闭 HTTP 客户端，释放资源。
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.warn("关闭 HTTP 客户端失败", e);
        }
    }
}

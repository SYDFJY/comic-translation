package com.manga.translator.service.impl;

import com.manga.translator.OcrException;
import com.manga.translator.client.BaiduAuthManager;
import com.manga.translator.client.BaiduOcrClient;
import com.manga.translator.client.CircuitBreaker;
import com.manga.translator.client.TokenBucketRateLimiter;
import com.manga.translator.model.OcrVersion;
import com.manga.translator.model.TextRegion;
import com.manga.translator.model.TranslationConfig;
import com.manga.translator.service.OcrService;
import com.manga.translator.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 百度 OCR 服务实现。
 * <p>
 * 使用百度通用文字识别/高精度版 API 实现 OCR 文字检测。
 */
public class BaiduOcrServiceImpl implements OcrService {

    private static final Logger log = LoggerFactory.getLogger(BaiduOcrServiceImpl.class);

    private static final double OCR_QPS = 1.5;

    private final BaiduOcrClient ocrClient;
    private final BaiduAuthManager authManager;
    private final TranslationConfig config;
    private final TokenBucketRateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;

    public BaiduOcrServiceImpl(BaiduOcrClient ocrClient, BaiduAuthManager authManager,
                               TranslationConfig config) {
        this.ocrClient = ocrClient;
        this.authManager = authManager;
        this.config = config;
        this.rateLimiter = new TokenBucketRateLimiter(OCR_QPS);
        this.circuitBreaker = new CircuitBreaker("OCR");
    }

    @Override
    public List<TextRegion> recognize(BufferedImage image) throws OcrException {
        if (image == null) {
            log.warn("OCR 输入图片为 null");
            return Collections.emptyList();
        }

        // 检查熔断器
        if (!circuitBreaker.allowRequest()) {
            log.warn("OCR 熔断器开启中，拒绝请求");
            throw new OcrException("OCR 服务暂时不可用（熔断中）");
        }

        // 限速
        if (!rateLimiter.tryAcquire()) {
            log.warn("OCR 限速等待超时");
            throw new OcrException("OCR 服务繁忙，请稍后重试");
        }

        try {
            // 图片编码为 Base64
            String imageBase64 = ImageUtil.imageToBase64(image, "png");

            // 获取 access_token
            String accessToken = authManager.getAccessToken(config);

            // 调用 OCR API
            OcrVersion ocrVersion = config.getOcrVersion() != null
                    ? config.getOcrVersion() : OcrVersion.GENERAL_BASIC;

            String responseJson = ocrClient.recognize(imageBase64, accessToken, ocrVersion);

            // 解析响应为 TextRegion 列表
            List<TextRegion> regions = ocrClient.parseResponse(responseJson, config.getExpandMargin());

            log.info("OCR 识别完成: 有效区域={}", regions.size());

            // 记录成功
            circuitBreaker.onSuccess();
            return regions;

        } catch (IOException e) {
            circuitBreaker.onFailure();
            log.error("OCR 网络请求失败", e);
            throw new OcrException("OCR 网络请求失败: " + e.getMessage(), e);
        } catch (OcrException e) {
            circuitBreaker.onFailure();
            throw e;
        }
    }

    /**
     * 获取 OCR 熔断器（供外部检查状态）。
     *
     * @return 熔断器实例
     */
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}

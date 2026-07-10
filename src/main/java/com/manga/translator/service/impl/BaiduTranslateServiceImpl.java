package com.manga.translator.service.impl;

import com.manga.translator.TranslationException;
import com.manga.translator.client.BaiduTranslateClient;
import com.manga.translator.client.CircuitBreaker;
import com.manga.translator.client.TokenBucketRateLimiter;
import com.manga.translator.model.RegionStatus;
import com.manga.translator.model.TextRegion;
import com.manga.translator.model.TranslationConfig;
import com.manga.translator.service.TranslateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 百度翻译服务实现。
 * <p>
 * 使用百度通用翻译 API 实现文字翻译。
 * 支持单条和批量翻译模式。
 */
public class BaiduTranslateServiceImpl implements TranslateService {

    private static final Logger log = LoggerFactory.getLogger(BaiduTranslateServiceImpl.class);

    private static final double TRANSLATE_QPS = 8.0;

    /** 逐条翻译的最大条数 */
    private static final int SINGLE_THRESHOLD = 5;
    /** 分批翻译的分组大小 */
    private static final int BATCH_SIZE_SMALL = 5;
    private static final int BATCH_SIZE_LARGE = 10;

    private final BaiduTranslateClient translateClient;
    private final TranslationConfig config;
    private final TokenBucketRateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;

    public BaiduTranslateServiceImpl(BaiduTranslateClient translateClient, TranslationConfig config) {
        this.translateClient = translateClient;
        this.config = config;
        this.rateLimiter = new TokenBucketRateLimiter(TRANSLATE_QPS);
        this.circuitBreaker = new CircuitBreaker("TRANSLATE");
    }

    @Override
    public void translate(List<TextRegion> regions) throws TranslationException {
        if (regions == null || regions.isEmpty()) {
            log.info("翻译列表为空，跳过翻译");
            return;
        }

        // 检查熔断器
        if (!circuitBreaker.allowRequest()) {
            log.warn("翻译熔断器开启中，拒绝请求");
            throw new TranslationException("翻译服务暂时不可用（熔断中）");
        }

        String from = config.getSourceLanguage() != null ? config.getSourceLanguage() : "auto";
        String to = config.getTargetLanguage() != null ? config.getTargetLanguage() : "zh";
        String appId = config.getTranslateAppId();
        String secretKey = config.getTranslateSecretKey();

        if (appId == null || appId.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            log.warn("翻译 API 未配置（缺少 AppId 或 SecretKey）");
            for (TextRegion region : regions) {
                region.setStatus(RegionStatus.ERROR);
            }
            return;
        }

        int total = regions.size();
        int failedCount = 0;
        int successCount = 0;

        try {
            if (total <= SINGLE_THRESHOLD) {
                // 逐条翻译
                for (TextRegion region : regions) {
                    if (translateSingle(region, from, to, appId, secretKey)) {
                        successCount++;
                    } else {
                        failedCount++;
                    }
                }
            } else {
                // 分批翻译
                int batchSize = total > 20 ? BATCH_SIZE_LARGE : BATCH_SIZE_SMALL;
                failedCount += translateBatched(regions, from, to, appId, secretKey, batchSize);
                successCount = total - failedCount;
            }

            log.info("翻译完成: 成功={}, 失败={}", successCount, failedCount);

            if (failedCount > 0 && successCount > 0) {
                circuitBreaker.onSuccess(); // 部分成功也算通过
            } else if (failedCount == total) {
                circuitBreaker.onFailure();
            } else {
                circuitBreaker.onSuccess();
            }

        } catch (IOException e) {
            circuitBreaker.onFailure();
            log.error("翻译网络请求失败", e);
            throw new TranslationException("翻译网络请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 翻译单个文字区域。
     *
     * @return true 如果翻译成功
     */
    private boolean translateSingle(TextRegion region, String from, String to,
                                    String appId, String secretKey) throws IOException {
        String text = region.getOriginalText();
        if (text == null || text.isEmpty()) {
            region.setStatus(RegionStatus.ERROR);
            return false;
        }

        // 限速
        rateLimiter.acquire();

        String result = translateClient.translate(text, from, to, appId, secretKey);
        if (result != null && !result.isEmpty()) {
            region.setTranslatedText(result);
            region.setStatus(RegionStatus.TRANSLATED);
            return true;
        } else {
            log.warn("翻译结果为空: region_id={}, text={}", region.getId(), text);
            region.setStatus(RegionStatus.ERROR);
            return false;
        }
    }

    /**
     * 分批翻译多个文字区域。
     *
     * @return 失败数量
     */
    private int translateBatched(List<TextRegion> regions, String from, String to,
                                 String appId, String secretKey, int batchSize) throws IOException {
        int failedCount = 0;
        List<List<TextRegion>> batches = partition(regions, batchSize);

        for (List<TextRegion> batch : batches) {
            // 收集该批次的原文
            String[] queries = new String[batch.size()];
            int[] regionIndices = new int[batch.size()];
            for (int i = 0; i < batch.size(); i++) {
                queries[i] = batch.get(i).getOriginalText();
                regionIndices[i] = regions.indexOf(batch.get(i));
            }

            // 限速
            rateLimiter.acquire();

            // 调用批量翻译
            String[] results = translateClient.translateBatch(queries, from, to, appId, secretKey);

            if (results.length == 0) {
                // 全部失败
                for (TextRegion region : batch) {
                    region.setStatus(RegionStatus.ERROR);
                }
                failedCount += batch.size();
            } else {
                // 回填结果
                for (int i = 0; i < Math.min(results.length, batch.size()); i++) {
                    TextRegion region = batch.get(i);
                    if (results[i] != null && !results[i].isEmpty()) {
                        region.setTranslatedText(results[i]);
                        region.setStatus(RegionStatus.TRANSLATED);
                    } else {
                        region.setStatus(RegionStatus.ERROR);
                        failedCount++;
                    }
                }
                // 如果翻译结果少于输入，剩余标记失败
                for (int i = results.length; i < batch.size(); i++) {
                    batch.get(i).setStatus(RegionStatus.ERROR);
                    failedCount++;
                }
            }
        }

        return failedCount;
    }

    /**
     * 将列表分批。
     */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    /**
     * 获取翻译熔断器（供外部检查状态）。
     *
     * @return 熔断器实例
     */
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}

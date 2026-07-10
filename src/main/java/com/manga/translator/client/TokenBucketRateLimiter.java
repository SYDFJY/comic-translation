package com.manga.translator.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶限速器。
 * <p>
 * 用于控制 API 调用的 QPS，防止触发百度 API 的限流机制。
 * OCR: ≤1.5 QPS（每 667ms 释放 1 令牌）
 * 翻译: ≤8 QPS（每 125ms 释放 1 令牌）
 */
public class TokenBucketRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

    private final Semaphore semaphore;
    private final double maxQps;
    private final long intervalMs;

    /**
     * 创建令牌桶限速器。
     *
     * @param maxQps 最大 QPS
     */
    public TokenBucketRateLimiter(double maxQps) {
        this.maxQps = maxQps;
        this.semaphore = new Semaphore(1);
        this.intervalMs = (long) (1000.0 / maxQps);
    }

    /**
     * 尝试获取一个令牌。
     * <p>
     * 最多等待 5 秒，如果仍无法获取则返回 false。
     *
     * @return true 如果成功获取令牌
     */
    public boolean tryAcquire() {
        try {
            boolean acquired = semaphore.tryAcquire(5, TimeUnit.SECONDS);
            if (acquired) {
                // 在单独的线程中延迟释放令牌，控制 QPS
                startReleaseTimer();
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 获取一个令牌（阻塞直到获取成功）。
     */
    public void acquire() {
        try {
            semaphore.acquire();
            startReleaseTimer();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 在指定延迟后释放令牌。
     */
    private void startReleaseTimer() {
        Thread releaseThread = new Thread(() -> {
            try {
                Thread.sleep(intervalMs);
                semaphore.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                semaphore.release(); // 中断时立即释放
            }
        }, "rate-limiter-" + maxQps);
        releaseThread.setDaemon(true);
        releaseThread.start();
    }

    /**
     * 获取当前最大 QPS。
     *
     * @return 最大 QPS
     */
    public double getMaxQps() {
        return maxQps;
    }

    /**
     * 获取令牌间隔（毫秒）。
     *
     * @return 间隔毫秒数
     */
    public long getIntervalMs() {
        return intervalMs;
    }
}

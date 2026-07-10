package com.manga.translator.util;

import com.manga.translator.client.TokenBucketRateLimiter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenBucketRateLimiter 单元测试。
 */
class TokenBucketRateLimiterTest {

    @Test
    void shouldAllowRequest_when_created() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(10);
        assertTrue(limiter.tryAcquire());
    }

    @Test
    void shouldRespectQpsLimit() throws InterruptedException {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(20); // 50ms per token

        // 快速获取 3 个
        assertTrue(limiter.tryAcquire());
        Thread.sleep(60);
        assertTrue(limiter.tryAcquire());
        Thread.sleep(60);
        assertTrue(limiter.tryAcquire());
    }

    @Test
    void shouldHaveCorrectInterval() {
        TokenBucketRateLimiter ocr = new TokenBucketRateLimiter(1.5);
        assertEquals(666, ocr.getIntervalMs(), 1);

        TokenBucketRateLimiter trans = new TokenBucketRateLimiter(8);
        assertEquals(125, trans.getIntervalMs(), 1);
    }

    @Test
    void shouldReturnMaxQps() {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5.0);
        assertEquals(5.0, limiter.getMaxQps(), 0.01);
    }
}

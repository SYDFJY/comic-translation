package com.manga.translator.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CircuitBreaker 单元测试。
 */
class CircuitBreakerTest {

    @Test
    void shouldAllowRequest_when_closed() {
        CircuitBreaker cb = new CircuitBreaker("test");
        assertTrue(cb.allowRequest());
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    @Test
    void shouldOpenAfterFailures() {
        CircuitBreaker cb = new CircuitBreaker("test");
        // 连续失败 5 次
        for (int i = 0; i < 5; i++) {
            cb.onFailure();
        }
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
        assertFalse(cb.allowRequest());
    }

    @Test
    void shouldNotOpenBeforeThreshold() {
        CircuitBreaker cb = new CircuitBreaker("test");
        for (int i = 0; i < 4; i++) {
            cb.onFailure();
        }
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        assertTrue(cb.allowRequest());
    }

    @Test
    void shouldResetOnSuccess() {
        CircuitBreaker cb = new CircuitBreaker("test");
        for (int i = 0; i < 3; i++) {
            cb.onFailure();
        }
        cb.onSuccess(); // 重置计数
        assertEquals(0, cb.getConsecutiveFailures());
        assertTrue(cb.allowRequest());
    }

    @Test
    void shouldResetManually() {
        CircuitBreaker cb = new CircuitBreaker("test");
        for (int i = 0; i < 5; i++) {
            cb.onFailure();
        }
        assertTrue(cb.isLocked() || cb.getState() == CircuitBreaker.State.OPEN);

        cb.reset();
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        assertEquals(0, cb.getConsecutiveFailures());
    }
}

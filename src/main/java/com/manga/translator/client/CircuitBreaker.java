package com.manga.translator.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 熔断器。
 * <p>
 * 用于防止在 API 连续失败时反复发送无效请求。
 * <ul>
 *   <li>连续 5 次失败 → 熔断开启，暂停调用 60 秒</li>
 *   <li>60 秒后半开状态，允许 1 次探测请求</li>
 *   <li>探测成功 → 关闭熔断器，恢复正常</li>
 *   <li>探测失败 → 继续熔断，最多 3 次后锁定</li>
 * </ul>
 */
public class CircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);

    /** 熔断器状态 */
    public enum State {
        /** 正常 */
        CLOSED,
        /** 熔断中 */
        OPEN,
        /** 半开（允许探测） */
        HALF_OPEN
    }

    /** 连续失败次数阈值 */
    private static final int FAILURE_THRESHOLD = 5;
    /** 熔断持续时间（毫秒） */
    private static final long OPEN_TIMEOUT_MS = 60_000;
    /** 最大熔断次数（之后不再自动恢复） */
    private static final int MAX_OPEN_COUNT = 3;

    private final String name;
    private State state;
    private int consecutiveFailures;
    private int openCount;
    private long openedAt;

    /**
     * 创建熔断器。
     *
     * @param name 熔断器名称（用于日志区分）
     */
    public CircuitBreaker(String name) {
        this.name = name;
        this.state = State.CLOSED;
        this.consecutiveFailures = 0;
        this.openCount = 0;
        this.openedAt = 0;
    }

    /**
     * 检查是否允许发送请求。
     *
     * @return true 如果允许请求
     */
    public synchronized boolean allowRequest() {
        switch (state) {
            case CLOSED:
                return true;
            case OPEN:
                // 检查是否到了半开时间
                if (System.currentTimeMillis() - openedAt >= OPEN_TIMEOUT_MS) {
                    state = State.HALF_OPEN;
                    log.info("熔断器[{}] 进入半开状态，允许探测请求", name);
                    return true;
                }
                log.debug("熔断器[{}] 开启中，拒绝请求（剩余 {} 秒）",
                        name, (OPEN_TIMEOUT_MS - (System.currentTimeMillis() - openedAt)) / 1000);
                return false;
            case HALF_OPEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * 记录一次成功调用。
     */
    public synchronized void onSuccess() {
        if (state == State.HALF_OPEN) {
            log.info("熔断器[{}] 探测成功，恢复关闭状态", name);
            state = State.CLOSED;
            openCount = 0;
        }
        consecutiveFailures = 0;
    }

    /**
     * 记录一次失败调用。
     */
    public synchronized void onFailure() {
        consecutiveFailures++;

        if (consecutiveFailures >= FAILURE_THRESHOLD) {
            if (state == State.HALF_OPEN || state == State.CLOSED) {
                openCount++;
                state = State.OPEN;
                openedAt = System.currentTimeMillis();
                log.warn("熔断器[{}] 触发熔断（第{}/{}次），连续失败: {}, 熔断时长: {}秒",
                        name, openCount, MAX_OPEN_COUNT, consecutiveFailures, OPEN_TIMEOUT_MS / 1000);

                if (openCount >= MAX_OPEN_COUNT) {
                    log.error("熔断器[{}] 已达最大熔断次数({})，已锁定，需手动恢复", name, MAX_OPEN_COUNT);
                }
            }
        } else {
            log.debug("熔断器[{}] 失败计数: {}/{}", name, consecutiveFailures, FAILURE_THRESHOLD);
        }
    }

    /**
     * 手动重置熔断器（用户触发重试时调用）。
     */
    public synchronized void reset() {
        this.state = State.CLOSED;
        this.consecutiveFailures = 0;
        this.openCount = 0;
        this.openedAt = 0;
        log.info("熔断器[{}] 已手动重置", name);
    }

    // === Getter ===

    public State getState() { return state; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public int getOpenCount() { return openCount; }
    public String getName() { return name; }

    /**
     * 是否已锁定（不再自动恢复）。
     *
     * @return true 如果已锁定
     */
    public boolean isLocked() {
        return openCount >= MAX_OPEN_COUNT;
    }
}

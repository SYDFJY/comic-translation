package com.manga.translator;

/**
 * 限流异常。
 * <p>
 * 当 API 调用触发 QPS 限流或令牌桶耗尽时抛出。
 */
public class RateLimitException extends MangaTranslatorException {

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}

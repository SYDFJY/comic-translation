package com.manga.translator;

/**
 * API 鉴权异常。
 * <p>
 * 当百度 API 鉴权失败（access_token 无效、API Key 错误等）时抛出。
 */
public class AuthException extends MangaTranslatorException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

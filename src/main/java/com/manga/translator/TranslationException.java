package com.manga.translator;

/**
 * 翻译异常。
 * <p>
 * 当百度翻译 API 调用失败或返回异常结果时抛出。
 */
public class TranslationException extends MangaTranslatorException {

    public TranslationException(String message) {
        super(message);
    }

    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.manga.translator;

/**
 * 漫画翻译器自定义异常基类。
 * <p>
 * 所有自定义异常均继承此类，便于上层统一捕获和处理。
 */
public class MangaTranslatorException extends RuntimeException {

    public MangaTranslatorException(String message) {
        super(message);
    }

    public MangaTranslatorException(String message, Throwable cause) {
        super(message, cause);
    }
}

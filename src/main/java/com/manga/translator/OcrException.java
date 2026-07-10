package com.manga.translator;

/**
 * OCR 识别异常。
 * <p>
 * 当百度 OCR API 调用失败或返回异常结果时抛出。
 */
public class OcrException extends MangaTranslatorException {

    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }
}

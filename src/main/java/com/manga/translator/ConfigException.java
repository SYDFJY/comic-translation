package com.manga.translator;

/**
 * 配置异常。
 * <p>
 * 当配置文件读取失败、API Key 无效或配置格式错误时抛出。
 */
public class ConfigException extends MangaTranslatorException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}

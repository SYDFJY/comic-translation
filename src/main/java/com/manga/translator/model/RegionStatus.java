package com.manga.translator.model;

/**
 * 文字区域状态枚举。
 */
public enum RegionStatus {
    /** 待处理 */
    PENDING,
    /** 已翻译 */
    TRANSLATED,
    /** 用户已修正 */
    CORRECTED,
    /** 处理失败 */
    ERROR
}

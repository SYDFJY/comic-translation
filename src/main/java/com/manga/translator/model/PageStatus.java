package com.manga.translator.model;

/**
 * 页面状态枚举。
 */
public enum PageStatus {
    /** 空闲 */
    IDLE,
    /** 已加载 */
    LOADED,
    /** 翻译中 */
    TRANSLATING,
    /** 翻译完成 */
    COMPLETED,
    /** 修正中 */
    CORRECTING,
    /** 已导出 */
    EXPORTED
}

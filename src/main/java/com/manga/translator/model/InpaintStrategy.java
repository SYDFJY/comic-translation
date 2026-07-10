package com.manga.translator.model;

/**
 * 图片修补策略枚举。
 */
public enum InpaintStrategy {
    /** 白色覆盖填充 */
    WHITE_FILL,
    /** 取色填充（采样边缘像素均值） */
    AVG_COLOR_FILL
}

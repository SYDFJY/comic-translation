package com.manga.translator.service;

import com.manga.translator.TranslationException;
import com.manga.translator.model.TextRegion;

import java.util.List;

/**
 * 翻译服务接口。
 * <p>
 * 负责调用百度翻译 API 将识别文字翻译为目标语言。
 */
public interface TranslateService {

    /**
     * 翻译文字区域列表。
     * <p>
     * 将每个 TextRegion 的原文翻译为译文，回填到 translatedText 字段。
     *
     * @param regions 文字区域列表（原文需已设置）
     * @throws TranslationException 当翻译 API 调用失败时抛出
     */
    void translate(List<TextRegion> regions) throws TranslationException;
}

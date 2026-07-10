package com.manga.translator.service;

import com.manga.translator.OcrException;
import com.manga.translator.model.TextRegion;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * OCR 检测服务接口。
 * <p>
 * 负责调用百度 OCR API 识别图片中的文字区域。
 */
public interface OcrService {

    /**
     * 对指定图片执行 OCR 识别。
     *
     * @param image 漫画原图（非 null）
     * @return 识别到的文字区域列表（可能为空，不为 null）
     * @throws OcrException 当 API 调用失败时抛出
     */
    List<TextRegion> recognize(BufferedImage image) throws OcrException;
}

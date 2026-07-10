package com.manga.translator.service;

import com.manga.translator.model.TextRegion;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 图片修补服务接口。
 * <p>
 * 负责擦除原图中的文字区域，用背景色覆盖。
 */
public interface InpaintService {

    /**
     * 修补图片中的文字区域。
     *
     * @param image   原图（不会被修改，内部创建副本操作）
     * @param regions 文字区域列表（含扩展后的覆盖坐标）
     * @return 修补后的图片副本
     */
    BufferedImage inpaint(BufferedImage image, List<TextRegion> regions);
}

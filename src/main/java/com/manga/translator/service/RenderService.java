package com.manga.translator.service;

import com.manga.translator.model.TextRegion;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 文字回填渲染服务接口。
 * <p>
 * 负责将翻译后的文字渲染到修补后的图片上。
 */
public interface RenderService {

    /**
     * 将译文渲染到修补后的图片上。
     *
     * @param inpaintedImage 修补后的图片
     * @param regions        文字区域列表（含译文及排版参数）
     * @return 渲染完成后的最终图片
     */
    BufferedImage render(BufferedImage inpaintedImage, List<TextRegion> regions);
}

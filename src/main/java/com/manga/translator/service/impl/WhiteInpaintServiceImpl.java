package com.manga.translator.service.impl;

import com.manga.translator.model.InpaintStrategy;
import com.manga.translator.model.TextRegion;
import com.manga.translator.service.InpaintService;
import com.manga.translator.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 白色覆盖图片修补实现。
 * <p>
 * 使用白色圆角矩形覆盖原图中的文字区域。
 * 适用于白底气泡框的漫画。
 */
public class WhiteInpaintServiceImpl implements InpaintService {

    private static final Logger log = LoggerFactory.getLogger(WhiteInpaintServiceImpl.class);

    /** 圆角半径 */
    private static final int CORNER_RADIUS = 8;

    private final InpaintStrategy strategy;

    public WhiteInpaintServiceImpl() {
        this.strategy = InpaintStrategy.WHITE_FILL;
    }

    @Override
    public BufferedImage inpaint(BufferedImage image, List<TextRegion> regions) {
        if (image == null) {
            log.warn("修补输入图片为 null");
            return null;
        }
        if (regions == null || regions.isEmpty()) {
            log.info("无文字区域需要修补");
            return ImageUtil.copyImage(image);
        }

        // 创建原图副本
        BufferedImage result = ImageUtil.copyImage(image);
        Graphics2D g2d = result.createGraphics();

        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            for (TextRegion region : regions) {
                inpaintRegion(g2d, region);
            }

            log.info("图片修补完成: 区域数={}", regions.size());
        } finally {
            g2d.dispose();
        }

        return result;
    }

    /**
     * 修补单个文字区域。
     */
    private void inpaintRegion(Graphics2D g2d, TextRegion region) {
        int x = region.getExpandLeft();
        int y = region.getExpandTop();
        int w = region.getExpandWidth();
        int h = region.getExpandHeight();

        // 使用白色填充
        g2d.setColor(Color.WHITE);
        g2d.fill(new RoundRectangle2D.Double(x, y, w, h, CORNER_RADIUS, CORNER_RADIUS));

        log.debug("修补区域: id={}, x={}, y={}, w={}, h={}", region.getId(), x, y, w, h);
    }

    /**
     * 获取当前修补策略。
     *
     * @return 修补策略
     */
    public InpaintStrategy getStrategy() {
        return strategy;
    }
}

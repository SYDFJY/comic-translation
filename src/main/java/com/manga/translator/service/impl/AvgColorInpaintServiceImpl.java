package com.manga.translator.service.impl;

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
 * 取色填充图片修补实现。
 * <p>
 * 采样文字区域边缘像素的 RGB 均值作为填充颜色。
 * 适用于彩色背景漫画（非白底气泡框）。
 */
public class AvgColorInpaintServiceImpl implements InpaintService {

    private static final Logger log = LoggerFactory.getLogger(AvgColorInpaintServiceImpl.class);

    /** 圆角半径 */
    private static final int CORNER_RADIUS = 8;
    /** 边缘采样宽度（px） */
    private static final int SAMPLE_WIDTH = 2;

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

        BufferedImage result = ImageUtil.copyImage(image);
        Graphics2D g2d = result.createGraphics();

        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            for (TextRegion region : regions) {
                Color avgColor = sampleEdgeColor(image, region);
                g2d.setColor(avgColor);
                g2d.fill(new RoundRectangle2D.Double(
                        region.getExpandLeft(), region.getExpandTop(),
                        region.getExpandWidth(), region.getExpandHeight(),
                        CORNER_RADIUS, CORNER_RADIUS));
            }

            log.info("取色修补完成: 区域数={}", regions.size());
        } finally {
            g2d.dispose();
        }

        return result;
    }

    /**
     * 采样区域边缘的颜色均值。
     */
    private Color sampleEdgeColor(BufferedImage image, TextRegion region) {
        int x = region.getExpandLeft();
        int y = region.getExpandTop();
        int w = region.getExpandWidth();
        int h = region.getExpandHeight();

        int imgW = image.getWidth();
        int imgH = image.getHeight();

        long sumR = 0, sumG = 0, sumB = 0;
        int count = 0;

        // 上边缘
        for (int sx = x; sx < x + w && sx < imgW; sx++) {
            for (int sy = y; sy < y + SAMPLE_WIDTH && sy < imgH; sy++) {
                if (sx >= 0 && sy >= 0) {
                    int rgb = image.getRGB(sx, sy);
                    sumR += (rgb >> 16) & 0xFF;
                    sumG += (rgb >> 8) & 0xFF;
                    sumB += rgb & 0xFF;
                    count++;
                }
            }
        }

        // 下边缘
        for (int sx = x; sx < x + w && sx < imgW; sx++) {
            for (int sy = y + h - SAMPLE_WIDTH; sy < y + h && sy < imgH; sy++) {
                if (sx >= 0 && sy >= 0) {
                    int rgb = image.getRGB(sx, sy);
                    sumR += (rgb >> 16) & 0xFF;
                    sumG += (rgb >> 8) & 0xFF;
                    sumB += rgb & 0xFF;
                    count++;
                }
            }
        }

        // 左边缘
        for (int sx = x; sx < x + SAMPLE_WIDTH && sx < imgW; sx++) {
            for (int sy = y; sy < y + h && sy < imgH; sy++) {
                if (sx >= 0 && sy >= 0) {
                    int rgb = image.getRGB(sx, sy);
                    sumR += (rgb >> 16) & 0xFF;
                    sumG += (rgb >> 8) & 0xFF;
                    sumB += rgb & 0xFF;
                    count++;
                }
            }
        }

        // 右边缘
        for (int sx = x + w - SAMPLE_WIDTH; sx < x + w && sx < imgW; sx++) {
            for (int sy = y; sy < y + h && sy < imgH; sy++) {
                if (sx >= 0 && sy >= 0) {
                    int rgb = image.getRGB(sx, sy);
                    sumR += (rgb >> 16) & 0xFF;
                    sumG += (rgb >> 8) & 0xFF;
                    sumB += rgb & 0xFF;
                    count++;
                }
            }
        }

        if (count == 0) {
            return Color.WHITE;
        }

        return new Color(
                (int) (sumR / count),
                (int) (sumG / count),
                (int) (sumB / count)
        );
    }
}

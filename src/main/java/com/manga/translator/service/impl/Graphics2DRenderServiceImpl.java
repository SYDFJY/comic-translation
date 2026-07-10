package com.manga.translator.service.impl;

import com.manga.translator.model.TextRegion;
import com.manga.translator.service.RenderService;
import com.manga.translator.util.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Graphics2D 文字回填渲染实现。
 * <p>
 * 使用 Java Graphics2D 将译文渲染到修补后的图片上。
 * 支持横排居中对齐和竖排逐字符渲染，字号自适应。
 */
public class Graphics2DRenderServiceImpl implements RenderService {

    private static final Logger log = LoggerFactory.getLogger(Graphics2DRenderServiceImpl.class);

    /** 横排字号基数比例（框高的百分比） */
    private static final double HORIZONTAL_FONT_RATIO = 0.65;
    /** 横排文字最大宽度比例（框宽的百分比） */
    private static final double HORIZONTAL_MAX_WIDTH_RATIO = 0.9;
    /** 竖排字号：框宽比例 */
    private static final double VERTICAL_FONT_WIDTH_RATIO = 0.7;
    /** 竖排字号：框高/字数比例 */
    private static final double VERTICAL_FONT_HEIGHT_RATIO = 0.85;
    /** 竖排列宽因子 */
    private static final double VERTICAL_COL_WIDTH_FACTOR = 1.3;
    /** 竖排行高因子 */
    private static final double VERTICAL_LINE_HEIGHT_FACTOR = 1.2;
    /** 最低字号 */
    private static final float MIN_FONT_SIZE = 10f;

    /** 回填默认字体 */
    private static final String DEFAULT_FONT = "SansSerif";

    private final String fontName;

    public Graphics2DRenderServiceImpl() {
        this.fontName = DEFAULT_FONT;
    }

    public Graphics2DRenderServiceImpl(String fontName) {
        this.fontName = fontName != null ? fontName : DEFAULT_FONT;
    }

    @Override
    public BufferedImage render(BufferedImage inpaintedImage, List<TextRegion> regions) {
        if (inpaintedImage == null) {
            log.warn("渲染输入图片为 null");
            return null;
        }
        if (regions == null || regions.isEmpty()) {
            log.info("无文字区域需要渲染");
            return ImageUtil.copyImage(inpaintedImage);
        }

        BufferedImage result = ImageUtil.copyImage(inpaintedImage);
        Graphics2D g2d = result.createGraphics();

        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g2d.setColor(Color.BLACK);

            int renderedCount = 0;
            for (TextRegion region : regions) {
                if (renderRegion(g2d, region)) {
                    renderedCount++;
                }
            }

            log.info("渲染完成: 渲染区域数={}, 总数={}", renderedCount, regions.size());
        } finally {
            g2d.dispose();
        }

        return result;
    }

    /**
     * 渲染单个文字区域。
     *
     * @return true 如果渲染成功
     */
    private boolean renderRegion(Graphics2D g2d, TextRegion region) {
        String text = region.getDisplayText();
        if (text == null || text.isEmpty()) {
            return false;
        }

        int areaW = region.getExpandWidth();
        int areaH = region.getExpandHeight();

        if (areaW <= 0 || areaH <= 0) {
            return false;
        }

        if (region.getDirection() == 1) {
            // 竖排渲染
            drawVertical(g2d, text, region, areaW, areaH);
        } else {
            // 横排渲染
            drawHorizontal(g2d, text, region, areaW, areaH);
        }

        return true;
    }

    /**
     * 横排文字渲染（居中对齐，字号自适应）。
     */
    private void drawHorizontal(Graphics2D g2d, String text, TextRegion region,
                                int areaW, int areaH) {
        float fontSize = calculateHorizontalFontSize(g2d, text, areaW, areaH);
        Font font = new Font(fontName, Font.PLAIN, Math.round(fontSize));
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        int textW = fm.stringWidth(text);

        int textX = region.getExpandLeft() + (areaW - textW) / 2;
        int textY = region.getExpandTop() + (areaH - fm.getHeight()) / 2 + fm.getAscent();

        g2d.drawString(text, textX, textY);
    }

    /**
     * 竖排文字渲染（从右到左逐列，每列从上到下逐字符）。
     */
    private void drawVertical(Graphics2D g2d, String text, TextRegion region,
                              int areaW, int areaH) {
        float fontSize = calculateVerticalFontSize(text, areaW, areaH);
        Font font = new Font(fontName, Font.PLAIN, Math.round(fontSize));
        g2d.setFont(font);

        float colW = fontSize * (float) VERTICAL_COL_WIDTH_FACTOR;
        float charH = fontSize * (float) VERTICAL_LINE_HEIGHT_FACTOR;

        float totalWidth = text.length() * colW;
        float startX = region.getExpandLeft() + (areaW - totalWidth) / 2 + colW / 2;
        float startY = region.getExpandTop() + charH;

        for (int i = 0; i < text.length(); i++) {
            float x = startX + i * colW;
            float y = startY;
            g2d.drawString(String.valueOf(text.charAt(i)), x, y);
        }
    }

    /**
     * 计算横排字号。
     * <p>
     * 基础字号 = 框高 × 65%。如果文字溢出的 90% 框宽，按比例缩小。
     */
    private float calculateHorizontalFontSize(Graphics2D g2d, String text, int areaW, int areaH) {
        float baseSize = areaH * (float) HORIZONTAL_FONT_RATIO;
        baseSize = Math.max(baseSize, MIN_FONT_SIZE);

        Font testFont = new Font(fontName, Font.PLAIN, Math.round(baseSize));
        FontMetrics fm = g2d.getFontMetrics(testFont);
        int textW = fm.stringWidth(text);

        if (textW > areaW * HORIZONTAL_MAX_WIDTH_RATIO && textW > 0) {
            baseSize *= (float) (areaW * HORIZONTAL_MAX_WIDTH_RATIO) / textW;
        }

        return Math.max(baseSize, MIN_FONT_SIZE);
    }

    /**
     * 计算竖排字号。
     * <p>
     * 取框宽×0.7 和 框高/字数×0.85 中的较小值。
     */
    private float calculateVerticalFontSize(String text, int areaW, int areaH) {
        float sizeByWidth = areaW * (float) VERTICAL_FONT_WIDTH_RATIO;
        float sizeByHeight = (areaH / (float) text.length()) * (float) VERTICAL_FONT_HEIGHT_RATIO;
        return Math.max(Math.min(sizeByWidth, sizeByHeight), MIN_FONT_SIZE);
    }
}

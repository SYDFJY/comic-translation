package com.manga.translator.service.impl;

import com.manga.translator.model.*;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WhiteInpaintServiceImpl 单元测试。
 */
class WhiteInpaintServiceImplTest {

    @Test
    void shouldReturnNull_when_imageIsNull() {
        WhiteInpaintServiceImpl service = new WhiteInpaintServiceImpl();
        assertNull(service.inpaint(null, new ArrayList<>()));
    }

    @Test
    void shouldReturnCopy_when_regionsEmpty() {
        WhiteInpaintServiceImpl service = new WhiteInpaintServiceImpl();
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage result = service.inpaint(image, new ArrayList<>());
        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    @Test
    void shouldFillRegionWithWhite() {
        WhiteInpaintServiceImpl service = new WhiteInpaintServiceImpl();
        // 创建一张黑色背景的图
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        // 添加一个文字区域
        TextRegion region = new TextRegion();
        region.setId(1);
        region.setExpandLeft(10);
        region.setExpandTop(10);
        region.setExpandWidth(80);
        region.setExpandHeight(30);

        List<TextRegion> regions = new ArrayList<>();
        regions.add(region);

        BufferedImage result = service.inpaint(image, regions);

        // 验证区域中心点变为白色
        int rgb = result.getRGB(50, 25);
        Color color = new Color(rgb);
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
    }
}

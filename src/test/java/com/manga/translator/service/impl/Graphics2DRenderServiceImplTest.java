package com.manga.translator.service.impl;

import com.manga.translator.model.TextRegion;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Graphics2DRenderServiceImpl 单元测试。
 */
class Graphics2DRenderServiceImplTest {

    @Test
    void shouldReturnNull_when_imageIsNull() {
        Graphics2DRenderServiceImpl service = new Graphics2DRenderServiceImpl();
        assertNull(service.render(null, new ArrayList<>()));
    }

    @Test
    void shouldReturnCopy_when_regionsEmpty() {
        Graphics2DRenderServiceImpl service = new Graphics2DRenderServiceImpl();
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage result = service.render(image, new ArrayList<>());
        assertNotNull(result);
        assertEquals(100, result.getWidth());
        assertEquals(100, result.getHeight());
    }

    @Test
    void shouldRenderHorizontalText() {
        Graphics2DRenderServiceImpl service = new Graphics2DRenderServiceImpl();
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 100);
        g.dispose();

        TextRegion region = new TextRegion();
        region.setId(1);
        region.setExpandLeft(10);
        region.setExpandTop(10);
        region.setExpandWidth(180);
        region.setExpandHeight(30);
        region.setOriginalText("测试");
        region.setTranslatedText("Hello Test");
        region.setDirection(0);

        List<TextRegion> regions = List.of(region);
        BufferedImage result = service.render(image, regions);
        assertNotNull(result);
    }

    @Test
    void shouldRenderVerticalText() {
        Graphics2DRenderServiceImpl service = new Graphics2DRenderServiceImpl();
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 200);
        g.dispose();

        TextRegion region = new TextRegion();
        region.setId(1);
        region.setExpandLeft(10);
        region.setExpandTop(10);
        region.setExpandWidth(30);
        region.setExpandHeight(180);
        region.setOriginalText("縦書き");
        region.setTranslatedText("竖排测试");
        region.setDirection(1);

        List<TextRegion> regions = List.of(region);
        BufferedImage result = service.render(image, regions);
        assertNotNull(result);
    }

    @Test
    void shouldSkipEmptyTextRegions() {
        Graphics2DRenderServiceImpl service = new Graphics2DRenderServiceImpl();
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

        TextRegion region = new TextRegion();
        region.setId(1);
        region.setExpandLeft(10);
        region.setExpandTop(10);
        region.setExpandWidth(50);
        region.setExpandHeight(20);
        region.setOriginalText("test");
        region.setTranslatedText("");  // 空译文

        List<TextRegion> regions = List.of(region);
        BufferedImage result = service.render(image, regions);
        assertNotNull(result);
    }
}

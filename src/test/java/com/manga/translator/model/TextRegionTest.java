package com.manga.translator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TextRegion 数据模型单元测试。
 */
class TextRegionTest {

    @Test
    void shouldReturnCorrectedText_when_correctedTextExists() {
        TextRegion region = new TextRegion();
        region.setOriginalText("こんにちは");
        region.setTranslatedText("你好");
        region.setCorrectedText("您好");

        assertEquals("您好", region.getDisplayText());
    }

    @Test
    void shouldReturnTranslatedText_when_noCorrection() {
        TextRegion region = new TextRegion();
        region.setOriginalText("こんにちは");
        region.setTranslatedText("你好");

        assertEquals("你好", region.getDisplayText());
    }

    @Test
    void shouldReturnOriginalText_when_noTranslation() {
        TextRegion region = new TextRegion();
        region.setOriginalText("こんにちは");

        assertEquals("こんにちは", region.getDisplayText());
    }

    @Test
    void shouldBePending_when_created() {
        TextRegion region = new TextRegion();
        assertEquals(RegionStatus.PENDING, region.getStatus());
    }

    @Test
    void shouldHaveDefaultId() {
        TextRegion region = new TextRegion();
        assertEquals(0, region.getId());
        region.setId(5);
        assertEquals(5, region.getId());
    }
}

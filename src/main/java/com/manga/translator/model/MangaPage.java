package com.manga.translator.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 漫画页面模型。
 * <p>
 * 表示一个漫画页面文件，包含原图、翻译结果、文字区域列表等。
 */
public class MangaPage {

    /** 文件名 */
    private String fileName;
    /** 文件绝对路径 */
    private String filePath;
    /** 原图 */
    private BufferedImage originalImage;
    /** 翻译后的图 */
    private BufferedImage translatedImage;
    /** 文字区域列表 */
    private List<TextRegion> textRegions;
    /** 页面处理状态 */
    private PageStatus status;
    /** 导入时间戳 */
    private long timestamp;

    public MangaPage() {
        this.textRegions = new ArrayList<>();
        this.status = PageStatus.IDLE;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 释放图片内存。
     */
    public void disposeImages() {
        if (originalImage != null) {
            originalImage.flush();
        }
        if (translatedImage != null) {
            translatedImage.flush();
        }
    }

    // === Getter & Setter ===

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public BufferedImage getOriginalImage() { return originalImage; }
    public void setOriginalImage(BufferedImage originalImage) { this.originalImage = originalImage; }

    public BufferedImage getTranslatedImage() { return translatedImage; }
    public void setTranslatedImage(BufferedImage translatedImage) { this.translatedImage = translatedImage; }

    public List<TextRegion> getTextRegions() { return textRegions; }
    public void setTextRegions(List<TextRegion> textRegions) { this.textRegions = textRegions; }

    public PageStatus getStatus() { return status; }
    public void setStatus(PageStatus status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MangaPage mangaPage = (MangaPage) o;
        return Objects.equals(filePath, mangaPage.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }

    @Override
    public String toString() {
        return "MangaPage{" +
                "fileName='" + fileName + '\'' +
                ", status=" + status +
                ", textRegions=" + (textRegions != null ? textRegions.size() : 0) +
                '}';
    }
}

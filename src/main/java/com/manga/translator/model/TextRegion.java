package com.manga.translator.model;

import java.util.Objects;

/**
 * 文字区域模型。
 * <p>
 * 表示 OCR 识别出的一个文字区域，包含坐标、原文、译文、排版方向等信息。
 */
public class TextRegion {

    /** 区域编号，从 1 开始 */
    private int id;
    /** OCR 检测的原始左上角 x 坐标 */
    private int left;
    /** OCR 检测的原始左上角 y 坐标 */
    private int top;
    /** OCR 检测的原始宽度 */
    private int width;
    /** OCR 检测的原始高度 */
    private int height;
    /** 扩展后的覆盖区域左上角 x 坐标 */
    private int expandLeft;
    /** 扩展后的覆盖区域左上角 y 坐标 */
    private int expandTop;
    /** 扩展后的覆盖区域宽度 */
    private int expandWidth;
    /** 扩展后的覆盖区域高度 */
    private int expandHeight;
    /** OCR 识别原文 */
    private String originalText;
    /** 百度翻译译文 */
    private String translatedText;
    /** 用户修正后的译文（优先使用） */
    private String correctedText;
    /** 排版方向：0=横排, 1=竖排 */
    private int direction;
    /** 检测到的语言代码（如 JAP/ENG/KOR） */
    private String language;
    /** OCR 置信度（0.0 ~ 1.0） */
    private double probability;
    /** 当前处理状态 */
    private RegionStatus status;

    public TextRegion() {
        this.status = RegionStatus.PENDING;
    }

    /**
     * 获取最终译文。
     * <p>
     * 优先返回用户修正译文，其次返回机器翻译译文。
     *
     * @return 最终译文
     */
    public String getDisplayText() {
        if (correctedText != null && !correctedText.isEmpty()) {
            return correctedText;
        }
        return translatedText != null ? translatedText : originalText;
    }

    // === Getter & Setter ===

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLeft() { return left; }
    public void setLeft(int left) { this.left = left; }

    public int getTop() { return top; }
    public void setTop(int top) { this.top = top; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int getExpandLeft() { return expandLeft; }
    public void setExpandLeft(int expandLeft) { this.expandLeft = expandLeft; }

    public int getExpandTop() { return expandTop; }
    public void setExpandTop(int expandTop) { this.expandTop = expandTop; }

    public int getExpandWidth() { return expandWidth; }
    public void setExpandWidth(int expandWidth) { this.expandWidth = expandWidth; }

    public int getExpandHeight() { return expandHeight; }
    public void setExpandHeight(int expandHeight) { this.expandHeight = expandHeight; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public String getCorrectedText() { return correctedText; }
    public void setCorrectedText(String correctedText) { this.correctedText = correctedText; }

    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public double getProbability() { return probability; }
    public void setProbability(double probability) { this.probability = probability; }

    public RegionStatus getStatus() { return status; }
    public void setStatus(RegionStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextRegion that = (TextRegion) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TextRegion{" +
                "id=" + id +
                ", originalText='" + originalText + '\'' +
                ", translatedText='" + translatedText + '\'' +
                ", direction=" + direction +
                ", probability=" + probability +
                ", status=" + status +
                '}';
    }
}

package com.manga.translator.pipeline;

import com.manga.translator.model.MangaPage;
import com.manga.translator.model.TextRegion;
import com.manga.translator.model.TranslationConfig;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * 管线上下文。
 * <p>
 * 在管线各步骤之间传递数据。
 * 每个步骤的输入是上一个步骤的输出。
 */
public class PipelineContext {

    /** 当前处理的漫画页面 */
    private MangaPage mangaPage;
    /** 翻译配置 */
    private TranslationConfig config;

    // 各步骤产出
    /** Step 1 后：OCR 结果 */
    private List<TextRegion> textRegions;
    /** Step 2 后：清洗合并后的文字区域 */
    private List<TextRegion> cleanedRegions;
    /** Step 4 后：修补后的图片 */
    private BufferedImage inpaintedImage;
    /** Step 5 后：最终结果图 */
    private BufferedImage resultImage;

    /** 当前进度（0.0 ~ 1.0） */
    private double progress;
    /** 当前步骤名称 */
    private String currentStepName;
    /** 是否已取消 */
    private volatile boolean cancelled;

    public PipelineContext() {
        this.textRegions = new ArrayList<>();
        this.cleanedRegions = new ArrayList<>();
        this.progress = 0.0;
        this.cancelled = false;
    }

    // === 进度管理 ===

    public void setProgress(double progress) {
        this.progress = Math.min(1.0, Math.max(0.0, progress));
    }

    public double getProgress() { return progress; }

    public void setCurrentStepName(String name) {
        this.currentStepName = name;
    }

    public String getCurrentStepName() { return currentStepName; }

    public void cancel() { this.cancelled = true; }
    public boolean isCancelled() { return cancelled; }

    // === Getter & Setter ===

    public MangaPage getMangaPage() { return mangaPage; }
    public void setMangaPage(MangaPage mangaPage) { this.mangaPage = mangaPage; }

    public TranslationConfig getConfig() { return config; }
    public void setConfig(TranslationConfig config) { this.config = config; }

    public List<TextRegion> getTextRegions() { return textRegions; }
    public void setTextRegions(List<TextRegion> textRegions) { this.textRegions = textRegions; }

    public List<TextRegion> getCleanedRegions() { return cleanedRegions; }
    public void setCleanedRegions(List<TextRegion> cleanedRegions) { this.cleanedRegions = cleanedRegions; }

    public BufferedImage getInpaintedImage() { return inpaintedImage; }
    public void setInpaintedImage(BufferedImage inpaintedImage) { this.inpaintedImage = inpaintedImage; }

    public BufferedImage getResultImage() { return resultImage; }
    public void setResultImage(BufferedImage resultImage) { this.resultImage = resultImage; }

    /**
     * 获取原图。
     *
     * @return 原图 BufferedImage
     */
    public BufferedImage getOriginalImage() {
        return mangaPage != null ? mangaPage.getOriginalImage() : null;
    }
}

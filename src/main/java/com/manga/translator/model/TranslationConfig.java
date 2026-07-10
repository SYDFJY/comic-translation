package com.manga.translator.model;

/**
 * 翻译配置模型。
 * <p>
 * 存储用户配置的百度 API 密钥、翻译选项、渲染与导出参数。
 */
public class TranslationConfig {

    /** 百度 OCR API Key */
    private String ocrApiKey;
    /** 百度 OCR Secret Key */
    private String ocrSecretKey;
    /** 百度翻译 App ID */
    private String translateAppId;
    /** 百度翻译密钥 */
    private String translateSecretKey;
    /** 源语言（默认 jp） */
    private String sourceLanguage = "jp";
    /** 目标语言（默认 zh） */
    private String targetLanguage = "zh";
    /** 气泡框扩展量（默认 12px） */
    private int expandMargin = 12;
    /** 修补策略（默认白色覆盖） */
    private InpaintStrategy inpaintStrategy = InpaintStrategy.WHITE_FILL;
    /** OCR 版本（默认通用版） */
    private OcrVersion ocrVersion = OcrVersion.GENERAL_BASIC;
    /** 回填字体名 */
    private String fontName = "Microsoft YaHei";
    /** 导出格式（默认 png） */
    private String exportFormat = "png";

    // === Getter & Setter ===

    public String getOcrApiKey() { return ocrApiKey; }
    public void setOcrApiKey(String ocrApiKey) { this.ocrApiKey = ocrApiKey; }

    public String getOcrSecretKey() { return ocrSecretKey; }
    public void setOcrSecretKey(String ocrSecretKey) { this.ocrSecretKey = ocrSecretKey; }

    public String getTranslateAppId() { return translateAppId; }
    public void setTranslateAppId(String translateAppId) { this.translateAppId = translateAppId; }

    public String getTranslateSecretKey() { return translateSecretKey; }
    public void setTranslateSecretKey(String translateSecretKey) { this.translateSecretKey = translateSecretKey; }

    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public int getExpandMargin() { return expandMargin; }
    public void setExpandMargin(int expandMargin) { this.expandMargin = expandMargin; }

    public InpaintStrategy getInpaintStrategy() { return inpaintStrategy; }
    public void setInpaintStrategy(InpaintStrategy inpaintStrategy) { this.inpaintStrategy = inpaintStrategy; }

    public OcrVersion getOcrVersion() { return ocrVersion; }
    public void setOcrVersion(OcrVersion ocrVersion) { this.ocrVersion = ocrVersion; }

    public String getFontName() { return fontName; }
    public void setFontName(String fontName) { this.fontName = fontName; }

    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }

    /**
     * 检查配置是否有效（API Key 是否已配置）。
     *
     * @return true 如果至少 OCR API Key 已配置
     */
    public boolean isValid() {
        return ocrApiKey != null && !ocrApiKey.isEmpty()
                && ocrSecretKey != null && !ocrSecretKey.isEmpty();
    }
}

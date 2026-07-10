package com.manga.translator.pipeline;

import com.manga.translator.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OCR 检测步骤。
 * <p>
 * 调用百度 OCR API 检测图片中的文字区域。
 * 权重：20（占管线 20% 进度）
 */
public class OcrStep implements TranslationStep {

    private static final Logger log = LoggerFactory.getLogger(OcrStep.class);

    private final OcrService ocrService;

    public OcrStep(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Override
    public void execute(PipelineContext context) throws Exception {
        log.info("开始 OCR 检测步骤");

        if (context.isCancelled()) return;

        var regions = ocrService.recognize(context.getOriginalImage());
        context.setTextRegions(regions);

        log.info("OCR 检测完成: 检测到 {} 个文字区域", regions.size());
    }

    @Override
    public String getStepName() {
        return "OCR 检测";
    }

    @Override
    public int getWeight() {
        return 20;
    }
}

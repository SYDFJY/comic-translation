package com.manga.translator.pipeline;

import com.manga.translator.service.TranslateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI 翻译步骤。
 * <p>
 * 调用百度翻译 API 将清洗后的文字翻译为目标语言。
 * 权重：30（占管线 30% 进度）
 */
public class TranslateStep implements TranslationStep {

    private static final Logger log = LoggerFactory.getLogger(TranslateStep.class);

    private final TranslateService translateService;

    public TranslateStep(TranslateService translateService) {
        this.translateService = translateService;
    }

    @Override
    public void execute(PipelineContext context) throws Exception {
        log.info("开始翻译步骤");

        if (context.isCancelled()) return;

        var regions = context.getCleanedRegions();
        if (regions == null || regions.isEmpty()) {
            log.info("无文字区域需要翻译");
            return;
        }

        translateService.translate(regions);

        int translatedCount = (int) regions.stream()
                .filter(r -> r.getTranslatedText() != null)
                .count();
        log.info("翻译完成: 成功={}/{}", translatedCount, regions.size());
    }

    @Override
    public String getStepName() {
        return "AI 翻译";
    }

    @Override
    public int getWeight() {
        return 30;
    }
}

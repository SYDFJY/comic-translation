package com.manga.translator.pipeline;

import com.manga.translator.service.RenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文字回填步骤。
 * <p>
 * 将翻译后的文字渲染到修补后的图片上。
 * 权重：20（占管线 20% 进度）
 */
public class RenderStep implements TranslationStep {

    private static final Logger log = LoggerFactory.getLogger(RenderStep.class);

    private final RenderService renderService;

    public RenderStep(RenderService renderService) {
        this.renderService = renderService;
    }

    @Override
    public void execute(PipelineContext context) {
        log.info("开始文字回填步骤");

        if (context.isCancelled()) return;

        var inpainted = context.getInpaintedImage();
        var regions = context.getCleanedRegions();

        if (inpainted == null) {
            log.warn("修补图片为 null，使用原图回填");
            inpainted = context.getOriginalImage();
        }

        var result = renderService.render(inpainted, regions);
        context.setResultImage(result);

        // 同时更新 MangaPage 的翻译结果图
        if (context.getMangaPage() != null) {
            context.getMangaPage().setTranslatedImage(result);
        }

        log.info("文字回填完成");
    }

    @Override
    public String getStepName() {
        return "文字回填";
    }

    @Override
    public int getWeight() {
        return 20;
    }
}

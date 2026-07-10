package com.manga.translator.pipeline;

import com.manga.translator.service.InpaintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 图片修补步骤。
 * <p>
 * 擦除原图中的文字区域，用背景色覆盖。
 * 权重：20（占管线 20% 进度）
 */
public class InpaintStep implements TranslationStep {

    private static final Logger log = LoggerFactory.getLogger(InpaintStep.class);

    private final InpaintService inpaintService;

    public InpaintStep(InpaintService inpaintService) {
        this.inpaintService = inpaintService;
    }

    @Override
    public void execute(PipelineContext context) {
        log.info("开始图片修补步骤");

        if (context.isCancelled()) return;

        var original = context.getOriginalImage();
        var regions = context.getCleanedRegions();

        if (original == null) {
            log.warn("原图为 null，跳过修补");
            return;
        }

        var inpainted = inpaintService.inpaint(original, regions);
        context.setInpaintedImage(inpainted);

        log.info("图片修补完成");
    }

    @Override
    public String getStepName() {
        return "图片修补";
    }

    @Override
    public int getWeight() {
        return 20;
    }
}

package com.manga.translator.pipeline;

import com.manga.translator.model.MangaPage;
import com.manga.translator.model.PageStatus;
import com.manga.translator.model.TranslationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 翻译管线编排器。
 * <p>
 * 按固定顺序编排 5 步翻译流程：
 * OCR 检测(20%) → 文本清洗(10%) → AI 翻译(30%) → 图片修补(20%) → 文字回填(20%)
 * <p>
 * 总进度权重：20 + 10 + 30 + 20 + 20 = 100
 */
public class TranslationPipeline {

    private static final Logger log = LoggerFactory.getLogger(TranslationPipeline.class);

    /** 管线步骤列表 */
    private final List<TranslationStep> steps;

    /** 事件总线（用于 UI 进度反馈） */
    private final PipelineEventBus eventBus;

    /** 取消标记 */
    private volatile boolean cancelled;

    public TranslationPipeline(PipelineEventBus eventBus) {
        this.steps = new ArrayList<>();
        this.eventBus = eventBus;
        this.cancelled = false;
    }

    /**
     * 添加步骤到管线。
     *
     * @param step 翻译步骤
     */
    public void addStep(TranslationStep step) {
        steps.add(step);
    }

    /**
     * 执行完整翻译管线。
     *
     * @param mangaPage 待翻译的漫画页面
     * @param config    翻译配置
     * @return 管线上下文（包含所有步骤的结果）
     */
    public PipelineContext execute(MangaPage mangaPage, TranslationConfig config) {
        this.cancelled = false;

        PipelineContext context = new PipelineContext();
        context.setMangaPage(mangaPage);
        context.setConfig(config);
        context.setCurrentStepName("初始化");

        mangaPage.setStatus(PageStatus.TRANSLATING);
        log.info("翻译管线启动: file={}", mangaPage.getFileName());

        double baseProgress = 0;
        int totalWeight = calculateTotalWeight();
        if (totalWeight == 0) totalWeight = 100;

        for (int i = 0; i < steps.size(); i++) {
            TranslationStep step = steps.get(i);

            if (cancelled) {
                log.warn("翻译管线已取消");
                break;
            }

            context.setCurrentStepName(step.getStepName());
            log.info("执行步骤 [{}/{}]: {}", i + 1, steps.size(), step.getStepName());

            try {
                step.execute(context);

                // 更新进度
                double stepProgress = (double) step.getWeight() / totalWeight;
                baseProgress += stepProgress;
                context.setProgress(baseProgress);

                eventBus.fireProgress(this, baseProgress, step.getStepName(),
                        step.getStepName() + " 完成");

            } catch (Exception e) {
                log.error("步骤执行失败: {}", step.getStepName(), e);
                eventBus.fireError(this, baseProgress, step.getStepName(),
                        step.getStepName() + " 失败: " + e.getMessage());

                // 管线中止，不再执行后续步骤
                mangaPage.setStatus(PageStatus.COMPLETED); // 部分完成
                return context;
            }
        }

        // 更新页面状态
        if (cancelled) {
            mangaPage.setStatus(PageStatus.LOADED);
        } else {
            mangaPage.setStatus(PageStatus.COMPLETED);
            eventBus.fireComplete(this, "翻译完成！共处理 " + (context.getCleanedRegions() != null ? context.getCleanedRegions().size() : 0) + " 个文字区域");
        }

        log.info("翻译管线完成: file={}, status={}", mangaPage.getFileName(), mangaPage.getStatus());
        return context;
    }

    /**
     * 取消当前翻译。
     */
    public void cancel() {
        this.cancelled = true;
        log.info("翻译管线取消请求已发出");
    }

    /**
     * 获取事件总线。
     *
     * @return 事件总线
     */
    public PipelineEventBus getEventBus() {
        return eventBus;
    }

    /**
     * 计算步骤总权重。
     */
    private int calculateTotalWeight() {
        return steps.stream().mapToInt(TranslationStep::getWeight).sum();
    }
}

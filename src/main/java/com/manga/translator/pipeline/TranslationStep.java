package com.manga.translator.pipeline;

/**
 * 翻译管线步骤接口。
 * <p>
 * 每个步骤独立实现此接口，步骤间通过 PipelineContext 传递数据。
 */
public interface TranslationStep {

    /**
     * 执行当前步骤。
     *
     * @param context 管线上下文（含输入数据和配置）
     * @throws Exception 如果步骤执行失败
     */
    void execute(PipelineContext context) throws Exception;

    /**
     * 获取步骤名称。
     *
     * @return 步骤名称（用于日志和进度显示）
     */
    String getStepName();

    /**
     * 获取步骤权重（用于计算进度百分比）。
     * 5 步权重之和应为 100。
     *
     * @return 权重值
     */
    int getWeight();
}

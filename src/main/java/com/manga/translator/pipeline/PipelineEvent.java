package com.manga.translator.pipeline;

import java.util.EventObject;

/**
 * 管线事件。
 * <p>
 * 当管线进度变化时触发，用于 UI 更新。
 */
public class PipelineEvent extends EventObject {

    private final double progress;
    private final String stepName;
    private final String message;

    /**
     * 创建管线事件。
     *
     * @param source   事件源
     * @param progress 当前进度 (0.0 ~ 1.0)
     * @param stepName 当前步骤名称
     * @param message  事件消息
     */
    public PipelineEvent(Object source, double progress, String stepName, String message) {
        super(source);
        this.progress = progress;
        this.stepName = stepName;
        this.message = message;
    }

    public double getProgress() { return progress; }
    public String getStepName() { return stepName; }
    public String getMessage() { return message; }
}

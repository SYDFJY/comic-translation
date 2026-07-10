package com.manga.translator.pipeline;

/**
 * 管线事件总线。
 * <p>
 * 负责在 Pipeline 和 UI 之间传递进度事件。
 */
public class PipelineEventBus {

    private PipelineListener listener;

    /**
     * 注册监听器。
     *
     * @param listener 进度监听器
     */
    public void addListener(PipelineListener listener) {
        this.listener = listener;
    }

    /**
     * 移除监听器。
     */
    public void removeListener() {
        this.listener = null;
    }

    /**
     * 触发进度事件。
     *
     * @param source   事件源
     * @param progress 进度
     * @param stepName 步骤名
     * @param message  消息
     */
    public void fireProgress(Object source, double progress, String stepName, String message) {
        if (listener != null) {
            listener.onProgress(new PipelineEvent(source, progress, stepName, message));
        }
    }

    /**
     * 触发错误事件。
     *
     * @param source   事件源
     * @param progress 进度
     * @param stepName 步骤名
     * @param message  消息
     */
    public void fireError(Object source, double progress, String stepName, String message) {
        if (listener != null) {
            listener.onError(new PipelineEvent(source, progress, stepName, message));
        }
    }

    /**
     * 触发完成事件。
     *
     * @param source  事件源
     * @param message 消息
     */
    public void fireComplete(Object source, String message) {
        if (listener != null) {
            listener.onComplete(new PipelineEvent(source, 1.0, "完成", message));
        }
    }
}

package com.manga.translator.pipeline;

import java.util.EventListener;

/**
 * 管线事件监听器接口。
 */
public interface PipelineListener extends EventListener {

    /**
     * 进度更新事件。
     *
     * @param event 管线事件
     */
    void onProgress(PipelineEvent event);

    /**
     * 错误事件。
     *
     * @param event 管线事件
     */
    void onError(PipelineEvent event);

    /**
     * 完成事件。
     *
     * @param event 管线事件
     */
    void onComplete(PipelineEvent event);
}

package com.manga.translator.ui;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * 底部状态栏。
 * <p>
 * 显示翻译进度条、进度文字、日志展开按钮。
 */
public class BottomBar extends HBox {

    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final Label logBtn;

    private static final String BG_COLOR = "#2A2A4A";
    private static final String TEXT_COLOR = "#606070";
    private static final String BTN_BG = "#252540";

    public BottomBar() {
        setStyle("-fx-background-color: " + BG_COLOR + ";"
                + "-fx-background-radius: 8px;"
                + "-fx-padding: 8px 14px;"
                + "-fx-spacing: 10px;"
                + "-fx-alignment: center-left;");
        setPrefHeight(44);

        // 进度条
        progressBar = new ProgressBar(0);
        progressBar.setStyle("-fx-accent: linear-gradient(to right, #533483, #E94560);"
                + "-fx-min-height: 6px;"
                + "-fx-max-height: 6px;");
        progressBar.setPrefWidth(200);

        // 进度文字
        progressLabel = new Label("就绪");
        progressLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + TEXT_COLOR + ";"
                + "-fx-min-width: 120px;");

        // 弹性空间
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 日志按钮
        logBtn = new Label("日志 ▾");
        String logBtnStyle = "-fx-font-size: 11px; -fx-text-fill: " + TEXT_COLOR + ";"
                + "-fx-padding: 2px 8px;"
                + "-fx-background-color: " + BTN_BG + ";"
                + "-fx-background-radius: 4px;"
                + "-fx-cursor: hand;";
        logBtn.setStyle(logBtnStyle);

        getChildren().addAll(progressBar, progressLabel, spacer, logBtn);
    }

    /**
     * 更新进度。
     *
     * @param progress 进度值 (0.0 ~ 1.0)
     * @param text     进度文字
     */
    public void updateProgress(double progress, String text) {
        progressBar.setProgress(progress);
        progressLabel.setText(text);
    }

    /**
     * 设置就绪状态。
     */
    public void setIdle() {
        progressBar.setProgress(0);
        progressLabel.setText("就绪");
    }

    /**
     * 设置日志按钮点击回调。
     *
     * @param callback 点击时执行
     */
    public void setOnLogToggle(Runnable callback) {
        logBtn.setOnMouseClicked(e -> callback.run());
    }
}

package com.manga.translator.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志面板。
 * <p>
 * 从底部栏展开，显示 API 调用日志和翻译进度日志。
 * 支持展开/折叠、日志级别颜色区分。
 */
public class LogPanel extends VBox {

    private static final Logger log = LoggerFactory.getLogger(LogPanel.class);

    private final VBox logContent;
    private final ScrollPane scrollPane;
    private boolean expanded;

    private static final String PANEL_BG = "#252540";
    private static final String BORDER_COLOR = "#404058";
    private static final String INFO_COLOR = "#A0A0B0";
    private static final String SUCCESS_COLOR = "#4ADE80";
    private static final String WARN_COLOR = "#FBBF24";
    private static final String ERROR_COLOR = "#F87171";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public LogPanel() {
        setStyle("-fx-background-color: " + PANEL_BG + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;"
                + "-fx-padding: 8px 12px;");

        logContent = new VBox(4);
        logContent.setStyle("-fx-padding: 4px 0;");

        scrollPane = new ScrollPane(logContent);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(100);

        this.expanded = false;
        setVisible(false);
        setManaged(false);

        getChildren().add(scrollPane);
    }

    /**
     * 添加一条信息日志。
     *
     * @param message 日志消息
     */
    public void info(String message) {
        addLog(INFO_COLOR, "INFO", message);
    }

    /**
     * 添加一条成功日志。
     *
     * @param message 日志消息
     */
    public void success(String message) {
        addLog(SUCCESS_COLOR, "OK", message);
    }

    /**
     * 添加一条警告日志。
     *
     * @param message 日志消息
     */
    public void warn(String message) {
        addLog(WARN_COLOR, "WARN", message);
    }

    /**
     * 添加一条错误日志。
     *
     * @param message 日志消息
     */
    public void error(String message) {
        addLog(ERROR_COLOR, "ERROR", message);
    }

    /**
     * 添加日志行。
     */
    private void addLog(String color, String level, String message) {
        String time = LocalDateTime.now().format(TIME_FORMATTER);
        Label line = new Label("[" + time + "] [" + level + "] " + message);
        line.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";"
                + "-fx-padding: 2px 0;"
                + "-fx-wrap-text: true;");
        logContent.getChildren().add(line);

        // 自动滚动到底部
        javafx.application.Platform.runLater(() ->
                scrollPane.setVvalue(1.0));
    }

    /**
     * 展开日志面板。
     */
    public void expand() {
        expanded = true;
        setVisible(true);
        setManaged(true);
        setPrefHeight(120);
    }

    /**
     * 折叠日志面板。
     */
    public void collapse() {
        expanded = false;
        setVisible(false);
        setManaged(false);
        setPrefHeight(0);
    }

    /**
     * 切换展开/折叠。
     */
    public void toggle() {
        if (expanded) {
            collapse();
        } else {
            expand();
        }
    }

    /**
     * 是否已展开。
     *
     * @return true 如果已展开
     */
    public boolean isExpanded() {
        return expanded;
    }

    /**
     * 清空所有日志。
     */
    public void clear() {
        logContent.getChildren().clear();
    }
}

package com.manga.translator.ui;

import com.manga.translator.model.TextRegion;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * 文本对照面板。
 * <p>
 * 左右双栏显示原文和译文，支持逐条对照。
 * Phase 1 MVP：只读显示，无双击修正功能。
 */
public class TextPanel extends VBox {

    private final GridPane grid;
    private final Label headerLabel;
    private final ScrollPane scrollPane;

    private static final String HEADER_BG = "#2A2A4A";
    private static final String PANEL_BG = "#252540";
    private static final String BORDER_COLOR = "#404058";
    private static final String HEADER_COLOR = "#60A5FA";
    private static final String ORIGIN_COLOR = "#A0A0B0";
    private static final String TRANS_COLOR = "#FBBF24";
    private static final String SEPARATOR_COLOR = "#353548";
    private static final String ROW_HOVER_COLOR = "#3A3A5A";

    public TextPanel() {
        setStyle("-fx-background-color: " + PANEL_BG + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;");
        setPrefHeight(160);

        // 头部
        headerLabel = new Label("文字区域对照");
        headerLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: " + HEADER_COLOR + ";"
                + "-fx-padding: 8px 12px;");

        // 双栏网格
        grid = new GridPane();
        grid.setStyle("-fx-padding: 2px;");
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // 列头部
        Label originHeader = new Label("原文");
        originHeader.setStyle("-fx-font-size: 11px; -fx-text-fill: #606070; -fx-font-weight: 500;"
                + "-fx-padding: 4px 8px; -fx-background-color: " + HEADER_BG + ";");
        Label transHeader = new Label("译文");
        transHeader.setStyle("-fx-font-size: 11px; -fx-text-fill: #606070; -fx-font-weight: 500;"
                + "-fx-padding: 4px 8px; -fx-background-color: " + HEADER_BG + ";");
        grid.add(originHeader, 0, 0);
        grid.add(transHeader, 1, 0);

        // 分隔线
        Label divider = new Label();
        divider.setStyle("-fx-background-color: " + SEPARATOR_COLOR + ";"
                + "-fx-min-width: 1px; -fx-max-width: 1px;");
        grid.add(divider, 0, 0, 1, 100);

        scrollPane = new ScrollPane(grid);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border: none;");
        scrollPane.setFitToWidth(true);

        getChildren().addAll(headerLabel, scrollPane);
    }

    /**
     * 更新文字区域列表。
     *
     * @param regions 文字区域列表
     */
    public void updateTextRegions(List<TextRegion> regions) {
        // 清除旧行（保留头部）
        grid.getChildren().removeIf(node -> {
            Integer row = GridPane.getRowIndex(node);
            return row != null && row > 0;
        });

        if (regions == null || regions.isEmpty()) {
            Label emptyLabel = new Label("无文字区域 — 请先执行翻译");
            emptyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #606070;"
                    + "-fx-padding: 8px;"
                    + "-fx-gridpane-column-span: 2;");
            GridPane.setColumnSpan(emptyLabel, 2);
            grid.add(emptyLabel, 0, 1);
            return;
        }

        for (int i = 0; i < regions.size(); i++) {
            TextRegion region = regions.get(i);
            int row = i + 1;

            // 原文列
            String originalText = region.getOriginalText() != null ? region.getOriginalText() : "";
            Label originLabel = new Label("#" + (i + 1) + " " + originalText);
            originLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + ORIGIN_COLOR + ";"
                    + "-fx-padding: 4px 8px;"
                    + "-fx-border-color: transparent transparent " + SEPARATOR_COLOR + " transparent;"
                    + "-fx-border-width: 0 0 0.5 0;");
            originLabel.setWrapText(true);
            originLabel.setMaxWidth(200);
            int finalI = i;
            originLabel.setOnMouseEntered(e ->
                    originLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + ORIGIN_COLOR + ";"
                            + "-fx-padding: 4px 8px;"
                            + "-fx-background-color: " + ROW_HOVER_COLOR + ";"
                            + "-fx-border-color: transparent transparent " + SEPARATOR_COLOR + " transparent;"
                            + "-fx-border-width: 0 0 0.5 0;"));
            originLabel.setOnMouseExited(e ->
                    originLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + ORIGIN_COLOR + ";"
                            + "-fx-padding: 4px 8px;"
                            + "-fx-border-color: transparent transparent " + SEPARATOR_COLOR + " transparent;"
                            + "-fx-border-width: 0 0 0.5 0;"));

            // 译文列
            String displayText = region.getDisplayText() != null ? region.getDisplayText() : "";
            Label transLabel = new Label(displayText);
            transLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TRANS_COLOR + ";"
                    + "-fx-padding: 4px 8px;"
                    + "-fx-border-color: transparent transparent " + SEPARATOR_COLOR + " transparent;"
                    + "-fx-border-width: 0 0 0.5 0;"
                    + "-fx-cursor: hand;");
            transLabel.setWrapText(true);
            transLabel.setMaxWidth(200);
            transLabel.setOnMouseEntered(e ->
                    transLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TRANS_COLOR + ";"
                            + "-fx-padding: 4px 8px;"
                            + "-fx-background-color: " + ROW_HOVER_COLOR + ";"
                            + "-fx-border-color: transparent transparent " + SEPARATOR_COLOR + " transparent;"
                            + "-fx-border-width: 0 0 0.5 0;"
                            + "-fx-underline: true;"
                            + "-fx-cursor: hand;"));
            transLabel.setOnMouseExited(e ->
                    transLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TRANS_COLOR + ";"
                            + "-fx-padding: 4px 8px;"
                            + "-fx-border-color: transparent transparent " + SEPARATOR_COLOR + " transparent;"
                            + "-fx-border-width: 0 0 0.5 0;"
                            + "-fx-cursor: hand;"));

            grid.add(originLabel, 0, row);
            grid.add(transLabel, 1, row);

            // 行间分隔线
            Label rowDivider = new Label();
            rowDivider.setStyle("-fx-background-color: " + SEPARATOR_COLOR + ";"
                    + "-fx-min-width: 1px; -fx-max-width: 1px;");
            grid.add(rowDivider, 0, row, 1, 1);
        }
    }

    /**
     * 清空文本面板。
     */
    public void clearText() {
        grid.getChildren().removeIf(node -> {
            Integer row = GridPane.getRowIndex(node);
            return row != null && row > 0;
        });
    }
}

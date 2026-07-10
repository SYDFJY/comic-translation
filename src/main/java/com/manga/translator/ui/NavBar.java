package com.manga.translator.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * 导航栏组件。
 * <p>
 * 包含品牌标识、翻译/打开/批量导入/导出按钮、拆分视图/设置按钮、API 状态指示器。
 */
public class NavBar extends HBox {

    private final Button translateBtn;
    private final Button openBtn;
    private final Button batchImportBtn;
    private final Button exportBtn;
    private final Button splitViewBtn;
    private final Button settingsBtn;
    private final Label statusIndicator;

    private static final String BRAND_COLOR = "#E94560";
    private static final String BG_COLOR = "#2A2A4A";
    private static final String TEXT_COLOR = "#A0A0B0";
    private static final String BTN_HOVER_COLOR = "#60A5FA";

    public NavBar() {
        setStyle("-fx-background-color: " + BG_COLOR + ";"
                + "-fx-padding: 8px 16px;"
                + "-fx-spacing: 8px;"
                + "-fx-alignment: center-left;");
        setPrefHeight(52);

        // 品牌标识
        Label brand = new Label("MangaTranslator");
        brand.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + BRAND_COLOR + ";"
                + "-fx-min-width: 140px;");

        // 按钮
        translateBtn = createPrimaryButton("▶ 翻译");
        openBtn = createButton("📂 打开");
        batchImportBtn = createButton("📁 批量导入");
        exportBtn = createButton("💾 导出");

        // 弹性空间
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        splitViewBtn = createButton("⟷ 拆分视图");
        settingsBtn = createButton("⚙ 设置");

        // API 状态指示器
        statusIndicator = new Label("● API 已连接");
        statusIndicator.setStyle("-fx-font-size: 11px; -fx-text-fill: #4ADE80;"
                + "-fx-padding: 0 4px;");

        getChildren().addAll(brand, translateBtn, openBtn, batchImportBtn, exportBtn,
                spacer, splitViewBtn, settingsBtn, statusIndicator);
    }

    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #252540;"
                + "-fx-text-fill: " + TEXT_COLOR + ";"
                + "-fx-border-color: #404058;"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
                + "-fx-padding: 5px 14px;"
                + "-fx-font-size: 12px;"
                + "-fx-cursor: hand;");
        btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: #252540;"
                        + "-fx-text-fill: #E8E8E8;"
                        + "-fx-border-color: " + BTN_HOVER_COLOR + ";"
                        + "-fx-border-radius: 6px;"
                        + "-fx-background-radius: 6px;"
                        + "-fx-padding: 5px 14px;"
                        + "-fx-font-size: 12px;"
                        + "-fx-cursor: hand;"));
        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: #252540;"
                        + "-fx-text-fill: " + TEXT_COLOR + ";"
                        + "-fx-border-color: #404058;"
                        + "-fx-border-radius: 6px;"
                        + "-fx-background-radius: 6px;"
                        + "-fx-padding: 5px 14px;"
                        + "-fx-font-size: 12px;"
                        + "-fx-cursor: hand;"));
        return btn;
    }

    private Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + BRAND_COLOR + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: 500;"
                + "-fx-background-radius: 6px;"
                + "-fx-padding: 7px 20px;"
                + "-fx-font-size: 12px;"
                + "-fx-cursor: hand;"
                + "-fx-border: none;");
        btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: #FF6B81;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: 500;"
                        + "-fx-background-radius: 6px;"
                        + "-fx-padding: 7px 20px;"
                        + "-fx-font-size: 12px;"
                        + "-fx-cursor: hand;"
                        + "-fx-border: none;"));
        btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: " + BRAND_COLOR + ";"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: 500;"
                        + "-fx-background-radius: 6px;"
                        + "-fx-padding: 7px 20px;"
                        + "-fx-font-size: 12px;"
                        + "-fx-cursor: hand;"
                        + "-fx-border: none;"));
        return btn;
    }

    // === 公开方法 ===

    public Button getTranslateBtn() { return translateBtn; }
    public Button getOpenBtn() { return openBtn; }
    public Button getBatchImportBtn() { return batchImportBtn; }
    public Button getExportBtn() { return exportBtn; }
    public Button getSplitViewBtn() { return splitViewBtn; }
    public Button getSettingsBtn() { return settingsBtn; }

    public void setApiStatus(boolean connected) {
        if (connected) {
            statusIndicator.setText("● API 已连接");
            statusIndicator.setStyle("-fx-font-size: 11px; -fx-text-fill: #4ADE80; -fx-padding: 0 4px;");
        } else {
            statusIndicator.setText("● API 断连");
            statusIndicator.setStyle("-fx-font-size: 11px; -fx-text-fill: #F87171; -fx-padding: 0 4px;");
        }
    }

    public void setTranslating(boolean isTranslating) {
        if (isTranslating) {
            translateBtn.setText("✕ 取消");
            translateBtn.setStyle("-fx-background-color: #F87171;"
                    + "-fx-text-fill: white;"
                    + "-fx-font-weight: 500;"
                    + "-fx-background-radius: 6px;"
                    + "-fx-padding: 7px 20px;"
                    + "-fx-font-size: 12px;"
                    + "-fx-cursor: hand;"
                    + "-fx-border: none;");
        } else {
            translateBtn.setText("▶ 翻译");
            translateBtn.setStyle("-fx-background-color: " + BRAND_COLOR + ";"
                    + "-fx-text-fill: white;"
                    + "-fx-font-weight: 500;"
                    + "-fx-background-radius: 6px;"
                    + "-fx-padding: 7px 20px;"
                    + "-fx-font-size: 12px;"
                    + "-fx-cursor: hand;"
                    + "-fx-border: none;");
        }
    }
}

package com.manga.translator.ui;

import com.manga.translator.model.TextRegion;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * 翻译修正对话框。
 * <p>
 * 双击文本面板中的译文行时弹出，允许用户手动修正翻译结果。
 * 修正后自动重新渲染对应文字区域。
 */
public class CorrectionDialog extends Dialog<CorrectionDialog.CorrectionResult> {

    private final TextRegion region;
    private final TextArea editArea;

    /** 原始机器翻译文本（用于比对） */
    private final String originalTranslation;
    /** 修正后的文本 */
    private String correctedText;

    public static class CorrectionResult {
        private final String correctedText;
        private final boolean retranslate;

        public CorrectionResult(String correctedText, boolean retranslate) {
            this.correctedText = correctedText;
            this.retranslate = retranslate;
        }

        public String getCorrectedText() { return correctedText; }
        public boolean isRetranslate() { return retranslate; }
    }

    private static final String DIALOG_BG = "#2A2A4A";
    private static final String INPUT_BG = "#1E1E2E";
    private static final String BORDER_COLOR = "#404058";
    private static final String LABEL_COLOR = "#606070";
    private static final String AMBER_COLOR = "#FBBF24";

    public CorrectionDialog(TextRegion region) {
        this.region = region;
        this.originalTranslation = region.getTranslatedText() != null ? region.getTranslatedText() : "";
        this.correctedText = region.getCorrectedText() != null ? region.getCorrectedText() : originalTranslation;

        setTitle("修正翻译 — 文字区域 #" + region.getId());
        setHeaderText(null);

        getDialogPane().setStyle("-fx-background-color: " + DIALOG_BG + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 12px;"
                + "-fx-background-radius: 12px;"
                + "-fx-padding: 0;");

        VBox content = new VBox(14);
        content.setPrefWidth(440);
        content.setPadding(new Insets(18));

        // 头部
        Label header = new Label("修正翻译 — 文字区域 #" + region.getId());
        header.setStyle("-fx-font-size: 15px; -fx-text-fill: #60A5FA; -fx-font-weight: 500;");

        // 原文区
        VBox originalSection = createReadOnlySection("原文", region.getOriginalText(), "#E8E8E8");

        // 机器翻译区
        VBox machineSection = createReadOnlySection("机器翻译", originalTranslation, LABEL_COLOR);

        // 修正编辑区
        Label editLabel = new Label("修正译文（可编辑）");
        editLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + AMBER_COLOR + "; -fx-font-weight: 500;");

        editArea = new TextArea(correctedText);
        editArea.setStyle("-fx-background-color: " + INPUT_BG + ";"
                + "-fx-text-fill: " + AMBER_COLOR + ";"
                + "-fx-border-color: " + AMBER_COLOR + ";"
                + "-fx-border-width: 2px;"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
                + "-fx-padding: 10px 12px;"
                + "-fx-font-size: 13px;"
                + "-fx-wrap-text: true;");
        editArea.setPrefHeight(60);
        editArea.setPrefWidth(400);

        content.getChildren().addAll(header, originalSection, machineSection, editLabel, editArea);

        getDialogPane().setContent(content);

        // 按钮
        ButtonType confirmBtnType = new ButtonType("确认修正", ButtonBar.ButtonData.OK_DONE);
        ButtonType retranslateBtnType = new ButtonType("重新翻译");
        ButtonType cancelBtnType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(confirmBtnType, retranslateBtnType, cancelBtnType);

        // 确认修正 → 不调用翻译API，直接更新译文
        setResultConverter(btnType -> {
            if (btnType == confirmBtnType) {
                return new CorrectionResult(editArea.getText(), false);
            } else if (btnType == retranslateBtnType) {
                return new CorrectionResult(editArea.getText(), true);
            }
            return null;
        });
    }

    /**
     * 创建只读展示区域。
     */
    private VBox createReadOnlySection(String label, String text, String textColor) {
        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + LABEL_COLOR + "; -fx-font-weight: 500;");

        Label textLabel = new Label(text != null ? text : "");
        textLabel.setStyle("-fx-background-color: " + INPUT_BG + ";"
                + "-fx-text-fill: " + textColor + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
                + "-fx-padding: 10px 12px;"
                + "-fx-font-size: 13px;"
                + "-fx-wrap-text: true;"
                + "-fx-max-width: 400px;");

        VBox section = new VBox(4, titleLabel, textLabel);
        return section;
    }
}

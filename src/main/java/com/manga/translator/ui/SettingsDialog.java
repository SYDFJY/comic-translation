package com.manga.translator.ui;

import com.manga.translator.AuthException;
import com.manga.translator.client.BaiduAuthManager;
import com.manga.translator.model.InpaintStrategy;
import com.manga.translator.model.OcrVersion;
import com.manga.translator.model.TranslationConfig;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 设置对话框。
 * <p>
 * 允许用户配置百度 API Key、翻译选项、渲染与导出参数。
 */
public class SettingsDialog extends Dialog<TranslationConfig> {

    private static final Logger log = LoggerFactory.getLogger(SettingsDialog.class);

    private final TranslationConfig config;
    private final BaiduAuthManager authManager;

    private final PasswordField ocrApiKeyField;
    private final PasswordField ocrSecretKeyField;
    private final TextField translateAppIdField;
    private final PasswordField translateSecretKeyField;
    private final ComboBox<String> ocrVersionCombo;
    private final ComboBox<String> sourceLangCombo;
    private final ComboBox<String> targetLangCombo;
    private final TextField expandMarginField;
    private final ComboBox<String> inpaintStrategyCombo;
    private final ComboBox<String> fontNameCombo;
    private final ComboBox<String> exportFormatCombo;

    private static final String DIALOG_BG = "#2A2A4A";
    private static final String INPUT_BG = "#1E1E2E";
    private static final String LABEL_COLOR = "#606070";
    private static final String FIELD_COLOR = "#60A5FA";
    private static final String BORDER_COLOR = "#404058";

    public SettingsDialog(TranslationConfig config, BaiduAuthManager authManager) {
        this.config = config;
        this.authManager = authManager;

        setTitle("设置");
        setHeaderText(null);

        // 对话框样式
        getDialogPane().setStyle("-fx-background-color: " + DIALOG_BG + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 12px;"
                + "-fx-background-radius: 12px;"
                + "-fx-padding: 18px;");

        // 内容
        VBox content = new VBox(16);
        content.setPrefWidth(520);

        // === API 配置 ===
        Label apiSection = new Label("百度 API 配置");
        apiSection.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #E94560;"
                + "-fx-border-color: transparent transparent rgba(233,69,96,0.3) transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 6 0;");

        GridPane apiGrid = new GridPane();
        apiGrid.setHgap(12);
        apiGrid.setVgap(6);
        apiGrid.setStyle("-fx-padding: 6px 0;");

        ocrApiKeyField = new PasswordField();
        ocrApiKeyField.setText(config.getOcrApiKey());
        styleField(ocrApiKeyField);

        ocrSecretKeyField = new PasswordField();
        ocrSecretKeyField.setText(config.getOcrSecretKey());
        styleField(ocrSecretKeyField);

        translateAppIdField = new TextField();
        translateAppIdField.setText(config.getTranslateAppId());
        styleField(translateAppIdField);

        translateSecretKeyField = new PasswordField();
        translateSecretKeyField.setText(config.getTranslateSecretKey());
        styleField(translateSecretKeyField);

        apiGrid.add(createLabel("OCR API Key:"), 0, 0);
        apiGrid.add(ocrApiKeyField, 1, 0);
        apiGrid.add(createLabel("OCR Secret Key:"), 0, 1);
        apiGrid.add(ocrSecretKeyField, 1, 1);
        apiGrid.add(createLabel("翻译 App ID:"), 0, 2);
        apiGrid.add(translateAppIdField, 1, 2);
        apiGrid.add(createLabel("翻译密钥:"), 0, 3);
        apiGrid.add(translateSecretKeyField, 1, 3);

        // === 翻译选项 ===
        Label transSection = new Label("翻译选项");
        transSection.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #60A5FA;"
                + "-fx-border-color: transparent transparent rgba(96,165,250,0.3) transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 6 0;");

        GridPane transGrid = new GridPane();
        transGrid.setHgap(12);
        transGrid.setVgap(6);
        transGrid.setStyle("-fx-padding: 6px 0;");

        ocrVersionCombo = new ComboBox<>();
        ocrVersionCombo.getItems().addAll("通用文字识别", "高精度版");
        ocrVersionCombo.setValue(config.getOcrVersion() == OcrVersion.ACCURATE_BASIC ? "高精度版" : "通用文字识别");
        styleCombo(ocrVersionCombo);

        sourceLangCombo = new ComboBox<>();
        sourceLangCombo.getItems().addAll("自动检测", "日语", "英语", "韩语", "中文");
        sourceLangCombo.setValue(mapLangToDisplay(config.getSourceLanguage()));
        styleCombo(sourceLangCombo);

        targetLangCombo = new ComboBox<>();
        targetLangCombo.getItems().addAll("中文", "英语", "日语", "韩语");
        targetLangCombo.setValue(mapLangToDisplay(config.getTargetLanguage()));
        styleCombo(targetLangCombo);

        transGrid.add(createLabel("OCR 版本:"), 0, 0);
        transGrid.add(ocrVersionCombo, 1, 0);
        transGrid.add(createLabel("源语言:"), 0, 1);
        transGrid.add(sourceLangCombo, 1, 1);
        transGrid.add(createLabel("目标语言:"), 0, 2);
        transGrid.add(targetLangCombo, 1, 2);

        // === 渲染与导出 ===
        Label renderSection = new Label("渲染与导出");
        renderSection.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #2DD4BF;"
                + "-fx-border-color: transparent transparent rgba(45,212,191,0.3) transparent;"
                + "-fx-border-width: 0 0 1 0; -fx-padding: 0 0 6 0;");

        GridPane renderGrid = new GridPane();
        renderGrid.setHgap(12);
        renderGrid.setVgap(6);
        renderGrid.setStyle("-fx-padding: 6px 0;");

        expandMarginField = new TextField(String.valueOf(config.getExpandMargin()));
        styleField(expandMarginField);

        inpaintStrategyCombo = new ComboBox<>();
        inpaintStrategyCombo.getItems().addAll("白色覆盖", "取色填充");
        inpaintStrategyCombo.setValue(config.getInpaintStrategy() == InpaintStrategy.AVG_COLOR_FILL ? "取色填充" : "白色覆盖");
        styleCombo(inpaintStrategyCombo);

        fontNameCombo = new ComboBox<>();
        fontNameCombo.getItems().addAll("Microsoft YaHei", "SimSun", "KaiTi");
        fontNameCombo.setValue(config.getFontName());
        styleCombo(fontNameCombo);

        exportFormatCombo = new ComboBox<>();
        exportFormatCombo.getItems().addAll("PNG", "JPG");
        exportFormatCombo.setValue(config.getExportFormat().toUpperCase());
        styleCombo(exportFormatCombo);

        renderGrid.add(createLabel("气泡扩展量(px):"), 0, 0);
        renderGrid.add(expandMarginField, 1, 0);
        renderGrid.add(createLabel("修补策略:"), 0, 1);
        renderGrid.add(inpaintStrategyCombo, 1, 1);
        renderGrid.add(createLabel("回填字体:"), 0, 2);
        renderGrid.add(fontNameCombo, 1, 2);
        renderGrid.add(createLabel("导出格式:"), 0, 3);
        renderGrid.add(exportFormatCombo, 1, 3);

        // 验证按钮
        Button verifyBtn = new Button("验证连通性");
        verifyBtn.setStyle("-fx-background-color: #252540; -fx-text-fill: #A0A0B0;"
                + "-fx-border-color: #404058; -fx-border-radius: 6px; -fx-background-radius: 6px;"
                + "-fx-padding: 5px 14px; -fx-font-size: 12px; -fx-cursor: hand;");
        verifyBtn.setOnAction(e -> verifyConnection());

        content.getChildren().addAll(
                apiSection, apiGrid,
                transSection, transGrid,
                renderSection, renderGrid,
                verifyBtn
        );

        getDialogPane().setContent(content);

        // 按钮
        ButtonType saveBtnType = new ButtonType("保存设置", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtnType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveBtnType, cancelBtnType);

        setResultConverter(btnType -> {
            if (btnType == saveBtnType) {
                return collectConfig();
            }
            return null;
        });
    }

    private void verifyConnection() {
        TranslationConfig testConfig = collectConfig();
        try {
            boolean success = authManager.verifyConnection(testConfig);
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "API 连通性验证成功！", ButtonType.OK);
                alert.setTitle("验证成功");
                alert.setHeaderText(null);
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "API 连通性验证失败，请检查 API Key", ButtonType.OK);
                alert.setTitle("验证失败");
                alert.setHeaderText(null);
                alert.show();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "验证出错: " + e.getMessage(), ButtonType.OK);
            alert.setTitle("验证失败");
            alert.setHeaderText(null);
            alert.show();
        }
    }

    private TranslationConfig collectConfig() {
        TranslationConfig c = new TranslationConfig();
        c.setOcrApiKey(ocrApiKeyField.getText());
        c.setOcrSecretKey(ocrSecretKeyField.getText());
        c.setTranslateAppId(translateAppIdField.getText());
        c.setTranslateSecretKey(translateSecretKeyField.getText());
        c.setOcrVersion("高精度版".equals(ocrVersionCombo.getValue()) ? OcrVersion.ACCURATE_BASIC : OcrVersion.GENERAL_BASIC);
        c.setSourceLanguage(mapDisplayToLang(sourceLangCombo.getValue()));
        c.setTargetLanguage(mapDisplayToLang(targetLangCombo.getValue()));
        try {
            c.setExpandMargin(Integer.parseInt(expandMarginField.getText()));
        } catch (NumberFormatException e) {
            c.setExpandMargin(12);
        }
        c.setInpaintStrategy("取色填充".equals(inpaintStrategyCombo.getValue()) ? InpaintStrategy.AVG_COLOR_FILL : InpaintStrategy.WHITE_FILL);
        c.setFontName(fontNameCombo.getValue());
        c.setExportFormat(exportFormatCombo.getValue().toLowerCase());
        return c;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + LABEL_COLOR + "; -fx-padding: 4px 0;");
        return label;
    }

    private void styleField(TextField field) {
        field.setStyle("-fx-background-color: " + INPUT_BG + ";"
                + "-fx-text-fill: #E8E8E8;"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
                + "-fx-padding: 6px 10px;"
                + "-fx-font-size: 12px;"
                + "-fx-pref-height: 30px;");
    }

    private void styleCombo(ComboBox<?> combo) {
        combo.setStyle("-fx-background-color: " + INPUT_BG + ";"
                + "-fx-text-fill: " + FIELD_COLOR + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 6px;"
                + "-fx-background-radius: 6px;"
                + "-fx-padding: 2px 10px;"
                + "-fx-font-size: 12px;"
                + "-fx-pref-height: 30px;");
    }

    private String mapLangToDisplay(String lang) {
        if (lang == null) return "自动检测";
        return switch (lang) {
            case "jp" -> "日语";
            case "en" -> "英语";
            case "kor" -> "韩语";
            case "zh" -> "中文";
            default -> "自动检测";
        };
    }

    private String mapDisplayToLang(String display) {
        if (display == null) return "auto";
        return switch (display) {
            case "日语" -> "jp";
            case "英语" -> "en";
            case "韩语" -> "kor";
            case "中文" -> "zh";
            default -> "auto";
        };
    }
}

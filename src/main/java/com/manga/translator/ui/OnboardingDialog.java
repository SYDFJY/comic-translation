package com.manga.translator.ui;

import com.manga.translator.client.BaiduAuthManager;
import com.manga.translator.model.TranslationConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * 首次引导对话框。
 * <p>
 * 首次启动时弹出，引导用户配置百度 API Key。
 * 3 步引导：欢迎介绍 → 填写 API Key → 验证连通性。
 */
public class OnboardingDialog extends Dialog<TranslationConfig> {

    private final BaiduAuthManager authManager;
    private int currentStep = 0;

    /** 同意复选框 */
    private CheckBox agreeCheck;

    private final VBox contentArea;
    private final HBox stepIndicator;

    private final PasswordField ocrApiKeyField;
    private final PasswordField ocrSecretKeyField;
    private final TextField translateAppIdField;
    private final PasswordField translateSecretKeyField;
    private final Label feedbackLabel;

    private TranslationConfig config;

    private static final String DIALOG_BG = "#2A2A4A";
    private static final String INPUT_BG = "#1E1E2E";
    private static final String BORDER_COLOR = "#404058";
    private static final String DONE_COLOR = "#4ADE80";
    private static final String ACTIVE_COLOR = "#60A5FA";
    private static final String INACTIVE_COLOR = "#404058";

    public OnboardingDialog(BaiduAuthManager authManager) {
        this.authManager = authManager;
        this.config = new TranslationConfig();

        setTitle("欢迎使用漫画翻译器");
        setHeaderText(null);

        getDialogPane().setStyle("-fx-background-color: " + DIALOG_BG + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 12px;"
                + "-fx-background-radius: 12px;");

        // 步骤指示器
        stepIndicator = new HBox(16);
        stepIndicator.setAlignment(Pos.CENTER);
        stepIndicator.setPadding(new Insets(16, 0, 12, 0));

        // 内容区
        contentArea = new VBox(16);
        contentArea.setPrefWidth(480);
        contentArea.setPadding(new Insets(0, 24, 24, 24));

        // 输入字段
        ocrApiKeyField = new PasswordField();
        styleField(ocrApiKeyField);
        ocrSecretKeyField = new PasswordField();
        styleField(ocrSecretKeyField);
        translateAppIdField = new TextField();
        styleField(translateAppIdField);
        translateSecretKeyField = new PasswordField();
        styleField(translateSecretKeyField);

        feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-font-size: 12px; -fx-padding: 8px 0;");

        VBox root = new VBox(0, stepIndicator, contentArea);
        getDialogPane().setContent(root);

        // 按钮
        ButtonType nextBtnType = new ButtonType("下一步", ButtonBar.ButtonData.NEXT_FORWARD);
        ButtonType finishBtnType = new ButtonType("完成", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtnType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(nextBtnType, finishBtnType, cancelBtnType);

        // 默认隐藏"完成"按钮
        Button finishBtn = (Button) getDialogPane().lookupButton(finishBtnType);
        finishBtn.setVisible(false);

        Button nextBtn = (Button) getDialogPane().lookupButton(nextBtnType);

        // 下一步逻辑
        nextBtn.setOnAction(e -> {
            if (currentStep == 0) {
                // 验证同意复选框
                if (agreeCheck == null || !agreeCheck.isSelected()) {
                    feedbackLabel.setText("请先勾选「我已了解」以继续");
                    feedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FBBF24;");
                    if (!contentArea.getChildren().contains(feedbackLabel)) {
                        contentArea.getChildren().add(feedbackLabel);
                    }
                    return;
                }
                contentArea.getChildren().remove(feedbackLabel);
                showStep1ApiConfig();
                updateStepIndicator(1);
                currentStep = 1;
            } else if (currentStep == 1) {
                showStep2Verify();
                updateStepIndicator(2);
                currentStep = 2;
                nextBtn.setVisible(false);
                finishBtn.setVisible(true);
            }
        });

        // 完成逻辑
        setResultConverter(btnType -> {
            if (btnType == finishBtnType) {
                // 保存输入的配置
                config.setOcrApiKey(ocrApiKeyField.getText());
                config.setOcrSecretKey(ocrSecretKeyField.getText());
                config.setTranslateAppId(translateAppIdField.getText());
                config.setTranslateSecretKey(translateSecretKeyField.getText());
                return config;
            }
            return null;
        });

        // 显示第一步
        showStep0Welcome();
        updateStepIndicator(0);
    }

    /**
     * Step 0: 欢迎页。
     */
    private void showStep0Welcome() {
        contentArea.getChildren().clear();

        Label title = new Label("欢迎使用漫画翻译器");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #E94560;");

        Label desc = new Label("MangaTranslator 是一款基于 JavaFX 的桌面漫画翻译工具。\n\n"
                + "本软件将调用百度云 API 处理您的图片，包括：\n"
                + "• 百度 OCR API — 识别图片中的文字\n"
                + "• 百度翻译 API — 自动翻译识别到的文字\n\n"
                + "使用前请确认您已阅读并同意百度云服务条款。");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #A0A0B0; -fx-line-spacing: 4px;");
        desc.setWrapText(true);

        agreeCheck = new CheckBox("我已了解，本软件将调用百度云 API 处理图片");
        agreeCheck.setStyle("-fx-font-size: 12px; -fx-text-fill: #E8E8E8;");

        contentArea.getChildren().addAll(title, desc, agreeCheck);
    }

    /**
     * Step 1: 填写 API Key。
     */
    private void showStep1ApiConfig() {
        contentArea.getChildren().clear();

        Label title = new Label("配置百度 API");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #60A5FA;");

        Label desc = new Label("请填写您的百度云 API 密钥。\n"
                + "如果没有账号，请先访问 https://console.bce.baidu.com/ 注册。");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #A0A0B0;");
        desc.setWrapText(true);

        VBox form = new VBox(6);
        form.setStyle("-fx-padding: 8px 0;");

        form.getChildren().addAll(
                createFieldLabel("OCR API Key"),
                ocrApiKeyField,
                createFieldLabel("OCR Secret Key"),
                ocrSecretKeyField,
                createFieldLabel("翻译 App ID（可选）"),
                translateAppIdField,
                createFieldLabel("翻译密钥（可选）"),
                translateSecretKeyField
        );

        contentArea.getChildren().addAll(title, desc, form);
    }

    /**
     * Step 2: 验证连通性。
     */
    private void showStep2Verify() {
        contentArea.getChildren().clear();

        config.setOcrApiKey(ocrApiKeyField.getText());
        config.setOcrSecretKey(ocrSecretKeyField.getText());
        config.setTranslateAppId(translateAppIdField.getText());
        config.setTranslateSecretKey(translateSecretKeyField.getText());

        Label title = new Label("验证连通性");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #60A5FA;");

        Label status = new Label("正在验证 API 连通性…");
        status.setStyle("-fx-font-size: 13px; -fx-text-fill: #A0A0B0;");

        contentArea.getChildren().addAll(title, status);

        // 在后台线程验证
        new Thread(() -> {
            try {
                boolean success = authManager.verifyConnection(config);
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        status.setText("✅ 验证成功！API 连接正常。");
                        status.setStyle("-fx-font-size: 13px; -fx-text-fill: " + DONE_COLOR + ";");
                        feedbackLabel.setText("点击「完成」开始使用漫画翻译器");
                        feedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + DONE_COLOR + ";");
                    } else {
                        status.setText("❌ 验证失败，请检查 API Key 是否正确");
                        status.setStyle("-fx-font-size: 13px; -fx-text-fill: #F87171;");
                        feedbackLabel.setText("请返回上一步检查 API Key 配置");
                        feedbackLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F87171;");
                    }
                    contentArea.getChildren().add(feedbackLabel);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    status.setText("❌ 验证出错: " + e.getMessage());
                    status.setStyle("-fx-font-size: 13px; -fx-text-fill: #F87171;");
                    contentArea.getChildren().add(feedbackLabel);
                });
            }
        }, "onboarding-verify").start();
    }

    /**
     * 更新步骤指示器。
     */
    private void updateStepIndicator(int activeStep) {
        stepIndicator.getChildren().clear();
        String[] labels = {"欢迎", "配置", "验证"};
        for (int i = 0; i < labels.length; i++) {
            Label dot = new Label(String.valueOf(i + 1));
            String color;
            if (i < activeStep) {
                color = DONE_COLOR; // 已完成
                dot.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: white;"
                        + "-fx-background-color: " + DONE_COLOR + ";"
                        + "-fx-min-width: 24px; -fx-min-height: 24px;"
                        + "-fx-max-width: 24px; -fx-max-height: 24px;"
                        + "-fx-background-radius: 12px;"
                        + "-fx-alignment: center;");
            } else if (i == activeStep) {
                color = ACTIVE_COLOR; // 当前
                dot.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: white;"
                        + "-fx-background-color: " + ACTIVE_COLOR + ";"
                        + "-fx-min-width: 24px; -fx-min-height: 24px;"
                        + "-fx-max-width: 24px; -fx-max-height: 24px;"
                        + "-fx-background-radius: 12px;"
                        + "-fx-alignment: center;");
            } else {
                color = INACTIVE_COLOR; // 未完成
                dot.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #606070;"
                        + "-fx-background-color: " + INACTIVE_COLOR + ";"
                        + "-fx-min-width: 24px; -fx-min-height: 24px;"
                        + "-fx-max-width: 24px; -fx-max-height: 24px;"
                        + "-fx-background-radius: 12px;"
                        + "-fx-alignment: center;");
            }

            Label label = new Label(labels[i]);
            label.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");

            VBox stepBox = new VBox(4, dot, label);
            stepBox.setAlignment(Pos.CENTER);
            stepIndicator.getChildren().add(stepBox);
        }
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #606070; -fx-font-weight: 500;");
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
}

package com.manga.translator;

import com.manga.translator.client.BaiduAuthManager;
import com.manga.translator.client.HttpUtil;
import com.manga.translator.config.ConfigManager;
import com.manga.translator.model.TranslationConfig;
import com.manga.translator.ui.MainWindow;
import com.manga.translator.ui.OnboardingDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 漫画翻译器主入口。
 * <p>
 * 基于 JavaFX 的桌面漫画翻译应用。
 * 通过调用百度 OCR + 翻译 API，实现漫画图片中文字的自动识别、翻译、擦除和回填渲染。
 */
public class MangaTranslatorApp extends Application {

    private static final String APP_TITLE = "MangaTranslator — 漫画翻译器";
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;
    private static final int WINDOW_MIN_WIDTH = 960;
    private static final int WINDOW_MIN_HEIGHT = 540;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ConfigManager configManager = new ConfigManager();
        TranslationConfig config = configManager.loadConfig();

        // 首次启动引导
        if (!config.isValid()) {
            HttpUtil httpUtil = new HttpUtil();
            BaiduAuthManager authManager = new BaiduAuthManager(httpUtil);

            OnboardingDialog onboarding = new OnboardingDialog(authManager);
            onboarding.initOwner(primaryStage);
            onboarding.showAndWait().ifPresent(savedConfig -> {
                configManager.saveConfig(savedConfig);
                // config saved by configManager.saveConfig(savedConfig);
            });
        }

        MainWindow mainWindow = new MainWindow();
        Scene scene = new Scene(mainWindow, WINDOW_WIDTH, WINDOW_HEIGHT);

        // 加载 CSS 样式
        var cssUrl = getClass().getResource("/styles/app.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(WINDOW_MIN_WIDTH);
        primaryStage.setMinHeight(WINDOW_MIN_HEIGHT);
        primaryStage.show();

        // 确保窗口关闭时清理资源
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }
}

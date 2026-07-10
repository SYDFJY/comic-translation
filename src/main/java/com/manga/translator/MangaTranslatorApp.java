package com.manga.translator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
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
        // Phase 1 placeholder — 后续替换为完整 MainWindow
        Label placeholder = new Label("MangaTranslator — 漫画翻译器");
        placeholder.setStyle("-fx-font-size: 24px; -fx-text-fill: #E94560;");

        StackPane root = new StackPane(placeholder);
        root.setStyle("-fx-background-color: #1E1E2E;");

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(WINDOW_MIN_WIDTH);
        primaryStage.setMinHeight(WINDOW_MIN_HEIGHT);
        primaryStage.show();
    }
}

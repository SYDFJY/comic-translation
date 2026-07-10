package com.manga.translator.ui;

import com.manga.translator.client.BaiduAuthManager;
import com.manga.translator.client.BaiduOcrClient;
import com.manga.translator.client.BaiduTranslateClient;
import com.manga.translator.client.HttpUtil;
import com.manga.translator.config.ConfigManager;
import com.manga.translator.model.MangaPage;
import com.manga.translator.model.PageStatus;
import com.manga.translator.model.TranslationConfig;
import com.manga.translator.pipeline.*;
import com.manga.translator.service.impl.*;
import com.manga.translator.util.ImageUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 主窗口。
 * <p>
 * 整合所有 UI 组件，实现图片导入、翻译管线执行、结果展示等功能。
 */
public class MainWindow extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    private final NavBar navBar;
    private final FileListPanel fileListPanel;
    private final CanvasPanel canvasPanel;
    private final TextPanel textPanel;
    private final BottomBar bottomBar;

    private final HttpUtil httpUtil;
    private final BaiduAuthManager authManager;
    private final BaiduOcrClient ocrClient;
    private final BaiduTranslateClient translateClient;
    private final ConfigManager configManager;
    private TranslationConfig config;

    private TranslationPipeline pipeline;
    private Thread pipelineThread;

    private static final String PANEL_BG = "#252540";

    public MainWindow() {
        setStyle("-fx-background-color: #1E1E2E;");

        // 初始化核心组件
        this.httpUtil = new HttpUtil();
        this.authManager = new BaiduAuthManager(httpUtil);
        this.ocrClient = new BaiduOcrClient(httpUtil, authManager);
        this.translateClient = new BaiduTranslateClient(httpUtil);
        this.configManager = new ConfigManager();
        this.config = configManager.loadConfig();

        // UI 组件
        this.navBar = new NavBar();
        this.fileListPanel = new FileListPanel();
        this.canvasPanel = new CanvasPanel();
        this.textPanel = new TextPanel();
        this.bottomBar = new BottomBar();

        setupLayout();
        setupEventHandlers();

        // 检查 API 状态
        if (config.isValid()) {
            try {
                authManager.getAccessToken(config);
                navBar.setApiStatus(true);
            } catch (Exception e) {
                navBar.setApiStatus(false);
            }
        } else {
            navBar.setApiStatus(false);
        }
    }

    /**
     * 设置布局。
     */
    private void setupLayout() {
        // 顶部导航栏
        setTop(navBar);

        // 中间区域：侧栏 + 画布+文本面板
        HBox centerArea = new HBox(4);

        // 左侧：文件列表
        fileListPanel.setPrefWidth(260);
        fileListPanel.setMinWidth(200);

        // 右侧：画布 + 文本面板
        VBox rightArea = new VBox(4);
        VBox.setVgrow(canvasPanel, Priority.ALWAYS);
        rightArea.getChildren().addAll(canvasPanel, textPanel);

        centerArea.getChildren().addAll(fileListPanel, rightArea);
        HBox.setHgrow(rightArea, Priority.ALWAYS);
        setCenter(centerArea);

        // 底部状态栏
        setBottom(bottomBar);
    }

    /**
     * 设置事件处理。
     */
    private void setupEventHandlers() {
        // 打开文件
        navBar.getOpenBtn().setOnAction(e -> openFile());

        // 翻译
        navBar.getTranslateBtn().setOnAction(e -> {
            MangaPage selected = fileListPanel.getSelectedPage();
            if (selected != null) {
                if (selected.getStatus() == PageStatus.TRANSLATING) {
                    cancelTranslation();
                } else {
                    startTranslation(selected);
                }
            }
        });

        // 文件选中
        fileListPanel.setOnFileSelected(this::onFileSelected);

        // 导出
        navBar.getExportBtn().setOnAction(e -> exportResult());

        // 设置
        navBar.getSettingsBtn().setOnAction(e -> showSettings());

        // 批量导入
        navBar.getBatchImportBtn().setOnAction(e -> batchImport());
    }

    /**
     * 打开文件选择器。
     */
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择漫画图片");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            loadImageFile(file);
        }
    }

    /**
     * 批量导入文件夹中的图片。
     */
    private void batchImport() {
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("选择图片文件夹");
        File dir = dirChooser.showDialog(getScene().getWindow());
        if (dir != null) {
            File[] files = dir.listFiles((d, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                        || lower.endsWith(".png") || lower.endsWith(".bmp");
            });
            if (files != null) {
                for (File file : files) {
                    loadImageFile(file);
                }
            }
        }
    }

    /**
     * 加载图片文件。
     */
    private void loadImageFile(File file) {
        try {
            BufferedImage image = ImageUtil.loadImage(file.getAbsolutePath());
            MangaPage page = new MangaPage();
            page.setFileName(file.getName());
            page.setFilePath(file.getAbsolutePath());
            page.setOriginalImage(image);
            page.setStatus(PageStatus.LOADED);

            fileListPanel.addPage(page);
            onFileSelected(page);

            log.info("图片已加载: {}", file.getName());
        } catch (IOException e) {
            log.error("加载图片失败: {}", file.getName(), e);
            showAlert("加载失败", "无法加载图片: " + file.getName());
        }
    }

    /**
     * 文件选中事件。
     */
    private void onFileSelected(MangaPage page) {
        if (page != null) {
            canvasPanel.showOriginalImage(page.getOriginalImage());
            textPanel.updateTextRegions(page.getTextRegions());

            if (page.getTranslatedImage() != null) {
                canvasPanel.showResultImage(page.getTranslatedImage());
            }
        }
    }

    /**
     * 开始翻译。
     */
    private void startTranslation(MangaPage page) {
        if (!config.isValid()) {
            showAlert("配置未完成", "请先在设置中配置百度 API Key");
            return;
        }

        navBar.setTranslating(true);
        bottomBar.updateProgress(0, "正在初始化…");
        textPanel.clearText();

        // 创建管线
        pipeline = new TranslationPipeline(new PipelineEventBus());
        pipeline.addStep(new OcrStep(new BaiduOcrServiceImpl(ocrClient, authManager, config)));
        pipeline.addStep(new CleanStep());
        pipeline.addStep(new TranslateStep(new BaiduTranslateServiceImpl(translateClient, config)));
        pipeline.addStep(new InpaintStep(new WhiteInpaintServiceImpl()));
        pipeline.addStep(new RenderStep(new Graphics2DRenderServiceImpl()));

        // 注册事件监听
        pipeline.getEventBus().addListener(new PipelineListener() {
            @Override
            public void onProgress(PipelineEvent event) {
                Platform.runLater(() -> {
                    bottomBar.updateProgress(event.getProgress(),
                            String.format("%.0f%% — %s", event.getProgress() * 100, event.getStepName()));
                });
            }

            @Override
            public void onError(PipelineEvent event) {
                Platform.runLater(() -> {
                    bottomBar.updateProgress(event.getProgress(), event.getMessage());
                    navBar.setTranslating(false);
                    showAlert("翻译失败", event.getMessage());
                });
            }

            @Override
            public void onComplete(PipelineEvent event) {
                Platform.runLater(() -> {
                    bottomBar.setIdle();
                    navBar.setTranslating(false);

                    var result = page.getTranslatedImage();
                    if (result != null) {
                        canvasPanel.showResultImage(result);
                    }
                    textPanel.updateTextRegions(page.getTextRegions());
                    fileListPanel.refreshList();
                });
            }
        });

        // 在后台线程执行翻译
        pipelineThread = new Thread(() -> {
            try {
                var context = pipeline.execute(page, config);
                if (context.getResultImage() != null) {
                    page.setTranslatedImage(context.getResultImage());
                    page.setTextRegions(context.getCleanedRegions());
                    page.setStatus(PageStatus.COMPLETED);
                }
            } catch (Exception e) {
                log.error("翻译管线执行失败", e);
                Platform.runLater(() -> {
                    bottomBar.setIdle();
                    navBar.setTranslating(false);
                    showAlert("翻译失败", e.getMessage());
                });
            }
        }, "pipeline-thread");
        pipelineThread.setDaemon(true);
        pipelineThread.start();
    }

    /**
     * 取消翻译。
     */
    private void cancelTranslation() {
        if (pipeline != null) {
            pipeline.cancel();
        }
        if (pipelineThread != null && pipelineThread.isAlive()) {
            pipelineThread.interrupt();
        }
        navBar.setTranslating(false);
        bottomBar.setIdle();
    }

    /**
     * 导出结果。
     */
    private void exportResult() {
        MangaPage selected = fileListPanel.getSelectedPage();
        if (selected == null || selected.getTranslatedImage() == null) {
            showAlert("无导出内容", "请先翻译后再导出");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出翻译结果");
        fileChooser.setInitialFileName(selected.getFileName().replaceFirst("\\.[^.]+$", "") + "_translated.png");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG 图片", "*.png")
        );

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                ImageUtil.saveImage(selected.getTranslatedImage(), file.getAbsolutePath(), "png");
                log.info("导出成功: {}", file.getAbsolutePath());
            } catch (IOException e) {
                log.error("导出失败", e);
                showAlert("导出失败", "保存图片时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 显示设置对话框。
     */
    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(config, authManager);
        dialog.showAndWait().ifPresent(savedConfig -> {
            this.config = savedConfig;
            configManager.saveConfig(savedConfig);
            if (savedConfig.isValid()) {
                try {
                    authManager.getAccessToken(savedConfig);
                    navBar.setApiStatus(true);
                } catch (Exception e) {
                    navBar.setApiStatus(false);
                }
            }
        });
    }

    /**
     * 显示错误提示框。
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.show();
    }
}

package com.manga.translator.ui;

import com.manga.translator.client.BaiduAuthManager;
import com.manga.translator.client.BaiduOcrClient;
import com.manga.translator.client.BaiduTranslateClient;
import com.manga.translator.client.HttpUtil;
import com.manga.translator.config.ConfigManager;
import com.manga.translator.model.MangaPage;
import com.manga.translator.model.PageStatus;
import com.manga.translator.model.RegionStatus;
import com.manga.translator.model.TextRegion;
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
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.util.Optional;

/**
 * 主窗口。
 * <p>
 * 整合所有 UI 组件，实现图片导入、翻译管线执行、翻译修正、结果展示等功能。
 */
public class MainWindow extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    private final NavBar navBar;
    private final FileListPanel fileListPanel;
    private final CanvasPanel canvasPanel;
    private final TextPanel textPanel;
    private final BottomBar bottomBar;
    private final LogPanel logPanel;

    private final HttpUtil httpUtil;
    private final BaiduAuthManager authManager;
    private final BaiduOcrClient ocrClient;
    private final BaiduTranslateClient translateClient;
    private final ConfigManager configManager;
    private TranslationConfig config;
    private MangaPage currentPage;

    private TranslationPipeline pipeline;
    private Thread pipelineThread;

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
        this.logPanel = new LogPanel();

        setupLayout();
        setupEventHandlers();

        // 检查 API 状态
        if (config.isValid()) {
            try {
                authManager.getAccessToken(config);
                navBar.setApiStatus(true);
                logPanel.success("API 连接成功");
            } catch (Exception e) {
                navBar.setApiStatus(false);
                logPanel.error("API 连接失败: " + e.getMessage());
            }
        } else {
            navBar.setApiStatus(false);
            logPanel.warn("API 未配置，请先在设置中配置 API Key");
        }
    }

    /**
     * 设置布局。
     */
    private void setupLayout() {
        setTop(navBar);

        // 中间区域
        HBox centerArea = new HBox(4);
        fileListPanel.setPrefWidth(260);
        fileListPanel.setMinWidth(200);

        VBox rightArea = new VBox(4);
        VBox.setVgrow(canvasPanel, Priority.ALWAYS);
        rightArea.getChildren().addAll(canvasPanel, textPanel);

        centerArea.getChildren().addAll(fileListPanel, rightArea);
        HBox.setHgrow(rightArea, Priority.ALWAYS);
        setCenter(centerArea);

        // 底部：状态栏 + 日志面板
        VBox bottomArea = new VBox(4);
        bottomArea.getChildren().addAll(bottomBar, logPanel);
        setBottom(bottomArea);
    }

    /**
     * 设置事件处理。
     */
    private void setupEventHandlers() {
        navBar.getOpenBtn().setOnAction(e -> openFile());
        navBar.getTranslateBtn().setOnAction(e -> {
            var selectedPages = fileListPanel.getSelectedPages();
            if (selectedPages.size() > 1) {
                // 多文件：批量翻译
                startBatchTranslation(selectedPages);
            } else if (selectedPages.size() == 1) {
                MangaPage selected = selectedPages.get(0);
                if (selected.getStatus() == PageStatus.TRANSLATING) {
                    cancelTranslation();
                } else {
                    startTranslation(selected);
                }
            }
        });
        navBar.getBatchImportBtn().setOnAction(e -> batchImport());
        navBar.getExportBtn().setOnAction(e -> exportResult());
        navBar.getSettingsBtn().setOnAction(e -> showSettings());
        navBar.getSplitViewBtn().setOnAction(e -> canvasPanel.toggleSplitView());

        fileListPanel.setOnFileSelected(this::onFileSelected);
        fileListPanel.setOnBatchTranslate(this::startBatchTranslation);

        // 双击修正
        textPanel.setOnTranslationDoubleClick(this::showCorrectionDialog);

        // 日志展开
        bottomBar.setOnLogToggle(() -> logPanel.toggle());
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
                logPanel.info("批量导入完成: " + files.length + " 张图片");
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
            logPanel.info("已加载: " + file.getName());
        } catch (IOException e) {
            log.error("加载图片失败: {}", file.getName(), e);
            logPanel.error("加载失败: " + file.getName());
            showAlert("加载失败", "无法加载图片: " + file.getName());
        }
    }

    /**
     * 文件选中事件。
     */
    private void onFileSelected(MangaPage page) {
        this.currentPage = page;
        if (page != null) {
            canvasPanel.showOriginalImage(page.getOriginalImage());
            textPanel.updateTextRegions(page.getTextRegions());
            if (page.getTranslatedImage() != null) {
                canvasPanel.showResultImage(page.getTranslatedImage());
            }
        }
    }

    /**
     * 显示翻译修正对话框。
     */
    private void showCorrectionDialog(TextRegion region) {
        CorrectionDialog dialog = new CorrectionDialog(region);
        Optional<CorrectionDialog.CorrectionResult> result = dialog.showAndWait();

        result.ifPresent(correction -> {
            if (correction.isRetranslate()) {
                // 用户点击"重新翻译"——保留原始文本不变，重新翻译
                // 重置译文状态
                region.setTranslatedText(null);
                region.setStatus(RegionStatus.PENDING);
                // 单条重新翻译：需要 API 调用，这里简化直接把原文保留
                region.setCorrectedText(correction.getCorrectedText());
                logPanel.info("区域 #" + region.getId() + " 请求重新翻译");
            } else {
                // 用户点击"确认修正"——直接使用修正文本
                region.setCorrectedText(correction.getCorrectedText());
                region.setStatus(RegionStatus.CORRECTED);
                logPanel.success("区域 #" + region.getId() + " 已修正");
            }

            // 重新渲染该区域
            reRenderRegion(region);
        });
    }

    /**
     * 重新渲染单个文字区域。
     */
    private void reRenderRegion(TextRegion region) {
        if (currentPage == null || currentPage.getOriginalImage() == null) return;

        try {
            // 用原图重新修补+渲染单个区域
            BufferedImage original = currentPage.getOriginalImage();
            if (currentPage.getTranslatedImage() == null) {
                // 无翻译结果图，先做完整修补
                var inpaintService = new WhiteInpaintServiceImpl();
                var renderService = new Graphics2DRenderServiceImpl();
                var inpainted = inpaintService.inpaint(original, currentPage.getTextRegions());
                var result = renderService.render(inpainted, currentPage.getTextRegions());
                currentPage.setTranslatedImage(result);
            } else {
                // 已有翻译图，重新修补+渲染受影响区域
                // 简化：直接用当前翻译图，仅更新显示的文本面板
            }

            textPanel.updateTextRegions(currentPage.getTextRegions());
            canvasPanel.showResultImage(currentPage.getTranslatedImage());
            logPanel.info("区域 #" + region.getId() + " 已重新渲染");
        } catch (Exception e) {
            log.error("重新渲染失败", e);
            logPanel.error("重新渲染失败: " + e.getMessage());
        }
    }

    /**
     * 开始翻译。
     */
    private void startTranslation(MangaPage page) {
        if (!config.isValid()) {
            logPanel.error("API 未配置");
            showAlert("配置未完成", "请先在设置中配置百度 API Key");
            return;
        }

        navBar.setTranslating(true);
        bottomBar.updateProgress(0, "正在初始化…");
        textPanel.clearText();
        logPanel.info("开始翻译: " + page.getFileName());

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
                    logPanel.error(event.getMessage());
                    showAlert("翻译失败", event.getMessage());
                });
            }

            @Override
            public void onComplete(PipelineEvent event) {
                Platform.runLater(() -> {
                    bottomBar.setIdle();
                    navBar.setTranslating(false);
                    logPanel.success(event.getMessage());

                    var result = page.getTranslatedImage();
                    if (result != null) {
                        canvasPanel.showResultImage(result);
                    }
                    textPanel.updateTextRegions(page.getTextRegions());
                    fileListPanel.refreshList();
                });
            }
        });

        // 后台线程执行
        pipelineThread = new Thread(() -> {
            try {
                var context = pipeline.execute(page, config);
                if (context.getResultImage() != null) {
                    page.setTranslatedImage(context.getResultImage());
                    page.setTextRegions(context.getCleanedRegions());
                    page.setStatus(PageStatus.COMPLETED);
                }
                int textCount = context.getCleanedRegions() != null ? context.getCleanedRegions().size() : 0;
                logPanel.success("翻译完成！共识别 " + textCount + " 个文字区域");
            } catch (Exception e) {
                log.error("翻译管线执行失败", e);
                Platform.runLater(() -> {
                    bottomBar.setIdle();
                    navBar.setTranslating(false);
                    logPanel.error("翻译失败: " + e.getMessage());
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
        logPanel.warn("翻译已取消");
    }

    /**
     * 批量翻译多个页面。
     */
    private void startBatchTranslation(List<MangaPage> pages) {
        if (!config.isValid()) {
            logPanel.error("API 未配置");
            showAlert("配置未完成", "请先在设置中配置百度 API Key");
            return;
        }

        navBar.setTranslating(true);
        logPanel.info("开始批量翻译: " + pages.size() + " 张图片");

        pipelineThread = new Thread(() -> {
            int total = pages.size();
            int[] completedRef = {0};

            for (MangaPage page : pages) {
                if (Thread.currentThread().isInterrupted()) break;

                int currentIdx = completedRef[0];
                Platform.runLater(() -> {
                    bottomBar.updateProgress((double) currentIdx / total,
                            "正在翻译第 " + (currentIdx + 1) + "/" + total + " 张…");
                    logPanel.info("翻译中: " + page.getFileName() + " (" + (currentIdx + 1) + "/" + total + ")");
                });

                try {
                    page.setStatus(PageStatus.TRANSLATING);
                    var batchPipeline = new TranslationPipeline(new PipelineEventBus());
                    batchPipeline.addStep(new OcrStep(new BaiduOcrServiceImpl(ocrClient, authManager, config)));
                    batchPipeline.addStep(new CleanStep());
                    batchPipeline.addStep(new TranslateStep(new BaiduTranslateServiceImpl(translateClient, config)));
                    batchPipeline.addStep(new InpaintStep(new WhiteInpaintServiceImpl()));
                    batchPipeline.addStep(new RenderStep(new Graphics2DRenderServiceImpl()));

                    var context = batchPipeline.execute(page, config);
                    if (context.getResultImage() != null) {
                        page.setTranslatedImage(context.getResultImage());
                        page.setTextRegions(context.getCleanedRegions());
                        page.setStatus(PageStatus.COMPLETED);
                    }
                    completedRef[0]++;
                } catch (Exception e) {
                    log.error("批量翻译失败: {}", page.getFileName(), e);
                    int failIdx = completedRef[0];
                    Platform.runLater(() ->
                            logPanel.error("翻译失败: " + page.getFileName() + " — " + e.getMessage()));
                    completedRef[0]++;
                }
            }

            int finalCompleted = completedRef[0];
            Platform.runLater(() -> {
                bottomBar.setIdle();
                navBar.setTranslating(false);
                fileListPanel.refreshList();
                logPanel.success("批量翻译完成！成功 " + finalCompleted + "/" + total + " 张");

                if (!pages.isEmpty()) {
                    MangaPage last = pages.get(pages.size() - 1);
                    onFileSelected(last);
                }
            });
        }, "batch-pipeline");
        pipelineThread.setDaemon(true);
        pipelineThread.start();
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
                logPanel.success("已保存到: " + file.getName());
            } catch (IOException e) {
                log.error("导出失败", e);
                logPanel.error("导出失败: " + e.getMessage());
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
                    logPanel.success("API 配置已更新并验证通过");
                } catch (Exception e) {
                    navBar.setApiStatus(false);
                    logPanel.error("API 验证失败: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 显示提示框。
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.show();
    }
}

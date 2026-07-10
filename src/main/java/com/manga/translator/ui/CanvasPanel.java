package com.manga.translator.ui;

import com.manga.translator.model.TextRegion;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 画布面板。
 * <p>
 * 显示原图和翻译结果，支持 Tab 切换、拆分视图、滚轮缩放。
 */
public class CanvasPanel extends VBox {

    private final TabPane tabPane;
    private final Tab originalTab;
    private final Tab resultTab;
    private final ImageView originalImageView;
    private final ImageView resultImageView;
    private final StackPane originalContent;
    private final StackPane resultContent;
    private final Label zoomLabel;

    /** 当前缩放比例 */
    private double currentZoom = 1.0;
    /** 是否拆分视图模式 */
    private boolean splitViewMode = false;

    /** 文字区域列表（用于高亮） */
    private List<TextRegion> textRegions;
    /** 缓存的 JavaFX 原图 */
    private Image cachedOriginalImage;
    /** 缓存的 JavaFX 结果图 */
    private Image cachedResultImage;

    private static final String BG_COLOR = "#1E1E2E";
    private static final String PANEL_BG = "#252540";
    private static final String BORDER_COLOR = "#404058";

    public CanvasPanel() {
        setStyle("-fx-background-color: " + PANEL_BG + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;");
        setSpacing(0);

        // Tab 面板
        tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent; -fx-tab-min-height: 36px;");

        // 原图 Tab
        originalTab = new Tab("原图");
        originalContent = new StackPane();
        originalContent.setStyle("-fx-background-color: " + BG_COLOR + ";");
        originalImageView = new ImageView();
        originalImageView.setPreserveRatio(true);
        originalContent.getChildren().add(originalImageView);
        originalTab.setContent(originalContent);
        originalTab.setClosable(false);

        // 翻译结果 Tab
        resultTab = new Tab("翻译结果");
        resultContent = new StackPane();
        resultContent.setStyle("-fx-background-color: " + BG_COLOR + ";");
        resultImageView = new ImageView();
        resultImageView.setPreserveRatio(true);
        resultContent.getChildren().add(resultImageView);
        resultTab.setContent(resultContent);
        resultTab.setClosable(false);

        tabPane.getTabs().addAll(originalTab, resultTab);
        tabPane.getSelectionModel().select(0);

        // 缩放比例标签
        zoomLabel = new Label("100%");
        zoomLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #606070;");
        StackPane.setAlignment(zoomLabel, javafx.geometry.Pos.TOP_RIGHT);
        StackPane.setMargin(zoomLabel, new javafx.geometry.Insets(8, 12, 0, 0));
        originalContent.getChildren().add(zoomLabel);

        // 滚轮缩放
        originalContent.setOnScroll(this::handleZoom);
        resultContent.setOnScroll(this::handleZoom);

        getChildren().add(tabPane);
    }

    /**
     * 处理滚轮缩放。
     */
    private void handleZoom(ScrollEvent event) {
        double delta = event.getDeltaY();
        if (delta > 0) {
            currentZoom *= 1.1; // 放大
        } else {
            currentZoom *= 0.9; // 缩小
        }
        // 限制范围 10% ~ 500%
        currentZoom = Math.max(0.1, Math.min(5.0, currentZoom));
        updateZoomDisplay();
        applyZoom();
        event.consume();
    }

    /**
     * 重置缩放到 100%。
     */
    public void resetZoom() {
        currentZoom = 1.0;
        updateZoomDisplay();
        applyZoom();
    }

    /**
     * 更新缩放比例显示。
     */
    private void updateZoomDisplay() {
        zoomLabel.setText(String.format("%.0f%%", currentZoom * 100));
    }

    /**
     * 应用缩放到当前显示的图片。
     */
    private void applyZoom() {
        ImageView target = getCurrentImageView();
        Image img = target.getImage();
        if (img != null) {
            target.setFitWidth(img.getWidth() * currentZoom);
            target.setFitHeight(img.getHeight() * currentZoom);
        }
    }

    /**
     * 获取当前显示的 ImageView。
     */
    private ImageView getCurrentImageView() {
        if (splitViewMode) {
            // 拆分模式返回原图的 ImageView（缩放联动同步）
            return originalImageView;
        }
        Tab selected = tabPane.getSelectionModel().getSelectedItem();
        return selected == resultTab ? resultImageView : originalImageView;
    }

    /**
     * 切换拆分视图模式。
     */
    public void toggleSplitView() {
        splitViewMode = !splitViewMode;
        if (splitViewMode) {
            // 进入拆分视图：左右并排显示
            HBox splitContent = new HBox(2);
            splitContent.setStyle("-fx-background-color: " + BG_COLOR + ";");

            // 左半：原图 + 缩放标签
            Label origTitle = new Label("原图");
            origTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #60A5FA;"
                    + "-fx-padding: 6px 12px; -fx-background-color: #2A2A4A;");
            VBox leftSide = new VBox(0, origTitle, originalContent);
            leftSide.setPrefWidth(200);

            // 右半：翻译结果 + 缩放标签
            Label resultTitle = new Label("翻译结果");
            resultTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #60A5FA;"
                    + "-fx-padding: 6px 12px; -fx-background-color: #2A2A4A;");
            VBox rightSide = new VBox(0, resultTitle, resultContent);
            rightSide.setPrefWidth(200);

            HBox.setHgrow(leftSide, Priority.ALWAYS);
            HBox.setHgrow(rightSide, Priority.ALWAYS);

            splitContent.getChildren().addAll(leftSide, rightSide);

            // 替换 tabPane 内容为 splitContent
            tabPane.getTabs().clear();
            Tab splitTab = new Tab("拆分视图");
            splitTab.setContent(splitContent);
            splitTab.setClosable(false);
            tabPane.getTabs().add(splitTab);
            tabPane.getSelectionModel().select(0);

            // 缩放联动
            originalContent.setOnScroll(this::handleZoom);
            resultContent.setOnScroll(this::handleZoom);
        } else {
            // 退出拆分视图：恢复 Tab 模式
            tabPane.getTabs().clear();
            tabPane.getTabs().addAll(originalTab, resultTab);
            tabPane.getSelectionModel().select(1); // 显示结果
        }
    }

    /**
     * 显示原图。
     */
    public void showOriginalImage(BufferedImage image) {
        if (image != null) {
            this.cachedOriginalImage = SwingFXUtils.toFXImage(image, null);
            originalImageView.setImage(cachedOriginalImage);
            applyZoom();
            resetZoom();
        }
        if (!splitViewMode) {
            tabPane.getSelectionModel().select(originalTab);
        }
    }

    /**
     * 显示翻译结果图。
     */
    public void showResultImage(BufferedImage image) {
        if (image != null) {
            this.cachedResultImage = SwingFXUtils.toFXImage(image, null);
            resultImageView.setImage(cachedResultImage);
            applyZoom();
            resetZoom();
        }
        if (!splitViewMode) {
            tabPane.getSelectionModel().select(resultTab);
        }
    }

    /**
     * 清除所有图片。
     */
    public void clearImages() {
        originalImageView.setImage(null);
        resultImageView.setImage(null);
        cachedOriginalImage = null;
        cachedResultImage = null;
    }

    /**
     * 设置文字区域列表（用于高亮绘制）。
     */
    public void setTextRegions(List<TextRegion> regions) {
        this.textRegions = regions;
    }
}

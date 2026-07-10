package com.manga.translator.ui;

import com.manga.translator.model.MangaPage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;

/**
 * 画布面板。
 * <p>
 * 显示原图和翻译结果，支持 Tab 切换。
 * Phase 1 MVP：基本显示功能，无缩放和拖拽。
 */
public class CanvasPanel extends VBox {

    private final TabPane tabPane;
    private final Tab originalTab;
    private final Tab resultTab;
    private final ImageView originalImageView;
    private final ImageView resultImageView;
    private final Label zoomLabel;

    private static final String BG_COLOR = "#1E1E2E";
    private static final String PANEL_BG = "#252540";
    private static final String BORDER_COLOR = "#404058";
    private static final String TAB_ACTIVE_COLOR = "#60A5FA";

    public CanvasPanel() {
        setStyle("-fx-background-color: " + PANEL_BG + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;");
        setSpacing(0);

        // Tab 面板
        tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;"
                + "-fx-tab-min-height: 36px;");

        // 原图 Tab
        originalTab = new Tab("原图");
        StackPane originalContent = new StackPane();
        originalContent.setStyle("-fx-background-color: " + BG_COLOR + ";");
        originalImageView = new ImageView();
        originalImageView.setPreserveRatio(true);
        originalContent.getChildren().add(originalImageView);
        originalTab.setContent(originalContent);
        originalTab.setClosable(false);

        // 翻译结果 Tab
        resultTab = new Tab("翻译结果");
        StackPane resultContent = new StackPane();
        resultContent.setStyle("-fx-background-color: " + BG_COLOR + ";");
        resultImageView = new ImageView();
        resultImageView.setPreserveRatio(true);
        resultContent.getChildren().add(resultImageView);
        resultTab.setContent(resultContent);
        resultTab.setClosable(false);

        tabPane.getTabs().addAll(originalTab, resultTab);
        tabPane.getSelectionModel().select(0);

        // 缩放比例（右上角占位）
        zoomLabel = new Label("100%");
        zoomLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #606070;"
                + "-fx-padding: 8px 12px;");
        StackPane tabArea = (StackPane) tabPane.lookup(".tab-header-area");
        if (tabArea != null) {
            // 先不加 zoomLabel 到 tab 区域，Phase 2 完善
        }

        getChildren().add(tabPane);
    }

    /**
     * 显示原图。
     *
     * @param image 原图
     */
    public void showOriginalImage(BufferedImage image) {
        if (image != null) {
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            originalImageView.setImage(fxImage);
            fitImageToPanel(originalImageView);
        }
        tabPane.getSelectionModel().select(originalTab);
    }

    /**
     * 显示翻译结果图。
     *
     * @param image 翻译结果图
     */
    public void showResultImage(BufferedImage image) {
        if (image != null) {
            Image fxImage = SwingFXUtils.toFXImage(image, null);
            resultImageView.setImage(fxImage);
            fitImageToPanel(resultImageView);
        }
        tabPane.getSelectionModel().select(resultTab);
    }

    /**
     * 清除所有图片。
     */
    public void clearImages() {
        originalImageView.setImage(null);
        resultImageView.setImage(null);
    }

    /**
     * 自适应缩放图片到面板大小。
     */
    private void fitImageToPanel(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double panelW = tabPane.getWidth() - 20;
            double panelH = tabPane.getHeight() - 60;
            if (panelW > 0 && panelH > 0) {
                double scale = Math.min(panelW / img.getWidth(), panelH / img.getHeight());
                imageView.setFitWidth(img.getWidth() * Math.min(1.0, scale));
                imageView.setFitHeight(img.getHeight() * Math.min(1.0, scale));
            }
        }
    }
}

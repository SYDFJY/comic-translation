package com.manga.translator.ui;

import com.manga.translator.model.MangaPage;
import com.manga.translator.model.PageStatus;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * 文件列表单元格渲染器。
 * <p>
 * 显示文件名和状态标签。
 */
public class FileListCell extends ListCell<MangaPage> {

    private static final String DONE_COLOR = "#4ADE80";
    private static final String PROCESSING_COLOR = "#60A5FA";
    private static final String PENDING_COLOR = "#FBBF24";
    private static final String FAILED_COLOR = "#F87171";
    private static final String TEXT_COLOR = "#A0A0B0";
    private static final String HOVER_COLOR = "#3A3A5A";
    private static final String ACTIVE_COLOR = "#E94560";

    @Override
    protected void updateItem(MangaPage page, boolean empty) {
        super.updateItem(page, empty);

        if (empty || page == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        HBox box = new HBox(6);
        box.setStyle("-fx-padding: 5px 8px; -fx-alignment: center-left;");

        VBox info = new VBox(2);
        Label fileName = new Label(page.getFileName());
        fileName.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_COLOR + ";");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 10px;");

        switch (page.getStatus()) {
            case COMPLETED:
                statusLabel.setText("✓ 已完成");
                statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + DONE_COLOR + ";");
                break;
            case TRANSLATING:
                statusLabel.setText("⏳ 翻译中");
                statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + PROCESSING_COLOR + ";");
                break;
            case IDLE:
            case LOADED:
                statusLabel.setText("待翻译");
                statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + PENDING_COLOR + ";");
                break;
            default:
                statusLabel.setText("✗ 失败");
                statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + FAILED_COLOR + ";");
                break;
        }

        info.getChildren().addAll(fileName, statusLabel);
        box.getChildren().add(info);

        // 选中状态
        if (isSelected()) {
            box.setStyle("-fx-padding: 5px 8px; -fx-background-color: " + ACTIVE_COLOR + ";"
                    + "-fx-background-radius: 4px;");
            fileName.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: 500;");
        } else {
            box.setStyle("-fx-padding: 5px 8px;");
        }

        setGraphic(box);
    }
}

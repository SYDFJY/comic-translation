package com.manga.translator.ui;

import com.manga.translator.model.MangaPage;
import com.manga.translator.model.PageStatus;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 文件列表面板。
 * <p>
 * 显示已导入图片的文件名和状态列表。
 * Phase 2：支持多选、右键菜单增强。
 */
public class FileListPanel extends VBox {

    private final ListView<MangaPage> fileList;
    private final Label headerLabel;
    private final List<MangaPage> pages;

    private Consumer<MangaPage> onFileSelected;
    private Consumer<List<MangaPage>> onBatchTranslate;
    private Runnable onDeleteFile;
    private Runnable onRetranslate;

    private static final String HEADER_COLOR = "#60A5FA";
    private static final String PANEL_BG = "#252540";
    private static final String BORDER_COLOR = "#404058";

    public FileListPanel() {
        setStyle("-fx-background-color: " + PANEL_BG + ";"
                + "-fx-border-color: " + BORDER_COLOR + ";"
                + "-fx-border-radius: 8px;"
                + "-fx-background-radius: 8px;");

        this.pages = new ArrayList<>();

        // 头部
        headerLabel = new Label("文件列表");
        headerLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: " + HEADER_COLOR + ";"
                + "-fx-padding: 8px 10px;");

        // 文件列表（支持多选）
        fileList = new ListView<>();
        fileList.setStyle("-fx-background-color: transparent; -fx-border: none;");
        fileList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        fileList.setCellFactory(param -> new FileListCell());

        // 选择事件
        fileList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null && onFileSelected != null) {
                onFileSelected.accept(selected);
            }
        });

        // 右键菜单
        ContextMenu contextMenu = new ContextMenu();

        MenuItem translateItem = new MenuItem("翻译选定文件");
        translateItem.setOnAction(e -> {
            var selected = fileList.getSelectionModel().getSelectedItems();
            if (!selected.isEmpty() && onBatchTranslate != null) {
                onBatchTranslate.accept(new ArrayList<>(selected));
            }
        });

        MenuItem deleteItem = new MenuItem("删除");
        deleteItem.setOnAction(e -> {
            MangaPage selected = fileList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                pages.remove(selected);
                refreshList();
            }
        });

        MenuItem retranslateItem = new MenuItem("重新翻译");
        retranslateItem.setOnAction(e -> {
            if (onRetranslate != null) onRetranslate.run();
        });

        contextMenu.getItems().addAll(translateItem, deleteItem, retranslateItem);
        fileList.setContextMenu(contextMenu);

        getChildren().addAll(headerLabel, fileList);
    }

    /**
     * 添加一个漫画页面到列表。
     */
    public void addPage(MangaPage page) {
        pages.add(page);
        refreshList();
        fileList.getSelectionModel().select(page);
    }

    /**
     * 获取当前选中的页面。
     */
    public MangaPage getSelectedPage() {
        return fileList.getSelectionModel().getSelectedItem();
    }

    /**
     * 获取所有选中的页面（多选）。
     */
    public List<MangaPage> getSelectedPages() {
        return new ArrayList<>(fileList.getSelectionModel().getSelectedItems());
    }

    /**
     * 获取所有页面。
     */
    public List<MangaPage> getPages() {
        return pages;
    }

    /**
     * 刷新列表显示。
     */
    public void refreshList() {
        fileList.getItems().setAll(pages);
    }

    /**
     * 获取文件数量。
     */
    public int getPageCount() {
        return pages.size();
    }

    /**
     * 获取待翻译的文件列表（未完成的）。
     */
    public List<MangaPage> getPendingPages() {
        List<MangaPage> pending = new ArrayList<>();
        for (MangaPage page : pages) {
            if (page.getStatus() != PageStatus.COMPLETED && page.getStatus() != PageStatus.TRANSLATING) {
                pending.add(page);
            }
        }
        return pending;
    }

    // === 事件设置 ===

    public void setOnFileSelected(Consumer<MangaPage> callback) { this.onFileSelected = callback; }
    public void setOnBatchTranslate(Consumer<List<MangaPage>> callback) { this.onBatchTranslate = callback; }
    public void setOnDeleteFile(Runnable callback) { this.onDeleteFile = callback; }
    public void setOnRetranslate(Runnable callback) { this.onRetranslate = callback; }
}

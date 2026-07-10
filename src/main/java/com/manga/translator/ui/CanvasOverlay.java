package com.manga.translator.ui;

import com.manga.translator.model.TextRegion;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * 画布覆盖层。
 * <p>
 * 在图片上方叠加透明层，用于绘制文字区域高亮框。
 * 支持悬停高亮和选中高亮。
 */
public class CanvasOverlay {

    private final Canvas canvas;
    private final GraphicsContext gc;

    /** 当前高亮的区域 ID（-1 表示无高亮） */
    private int highlightedRegionId = -1;

    /** 图片缩放比例 */
    private double scale = 1.0;

    private static final Color HIGHLIGHT_COLOR = Color.rgb(96, 165, 250, 0.3);
    private static final Color SELECTED_COLOR = Color.rgb(233, 69, 96, 0.4);

    public CanvasOverlay() {
        this.canvas = new Canvas();
        this.gc = canvas.getGraphicsContext2D();

        // 鼠标移动跟踪
        canvas.setOnMouseMoved(e -> {
            // 由外部通过坐标转换调用 setHighlightedRegion
        });
    }

    /**
     * 获取 Canvas 节点（添加到 StackPane 覆盖层）。
     *
     * @return Canvas 对象
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * 更新 Canvas 尺寸。
     *
     * @param width  宽度
     * @param height 高度
     */
    public void setSize(double width, double height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
    }

    /**
     * 设置图片缩放比例。
     *
     * @param scale 缩放比例
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * 设置高亮区域。
     *
     * @param regionId 区域 ID，-1 清除高亮
     */
    public void setHighlightedRegion(int regionId) {
        this.highlightedRegionId = regionId;
    }

    /**
     * 重绘高亮框。
     *
     * @param regions 文字区域列表
     */
    public void drawHighlights(List<TextRegion> regions) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (regions == null || regions.isEmpty()) return;

        for (TextRegion region : regions) {
            double x = region.getExpandLeft() * scale;
            double y = region.getExpandTop() * scale;
            double w = region.getExpandWidth() * scale;
            double h = region.getExpandHeight() * scale;

            if (region.getId() == highlightedRegionId) {
                // 选中区域：红色半透明
                gc.setFill(SELECTED_COLOR);
                gc.fillRect(x, y, w, h);
                gc.setStroke(Color.rgb(233, 69, 96, 0.8));
                gc.setLineWidth(2);
                gc.strokeRect(x, y, w, h);
            } else {
                // 默认区域：蓝色半透明（仅悬停时显示全部）
                // 默认不绘制，只有悬停时绘制
            }
        }
    }

    /**
     * 清除所有高亮。
     */
    public void clear() {
        highlightedRegionId = -1;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
}

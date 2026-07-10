package com.manga.translator.pipeline;

import com.manga.translator.model.TextRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文本清洗与合并步骤。
 * <p>
 * 对 OCR 结果进行去噪、合并相邻区域、标记排版方向。
 * 权重：10（占管线 10% 进度）
 */
public class CleanStep implements TranslationStep {

    private static final Logger log = LoggerFactory.getLogger(CleanStep.class);

    /** 噪声字符正则：Unicode 特殊符号区间 */
    private static final Pattern NOISE_PATTERN = Pattern.compile("[\\u2000-\\u206F\\uFF00-\\uFFEF]");
    /** y 坐标合并阈值（px） */
    private static final int Y_DIFF_THRESHOLD = 8;
    /** x 轴重叠比例阈值 */
    private static final double X_OVERLAP_RATIO = 0.6;

    @Override
    public void execute(PipelineContext context) {
        log.info("开始文本清洗与合并步骤");

        if (context.isCancelled()) return;

        List<TextRegion> regions = context.getTextRegions();
        if (regions == null || regions.isEmpty()) {
            log.info("无文字区域需要清洗");
            context.setCleanedRegions(new ArrayList<>());
            return;
        }

        // 1. 去噪声
        List<TextRegion> cleaned = new ArrayList<>();
        for (TextRegion region : regions) {
            String cleanedText = NOISE_PATTERN.matcher(region.getOriginalText()).replaceAll("");
            region.setOriginalText(cleanedText);

            // 去除噪声后可能为空
            if (!cleanedText.isEmpty()) {
                cleaned.add(region);
            }
        }

        // 2. 合并相邻区域
        List<TextRegion> merged = mergeRegions(cleaned);

        log.info("文本清洗完成: 清洗前={}, 合并后={}", regions.size(), merged.size());
        context.setCleanedRegions(merged);
    }

    /**
     * 合并相邻文字区域。
     * <p>
     * 如果两个区域的 y 坐标差 < 8px 且 x 轴重叠 > 60%，视为同一行。
     */
    private List<TextRegion> mergeRegions(List<TextRegion> regions) {
        List<TextRegion> result = new ArrayList<>();
        boolean[] merged = new boolean[regions.size()];

        for (int i = 0; i < regions.size(); i++) {
            if (merged[i]) continue;

            TextRegion base = regions.get(i);
            StringBuilder combinedText = new StringBuilder(base.getOriginalText());

            for (int j = i + 1; j < regions.size(); j++) {
                if (merged[j]) continue;

                TextRegion candidate = regions.get(j);

                // y 坐标差检查
                int yDiff = Math.abs(base.getTop() - candidate.getTop());
                if (yDiff > Y_DIFF_THRESHOLD) continue;

                // x 轴重叠检查
                if (hasXOverlap(base, candidate)) {
                    combinedText.append(candidate.getOriginalText());
                    // 合并区域：取并集
                    base.setLeft(Math.min(base.getLeft(), candidate.getLeft()));
                    base.setTop(Math.min(base.getTop(), candidate.getTop()));
                    base.setWidth(Math.max(base.getLeft() + base.getWidth(), candidate.getLeft() + candidate.getWidth()) - base.getLeft());
                    base.setHeight(Math.max(base.getTop() + base.getHeight(), candidate.getTop() + candidate.getHeight()) - base.getTop());
                    merged[j] = true;
                }
            }

            base.setOriginalText(combinedText.toString());
            result.add(base);
        }

        return result;
    }

    /**
     * 检查两个区域在 x 轴上是否有重叠。
     */
    private boolean hasXOverlap(TextRegion a, TextRegion b) {
        int aLeft = a.getLeft();
        int aRight = a.getLeft() + a.getWidth();
        int bLeft = b.getLeft();
        int bRight = b.getLeft() + b.getWidth();

        int overlapLeft = Math.max(aLeft, bLeft);
        int overlapRight = Math.min(aRight, bRight);

        if (overlapRight <= overlapLeft) return false;

        int overlapWidth = overlapRight - overlapLeft;
        int minWidth = Math.min(a.getWidth(), b.getWidth());

        return (double) overlapWidth / minWidth >= X_OVERLAP_RATIO;
    }

    @Override
    public String getStepName() {
        return "文本清洗";
    }

    @Override
    public int getWeight() {
        return 10;
    }
}

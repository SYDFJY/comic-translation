package com.manga.translator.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 图片工具类。
 * <p>
 * 提供图片编码、解码、缩放等基础操作。
 */
public class ImageUtil {

    private ImageUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 将 BufferedImage 编码为 Base64 字符串。
     *
     * @param image  图片对象
     * @param format 图片格式（png/jpg）
     * @return Base64 编码字符串（不含 data:image/xxx;base64, 前缀）
     * @throws IOException 如果编码失败
     */
    public static String imageToBase64(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 从文件路径加载图片。
     *
     * @param filePath 图片文件路径
     * @return BufferedImage 对象
     * @throws IOException 如果加载失败
     */
    public static BufferedImage loadImage(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }
        return ImageIO.read(file);
    }

    /**
     * 保存图片到文件。
     *
     * @param image    图片对象
     * @param filePath 目标文件路径
     * @param format   图片格式（png/jpg）
     * @throws IOException 如果保存失败
     */
    public static void saveImage(BufferedImage image, String filePath, String format) throws IOException {
        File outputFile = new File(filePath);
        // 确保父目录存在
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        ImageIO.write(image, format, outputFile);
    }

    /**
     * 创建图片副本。
     *
     * @param source 源图片
     * @return 副本图片（同一像素数据，可独立操作）
     */
    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                source.getType() != 0 ? source.getType() : BufferedImage.TYPE_INT_ARGB
        );
        copy.getGraphics().drawImage(source, 0, 0, null);
        return copy;
    }
}

package com.manga.translator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(ImageUtil.class);

    private ImageUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 将 BufferedImage 编码为 Base64 字符串。
     * <p>
     * 如果原图类型无法编码，自动转换为 TYPE_INT_RGB。
     *
     * @param image  图片对象
     * @param format 图片格式（png/jpg）
     * @return Base64 编码字符串（不含 data:image/xxx;base64, 前缀）
     * @throws IOException 如果编码失败
     */
    public static String imageToBase64(BufferedImage image, String format) throws IOException {
        BufferedImage writeable = ensureWriteable(image);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean written = ImageIO.write(writeable, format, outputStream);
        if (!written) {
            throw new IOException("ImageIO 无法编码图片格式: " + format + ", type=" + image.getType());
        }
        byte[] imageBytes = outputStream.toByteArray();
        if (imageBytes.length == 0) {
            throw new IOException("图片编码结果为空");
        }
        log.debug("图片 Base64 编码完成: 原大小={}x{}, Base64长度={}",
                image.getWidth(), image.getHeight(), imageBytes.length);
        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 确保图片类型可被 ImageIO 编码。
     * <p>
     * TYPE_CUSTOM (0) 或 TYPE_BYTE_BINARY (12) 等类型可能无法编码，
     * 转换为标准 TYPE_INT_RGB 或 TYPE_INT_ARGB。
     */
    private static BufferedImage ensureWriteable(BufferedImage image) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_RGB || type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_ARGB_PRE || type == BufferedImage.TYPE_3BYTE_BGR
                || type == BufferedImage.TYPE_4BYTE_ABGR || type == BufferedImage.TYPE_4BYTE_ABGR_PRE) {
            return image; // 直接可用
        }
        // 转换为 TYPE_INT_ARGB
        BufferedImage converted = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        converted.getGraphics().drawImage(image, 0, 0, null);
        log.info("图片类型转换: {} -> TYPE_INT_ARGB", type);
        return converted;
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
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("ImageIO 无法解析图片: " + filePath);
        }
        log.debug("图片加载完成: {}, {}x{}, type={}", filePath, image.getWidth(), image.getHeight(), image.getType());
        return image;
    }

    /**
     * 保存图片到文件。
     */
    public static void saveImage(BufferedImage image, String filePath, String format) throws IOException {
        File outputFile = new File(filePath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        BufferedImage writeable = ensureWriteable(image);
        boolean written = ImageIO.write(writeable, format, outputFile);
        if (!written) {
            throw new IOException("ImageIO 无法保存图片格式: " + format);
        }
    }

    /**
     * 创建图片副本。
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

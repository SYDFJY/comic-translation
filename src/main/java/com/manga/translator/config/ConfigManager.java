package com.manga.translator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manga.translator.ConfigException;
import com.manga.translator.model.TranslationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 配置管理器。
 * <p>
 * 负责读取、写入百度 API 密钥等配置信息。
 * 配置文件存储在用户主目录下的 ~/.manga-translator/config.json，
 * 使用 AES-128 加密存储密钥字段。
 */
public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

    /** 配置文件相对路径 */
    private static final String CONFIG_DIR = ".manga-translator";
    private static final String CONFIG_FILE = "config.json";

    private final Gson gson;
    private final Path configPath;

    public ConfigManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.configPath = Paths.get(System.getProperty("user.home"), CONFIG_DIR, CONFIG_FILE);
    }

    /**
     * 获取配置文件路径。
     *
     * @return 配置文件绝对路径
     */
    public Path getConfigPath() {
        return configPath;
    }

    /**
     * 检查配置文件是否存在。
     *
     * @return true 如果配置文件存在
     */
    public boolean configExists() {
        return Files.exists(configPath);
    }

    /**
     * 加载配置。
     * <p>
     * 从 ~/.manga-translator/config.json 读取配置。
     *
     * @return TranslationConfig 对象
     * @throws ConfigException 如果配置文件不存在或读取失败
     */
    public TranslationConfig loadConfig() {
        if (!configExists()) {
            log.warn("配置文件不存在: {}", configPath);
            return new TranslationConfig();
        }

        try (FileReader reader = new FileReader(configPath.toFile())) {
            TranslationConfig config = gson.fromJson(reader, TranslationConfig.class);
            log.info("配置已加载: {}", configPath);
            return config != null ? config : new TranslationConfig();
        } catch (IOException e) {
            log.error("读取配置文件失败: {}", configPath, e);
            throw new ConfigException("读取配置文件失败: " + configPath, e);
        }
    }

    /**
     * 保存配置。
     * <p>
     * 写入 ~/.manga-translator/config.json。
     *
     * @param config 翻译配置
     * @throws ConfigException 如果写入失败
     */
    public void saveConfig(TranslationConfig config) {
        try {
            // 确保目录存在
            Files.createDirectories(configPath.getParent());

            try (FileWriter writer = new FileWriter(configPath.toFile())) {
                gson.toJson(config, writer);
            }
            log.info("配置已保存: {}", configPath);
        } catch (IOException e) {
            log.error("保存配置文件失败: {}", configPath, e);
            throw new ConfigException("保存配置文件失败: " + configPath, e);
        }
    }

    /**
     * 删除配置文件。
     */
    public void deleteConfig() {
        try {
            Files.deleteIfExists(configPath);
            log.info("配置文件已删除: {}", configPath);
        } catch (IOException e) {
            log.warn("删除配置文件失败: {}", configPath, e);
        }
    }
}

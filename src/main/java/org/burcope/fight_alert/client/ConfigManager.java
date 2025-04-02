package org.burcope.fight_alert.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Path CONFIG_PATH = Paths.get("config/fight_alert.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class Config {
        public String webhookUrl = "";
    }

    public static Config loadConfig() {
        try {
            if (CONFIG_PATH.getParent() != null && !Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }

            if (!Files.exists(CONFIG_PATH)) {
                System.out.println("📢 설정 파일이 존재하지 않습니다. 새로 생성합니다.");
                Config defaultConfig = new Config();
                saveConfig(defaultConfig);
                return defaultConfig;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                return GSON.fromJson(reader, Config.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Config();
        }
    }

    public static void saveConfig(Config config) {
        try {
            if (CONFIG_PATH.getParent() != null && !Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }

            Config existingConfig = loadConfig();
            if (config.webhookUrl == null || config.webhookUrl.isEmpty()) {
                config.webhookUrl = existingConfig.webhookUrl;
            }

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }

            System.out.println("✅ 설정 파일이 저장되었습니다. (webhookUrl : " + config.webhookUrl + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

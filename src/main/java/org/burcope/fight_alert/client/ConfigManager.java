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

    /**
     * 설정 파일을 로드합니다.
     * 파일이 없을 경우 기본값으로 새로운 파일을 생성합니다.
     */
    public static Config loadConfig() {
        try {
            // 설정 파일 디렉토리 확인 및 생성
            if (CONFIG_PATH.getParent() != null && !Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }

            // 설정 파일이 존재하지 않으면 기본 설정 저장
            if (!Files.exists(CONFIG_PATH)) {
                System.out.println("📢 설정 파일이 존재하지 않습니다. 새로 생성합니다.");
                Config defaultConfig = new Config();
                saveConfig(defaultConfig);
                return defaultConfig;
            }

            // 기존 설정 파일 읽기
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                return GSON.fromJson(reader, Config.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new Config(); // 오류 발생 시 기본값 반환
        }
    }

    /**
     * 설정 파일을 저장합니다.
     * 기존 설정을 유지하면서 새로운 값을 반영합니다.
     */
    public static void saveConfig(Config config) {
        try {
            // 설정 파일 디렉토리 생성 (없으면 생성)
            if (CONFIG_PATH.getParent() != null && !Files.exists(CONFIG_PATH.getParent())) {
                Files.createDirectories(CONFIG_PATH.getParent());
            }

            // 기존 설정을 유지하면서 새로운 값 적용
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

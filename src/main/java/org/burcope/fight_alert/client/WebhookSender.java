package org.burcope.fight_alert.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebhookSender {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void sendWebhook(String webhookUrl, String jsonPayload) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            System.err.println("❌ [WebhookSender] 웹훅 URL이 설정되지 않았습니다.");
            return;
        }

        executor.submit(() -> {
            HttpURLConnection connection = null;
            try {
                JsonObject json;
                try {
                    json = JsonParser.parseString(jsonPayload).getAsJsonObject();
                } catch (Exception e) {
                    System.err.println("❌ [WebhookSender] JSON 파싱 실패: " + e.getMessage());
                    return;
                }

                URL url = new URL(webhookUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == 204) {
                    System.out.println("✅ [WebhookSender] 웹훅 전송 성공");
                    sendClientMessage("✅ 한타 알림이 성공적으로 전송되었습니다.");
                } else {
                    System.err.println("⚠️ [WebhookSender] 웹훅 전송 실패 | 응답 코드 : " + responseCode);
                    sendClientMessage("⚠️ 웹훅 전송에 실패했습니다 | 응답 코드 : " + responseCode);
                }
            } catch (Exception e) {
                System.err.println("❌ [WebhookSender] 웹훅 전송 중 오류 발생 : " + e.getMessage());
                sendClientMessage("❌ 웹훅 전송 중 오류가 발생했습니다! 로그를 확인하세요.");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static void sendClientMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.of(message), false);
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executor.shutdown();
            System.out.println("🛑 [WebhookSender] ExecutorService 종료됨.");
        }));
    }
}

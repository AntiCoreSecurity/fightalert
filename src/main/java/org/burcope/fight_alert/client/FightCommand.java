package org.burcope.fight_alert.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class FightCommand {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("한타")
                    .then(ClientCommandManager.literal("설정")
                            .then(ClientCommandManager.argument("webhook_url", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String url = StringArgumentType.getString(context, "webhook_url");

                                        // 설정 파일에 저장
                                        ConfigManager.Config config = ConfigManager.loadConfig();
                                        config.webhookUrl = url;
                                        ConfigManager.saveConfig(config);

                                        context.getSource().sendFeedback(Text.of("✅ 웹훅 URL이 설정되었습니다!"));
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("알림")
                            .then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1))
                                    .then(ClientCommandManager.argument("content", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                int count = IntegerArgumentType.getInteger(context, "count");
                                                String content = StringArgumentType.getString(context, "content");
                                                sendFightAlert(content, count);
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                    )
            );
        });
    }

    private static void sendFightAlert(String content, int count) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ConfigManager.Config config = ConfigManager.loadConfig();
        if (config.webhookUrl == null || config.webhookUrl.isEmpty()) {
            client.player.sendMessage(Text.of("⚠️ 웹훅 URL이 설정되지 않았습니다. `/한타 설정 [webhook_url]` 명령어를 사용하세요."), false);
            return;
        }

        String playerName = client.player.getGameProfile().getName();
        String serverName = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().name : "싱글플레이";
        int x = (int) client.player.getX();
        int y = (int) client.player.getY();
        int z = (int) client.player.getZ();

        String playerUUID = client.player.getGameProfile().getId().toString();
        String skinUrl = "https://mc-heads.net/avatar/" + playerUUID + "/400";

        // JSON 데이터 생성
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("content", "@everyone " + content);

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "");
        embed.addProperty("description", "> **" + content + "**");
        embed.addProperty("color", 16711680);

        JsonObject author = new JsonObject();
        author.addProperty("name", "한타 알림");
        embed.add("author", author);

        JsonArray fields = new JsonArray();

        JsonObject requesterField = new JsonObject();
        requesterField.addProperty("name", "요청자");
        requesterField.addProperty("value", "``" + playerName + "``");
        requesterField.addProperty("inline", false);
        fields.add(requesterField);

        JsonObject coordsField = new JsonObject();
        coordsField.addProperty("name", "좌표");
        coordsField.addProperty("value", "``" + x + ", " + y + ", " + z + "``");
        coordsField.addProperty("inline", false);
        fields.add(coordsField);

        embed.add("fields", fields);

        JsonObject thumbnail = new JsonObject();
        thumbnail.addProperty("url", skinUrl);
        embed.add("thumbnail", thumbnail);

        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        jsonPayload.add("embeds", embeds);

        // 웹훅 전송
        for (int i = 0; i < count; i++) {
            WebhookSender.sendWebhook(config.webhookUrl, jsonPayload.toString());
        }
    }
}

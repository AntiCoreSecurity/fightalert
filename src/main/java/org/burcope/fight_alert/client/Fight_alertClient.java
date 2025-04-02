package org.burcope.fight_alert.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fight_alertClient implements ClientModInitializer {

    public static final String MOD_ID = "fight_alert";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[FightAlertMod] 클라이언트 모드 초기화 중...");

        ConfigManager.loadConfig();

        FightCommand.registerCommands();

        LOGGER.info("[FightAlertMod] 초기화 완료!");
    }
}
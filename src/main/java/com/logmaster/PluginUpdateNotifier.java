package com.logmaster;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.logmaster.LogMasterConfig.CONFIG_GROUP;
import static com.logmaster.LogMasterConfig.PLUGIN_VERSION_KEY;

@Slf4j
@Singleton
public class PluginUpdateNotifier {
    private static final String PLUGIN_VERSION_TOKEN = "%PLUGIN_VERSION%";

    private static final String[] UPDATE_MESSAGES = {
            "<colHIGHLIGHT>Collection Log Master updated to v" + PLUGIN_VERSION_TOKEN,
            "<colHIGHLIGHT>- Added task synchronization",
            "<colHIGHLIGHT>- Added dynamic task image config option"
    };

    @Inject
    EventBus eventBus;

    @Inject
    ConfigManager configManager;

    @Inject
    ChatMessageManager chatMessageManager;

    public void startUp() {
        eventBus.register(this);
    }

    public void shutDown() {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState gameState = gameStateChanged.getGameState();
        if (gameState == GameState.LOGGED_IN) {
            checkUpdate();
        }
    }

    private String getPluginVersion() {
        try (InputStream is = LogMasterPlugin.class.getResourceAsStream("version")) {
            assert is != null;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-SNAPSHOT", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkUpdate() {
        boolean isDebug = false;
        String curVersion = getPluginVersion();
        String lastVersion = configManager.getRSProfileConfiguration(CONFIG_GROUP, PLUGIN_VERSION_KEY);

        //noinspection ConstantValue
        if (isDebug || !curVersion.equals(lastVersion)) {
            configManager.setRSProfileConfiguration(CONFIG_GROUP, PLUGIN_VERSION_KEY, curVersion);
            notifyUpdate(curVersion);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void notifyUpdate(String curVersion) {
        if (UPDATE_MESSAGES == null) return;

        String replacedMessage = String.join("<br>", UPDATE_MESSAGES).replace(PLUGIN_VERSION_TOKEN, curVersion);
        chatMessageManager.queue(
                QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(replacedMessage)
                        .build()
        );
    }
}

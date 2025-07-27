package com.logmaster.task;

import com.google.gson.JsonSyntaxException;
import com.logmaster.domain.savedata.BaseSaveData;
import com.logmaster.domain.savedata.SaveData;
import com.logmaster.domain.savedata.SaveDataUpdater;
import com.logmaster.util.EventBusSubscriber;
import com.logmaster.util.SimpleDebouncer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;

import static com.logmaster.LogMasterConfig.CONFIG_GROUP;
import static com.logmaster.util.GsonOverride.GSON;

@Singleton
@Slf4j
public class SaveDataStorage extends EventBusSubscriber {
    public static final String SAVE_DATA_KEY = "save-data";

    public static final String SAVE_DATA_BACKUP_KEY_BASE = "save-data-bk";

    @Inject
    private ConfigManager configManager;

    @Inject
    private SaveDataUpdater saveDataUpdater;

    @Inject
    private SimpleDebouncer saveDebouncer;

    private SaveData data;

    @Override
    public void startUp() {
        super.startUp();
        load();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e) {
        GameState state = e.getGameState();
        switch (state) {
            case LOGGED_IN:
                load();
                break;

            case LOGIN_SCREEN:
                saveImmediately();
                break;
        }
    }

    public SaveData get() {
        return data;
    }

    public void save() {
        log.debug("Scheduling save; {}", Instant.now());
        saveDebouncer.debounce(this::saveImmediately);
    }

    public void saveImmediately() {
        log.debug("Saving; {}", Instant.now());
        String json = GSON.toJson(data);
        configManager.setRSProfileConfiguration(CONFIG_GROUP, SAVE_DATA_KEY, json);
    }

    public void saveBackup(BaseSaveData data) {
        String json = GSON.toJson(data);
        configManager.setRSProfileConfiguration(
                CONFIG_GROUP,
                SAVE_DATA_BACKUP_KEY_BASE + data.getVersion(),
                json
        );
    }

    private void load() {
        data = read();
    }

    private @NonNull SaveData read() {
        String json = configManager.getRSProfileConfiguration(CONFIG_GROUP, SAVE_DATA_KEY);
        if (json == null) {
            return new SaveData();
        }

        try {
            return saveDataUpdater.update(json);
        } catch (JsonSyntaxException e) {
            log.error("Unable to parse save data JSON", e);
        }

        return new SaveData();
    }
}

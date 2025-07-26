package com.logmaster.task;

import com.google.gson.JsonSyntaxException;
import com.logmaster.domain.savedata.SaveData;
import com.logmaster.domain.savedata.SaveDataUpdater;
import com.logmaster.util.EventBusSubscriber;
import io.github.bhowell2.debouncer.Debouncer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

import static com.logmaster.LogMasterConfig.CONFIG_GROUP;
import static com.logmaster.LogMasterConfig.SAVE_DATA_KEY;
import static com.logmaster.util.GsonOverride.GSON;

@Singleton
@Slf4j
public class SaveDataStorage extends EventBusSubscriber {
    public static final int SAVE_DEBOUNCE_INTERVAL = 500;

    @Inject
    private ConfigManager configManager;

    private final Debouncer<String> saveDebouncer = new Debouncer<>(1);

    private SaveData data;

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
        saveDebouncer.addRunLast(
                SAVE_DEBOUNCE_INTERVAL,
                TimeUnit.MILLISECONDS,
                "save",
                (k) -> saveImmediately()
        );
    }

    public void saveImmediately() {
        String json = GSON.toJson(data);
        this.configManager.setRSProfileConfiguration(CONFIG_GROUP, SAVE_DATA_KEY, json);
    }

    private void load() {
        data = read();
    }

    private @NonNull SaveData read() {
        String json = this.configManager.getRSProfileConfiguration(CONFIG_GROUP, SAVE_DATA_KEY);
        if (json == null) {
            return new SaveData();
        }

        try {
            return SaveDataUpdater.update(json);
        } catch (JsonSyntaxException e) {
            log.error("Unable to parse save data JSON", e);
        }

        return new SaveData();
    }
}

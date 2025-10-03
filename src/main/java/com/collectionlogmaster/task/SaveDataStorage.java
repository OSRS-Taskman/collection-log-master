package com.collectionlogmaster.task;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.google.gson.JsonSyntaxException;
import com.collectionlogmaster.domain.savedata.BaseSaveData;
import com.collectionlogmaster.domain.savedata.SaveData;
import com.collectionlogmaster.domain.savedata.SaveDataUpdater;
import com.collectionlogmaster.util.EventBusSubscriber;
import com.collectionlogmaster.util.SimpleDebouncer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;

import static com.collectionlogmaster.CollectionLogMasterConfig.CONFIG_GROUP;
import static com.collectionlogmaster.util.GsonOverride.GSON;

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
        importOldPluginSave();
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

	private void importOldPluginSave() {
		Boolean alreadyImported = configManager.getRSProfileConfiguration(
				CollectionLogMasterConfig.CONFIG_GROUP,
				"oldPluginSaveImported",
				Boolean.class
		);

		if (alreadyImported != null && alreadyImported) {
			return;
		}

        log.info("Importing old plugin save for profile {}", configManager.getRSProfileKey());
        String oldSave = configManager.getRSProfileConfiguration("log-master", SAVE_DATA_KEY);
        log.info("Old save: {}", oldSave);

        if (oldSave != null) {
            configManager.setRSProfileConfiguration(CONFIG_GROUP, SAVE_DATA_KEY, oldSave);
        }

        configManager.setRSProfileConfiguration(CONFIG_GROUP, "oldPluginSaveImported", true);
	}
}

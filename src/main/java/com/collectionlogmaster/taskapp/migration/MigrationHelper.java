package com.collectionlogmaster.taskapp.migration;

import static com.collectionlogmaster.CollectionLogMasterConfig.CONFIG_GROUP;
import static com.collectionlogmaster.util.GsonOverride.GSON;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.taskapp.TaskAppClient;
import com.collectionlogmaster.taskapp.TaskAppStateStorage;
import com.collectionlogmaster.taskapp.TaskService;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
@Singleton
public class MigrationHelper {
    private static final String SAVE_DATA_KEY = "save-data";

	@Inject
	private ConfigManager configManager;

	@Inject
	private TaskService taskService;

	@Inject
	private TaskAppStateStorage taskAppStateStorage;

	@Inject
	private TaskAppClient taskAppClient;

	public boolean canMigrate() {
		SaveData saveData = getOldSaveData();
		String oldActiveTaskId = saveData.getActiveTaskId();
		boolean isSameActiveTask = isIsSameActiveTask(oldActiveTaskId);

		return !saveData.isMigrated()
			&& !isSameActiveTask
			&& oldActiveTaskId != null
			&& !taskService.isComplete(oldActiveTaskId)
			&& !taskAppStateStorage.get().hasMigrated();
	}

	public CompletableFuture<Void> migrate() {
		SaveData saveData = getOldSaveData();

		return taskAppClient.migrate(saveData.getActiveTaskId())
			.thenRun(this::markAsMigrated);
	}

	public Task getOldActiveTask() {
		SaveData saveData = getOldSaveData();
		String activeTaskId = saveData.getActiveTaskId();

		return taskService.getTaskById(activeTaskId);
	}

	public SaveData getOldSaveData() {
		String json = configManager.getRSProfileConfiguration(CONFIG_GROUP, SAVE_DATA_KEY);
		if (json == null) {
			return new SaveData();
		}

		try {
			return GSON.fromJson(json, SaveData.class);
		} catch (Exception ignored) { }

		return new SaveData();
	}

	public void markAsMigrated() {
		markAsMigrated(true);
	}

	public void markAsMigrated(boolean migrated) {
		SaveData saveData = getOldSaveData();

		saveData.setMigrated(migrated);

		String json = GSON.toJson(saveData);
        configManager.setRSProfileConfiguration(CONFIG_GROUP, SAVE_DATA_KEY, json);
	}

	private boolean isIsSameActiveTask(String oldActiveTaskId) {
		Task newActiveTask = taskService.getActiveTask();

		return newActiveTask != null
			&& newActiveTask.getId().equals(oldActiveTaskId);
	}
}

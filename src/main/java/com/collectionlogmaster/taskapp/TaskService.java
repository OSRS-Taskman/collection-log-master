package com.collectionlogmaster.taskapp;

import static com.collectionlogmaster.CollectionLogMasterConfig.CONFIG_GROUP;
import static com.collectionlogmaster.util.GsonOverride.GSON;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.domain.Tag;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.taskapp.response.SyncResponse;
import com.collectionlogmaster.util.EventBusSubscriber;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Singleton
@Slf4j
public class TaskService extends EventBusSubscriber {
	@Inject
	private CollectionLogMasterConfig config;

	@Inject
	private TaskAppStateStorage taskAppStateStorage;

	@Inject
	private TaskListStorage taskListStorage;

	@Inject
	private TaskAppClient taskAppClient;

	@Override
	public void startUp() {
		super.startUp();
		taskAppStateStorage.startUp();
	}

	@Override
	public void shutDown() {
		super.shutDown();
		taskAppStateStorage.shutDown();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e) {
		if (!e.getGroup().equals(CONFIG_GROUP)) {
			return;
		}

		String configKey = e.getKey();
		if (
			configKey.equals(CollectionLogMasterConfig.TASK_APP_USERNAME)
			|| configKey.equals(CollectionLogMasterConfig.TASK_APP_PASSWORD)
		) {
			taskAppClient.invalidateToken();
			taskAppStateStorage.fetch();
			taskListStorage.fetch();
		}
	}

	public Task getActiveTask() {
		String activeTaskId = taskAppStateStorage.get().getActiveTaskId();

		return activeTaskId == null ? null : getTaskById(activeTaskId);
	}

	// we might want to build a cache map in the future
	public Task getTaskById(String taskId) {
		for (TaskTier t : TaskTier.values()) {
			List<Task> tasks = getTierTasks(t, true);
			for (Task task : tasks) {
				if (task.getId().equals(taskId)) {
					return task;
				}
			}
		}

		return null;
	}

	public @NonNull TaskTier getCurrentTier() {
		Map<TaskTier, Float> progress = getProgress();

		return getVisibleTiers().stream()
				.filter(t -> progress.get(t) < 1)
				.findFirst()
				.orElse(TaskTier.MASTER);
	}

	public List<Task> getTierTasks() {
		return getTierTasks(getCurrentTier());
	}

	public List<Task> getTierTasks(TaskTier tier) {
		return getTierTasks(tier, false);
	}

	public List<Task> getTierTasks(TaskTier tier, boolean skipLMSCheck) {
		List<Task> tierTasks = taskListStorage.get().getForTier(tier);

		if (!skipLMSCheck && !taskAppStateStorage.get().isLmsEnabled()) {
			return filterTag(tierTasks, Tag.LMS);
		}

		return tierTasks;
	}

	public List<Task> getIncompleteTierTasks() {
		return getIncompleteTierTasks(getCurrentTier());
	}

	public List<Task> getIncompleteTierTasks(TaskTier tier) {
		List<Task> tierTasks = getTierTasks(tier);

		return tierTasks.stream()
				.filter(t -> !isComplete(t.getId()))
				.collect(Collectors.toList());
	}

	public List<TaskTier> getVisibleTiers() {
		TaskTier hideBelow = config.hideBelow();

		return Arrays.stream(TaskTier.values())
				.filter(t -> t.ordinal() >= hideBelow.ordinal())
				.collect(Collectors.toList());
	}

	public @NonNull Map<TaskTier, Float> getProgress() {
		TaskAppState data = taskAppStateStorage.get();
		Set<String> completedTasks = data.getCompletedTasks();

		Map<TaskTier, Float> completionPercentages = new HashMap<>();
		for (TaskTier tier : TaskTier.values()) {
			Set<String> tierTasks = getTierTasks(tier).stream()
					.map(Task::getId)
					.collect(Collectors.toSet());

			float totalTierTasks = tierTasks.size();
			tierTasks.retainAll(completedTasks);

			float tierPercentage = tierTasks.size() / totalTierTasks;

			completionPercentages.put(tier, tierPercentage);
		}

		return completionPercentages;
	}

	public CompletableFuture<Task> generate() {
		return taskAppClient.generateTask()
			.thenCompose((res) -> taskAppStateStorage.fetch())
			.thenApply((v) -> getActiveTask());
	}

	public CompletableFuture<Void> complete() {
		return complete(taskAppStateStorage.get().getActiveTaskId());
	}

	public CompletableFuture<Void> complete(String taskId) {
		return taskAppClient.updateTask(taskId, true)
			.thenCompose((res) -> taskAppStateStorage.fetch());
	}

	public CompletableFuture<Void> uncomplete(String taskId) {
		return taskAppClient.updateTask(taskId, false)
			.thenCompose((res) -> taskAppStateStorage.fetch());
	}

	public CompletableFuture<Boolean> toggleComplete(String taskId) {
		if (isComplete(taskId)) {
			return uncomplete(taskId)
				.thenApply(v -> false);
		} else {
			return complete(taskId)
				.thenApply(v -> true);
		}
	}

	public boolean isComplete(String taskId) {
		Set<String> completedTasks = taskAppStateStorage.get().getCompletedTasks();

		return completedTasks.contains(taskId);
	}

	private List<Task> filterTag(List<Task> list, Tag tag) {
		return list.stream()
				.filter(t -> !t.getTags().contains(tag))
				.collect(Collectors.toList());
	}
}

package com.collectionlogmaster.task;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.command.TaskmanCommandManager;
import com.collectionlogmaster.domain.Tag;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.domain.savedata.SaveData;
import com.collectionlogmaster.domain.verification.clog.CollectionLogVerification;
import com.collectionlogmaster.util.EventBusSubscriber;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Range;

@Singleton
@Slf4j
public class TaskService extends EventBusSubscriber {
	@Inject
	private CollectionLogMasterConfig config;

	@Inject
	private SaveDataStorage saveDataStorage;

	@Inject
	private TaskListStorage taskListStorage;

	@Inject
	private TaskmanCommandManager taskmanCommandManager;

	@Override
	public void startUp() {
		super.startUp();
		saveDataStorage.startUp();
	}

	@Override
	public void shutDown() {
		super.shutDown();
		saveDataStorage.shutDown();
	}

	public Task getActiveTask() {
		String activeTaskId = saveDataStorage.get().getActiveTaskId();

		return activeTaskId == null ? null : getTaskById(activeTaskId);
	}

	// we might want to build a cache map in the future
	public Task getTaskById(String taskId) {
		for (TaskTier t : TaskTier.values()) {
			List<Task> tasks = getTierTasks(t);
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
		List<Task> tierTasks = taskListStorage.get().getForTier(tier);

		if (!config.isLMSEnabled()) {
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

	public @NonNull Map<TaskTier, @Range(from = 0, to = 1) Float> getProgress() {
		SaveData data = saveDataStorage.get();
		Set<String> completedTasks = data.getCompletedTasks();

		Map<TaskTier, @Range(from = 0, to = 1) Float> completionPercentages = new HashMap<>();
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

	public Task generate() {
		SaveData data = saveDataStorage.get();

		String activeTaskId = data.getActiveTaskId();
		if (activeTaskId != null) {
			log.warn("Tried to generate task when previous one wasn't completed yet");
			return null;
		}

		List<Task> incompleteTierTasks = getIncompleteTierTasks();
		if (incompleteTierTasks.isEmpty()) {
			log.warn("No tasks left");
			return null;
		}

		Task generatedTask = pickRandomTask(incompleteTierTasks);
		log.debug("New task generated: {}", generatedTask);

		data.setActiveTaskId(generatedTask.getId());
		saveDataStorage.save();
		taskmanCommandManager.updateServer();

		return generatedTask;
	}

	public void complete() {
		Task activeTask = getActiveTask();
		if (activeTask == null) {
			return;
		}

		complete(activeTask.getId());
	}

	public void complete(String taskId) {
		SaveData data = saveDataStorage.get();
		Set<String> completedTasks = data.getCompletedTasks();
		completedTasks.add(taskId);

		if (taskId.equals(data.getActiveTaskId())) {
			data.setActiveTaskId(null);
		}

		saveDataStorage.save();
		taskmanCommandManager.updateServer();
	}

	public void uncomplete(String taskId) {
		Set<String> completedTasks = saveDataStorage.get().getCompletedTasks();
		completedTasks.remove(taskId);

		saveDataStorage.save();
		taskmanCommandManager.updateServer();
	}

	public boolean toggleComplete(String taskId) {
		if (isComplete(taskId)) {
			uncomplete(taskId);
			return false;
		} else {
			complete(taskId);
			return true;
		}
	}

	public boolean isComplete(String taskId) {
		Set<String> completedTasks = saveDataStorage.get().getCompletedTasks();

		return completedTasks.contains(taskId);
	}

	private Task pickRandomTask(List<Task> tasks) {
		int index = (int) Math.floor(Math.random() * tasks.size());
		Task pickedTask = tasks.get(index);

		if (!(pickedTask.getVerification() instanceof CollectionLogVerification)) {
			return pickedTask;
		}

		// get first of similarly named tasks
		String taskName = pickedTask.getName();
		Stream<Task> similarTasks = tasks.stream()
				.filter(t -> taskName.equals(t.getName()))
				.filter(t -> t.getVerification() instanceof CollectionLogVerification);

		//noinspection DataFlowIssue
		return similarTasks.min(Comparator.comparingInt(
				t -> ((CollectionLogVerification) t.getVerification()).getCount()
		)).orElse(pickedTask);
	}

	private List<Task> filterTag(List<Task> list, Tag tag) {
		return list.stream()
				.filter(t -> !t.getTags().contains(tag))
				.collect(Collectors.toList());
	}
}

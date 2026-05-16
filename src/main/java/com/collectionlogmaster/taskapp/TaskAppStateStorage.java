package com.collectionlogmaster.taskapp;

import com.collectionlogmaster.command.TaskmanCommandManager;
import com.collectionlogmaster.taskapp.domain.CompletedTask;
import com.collectionlogmaster.util.EventBusSubscriber;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class TaskAppStateStorage extends EventBusSubscriber {
	@Inject
	private TaskAppClient taskAppClient;

	@Inject
	private TaskmanCommandManager taskmanCommandManager;

	private TaskAppState data = new TaskAppState();

	@Override
	public void startUp() {
		super.startUp();
		fetch();
	}

	public TaskAppState get() {
		return data;
	}

	public CompletableFuture<Void> fetch() {
		int hashBefore = data.hashCode();
		return taskAppClient.getUserProfile()
			.thenAccept(res -> {
				data.setActiveTaskId(res.getActiveTaskId());
				data.setOfficial(res.isOfficial());
				data.setLmsEnabled(res.isLmsEnabled());

				Set<String> newCompletedTasks = res.getCompletedTasks().stream()
					.map(CompletedTask::getId)
					.collect(Collectors.toSet());
				data.setCompletedTasks(newCompletedTasks);

				if (data.hashCode() != hashBefore) {
					taskmanCommandManager.updateServer();
				}
			});
	}
}

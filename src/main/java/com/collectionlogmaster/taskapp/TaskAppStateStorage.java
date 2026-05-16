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

	private volatile TaskAppState state = new TaskAppState();

	@Override
	public void startUp() {
		super.startUp();
		fetch();
	}

	public TaskAppState get() {
		return state;
	}

	public CompletableFuture<Void> fetch() {
		return taskAppClient.getUserProfile()
			.thenAccept(res -> {
				Set<String> completedTasks = res.getCompletedTasks().stream()
					.map(CompletedTask::getId)
					.collect(Collectors.toUnmodifiableSet());

				TaskAppState newState = new TaskAppState(
					res.getActiveTaskId(),
					res.isOfficial(),
					res.isLmsEnabled(),
					completedTasks
				);

				if (state.equals(newState)) {
					return;
				}

				state = newState;
				taskmanCommandManager.updateServer();
			});
	}
}

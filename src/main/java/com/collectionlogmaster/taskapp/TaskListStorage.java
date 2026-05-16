package com.collectionlogmaster.taskapp;

import com.collectionlogmaster.domain.TieredTaskList;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Singleton
public class TaskListStorage {
	private final TaskAppClient taskAppClient;

	private @NonNull TieredTaskList taskList = new TieredTaskList();

	@Inject
	public TaskListStorage(TaskAppClient taskAppClient) {
		this.taskAppClient = taskAppClient;
		fetch();
	}

	public @NonNull TieredTaskList get() {
		return taskList;
	}

	public void fetch() {
		taskAppClient.getTaskList()
				.thenAccept(taskList -> this.taskList = taskList);
	}
}

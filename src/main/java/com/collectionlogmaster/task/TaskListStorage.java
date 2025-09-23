package com.collectionlogmaster.task;

import com.collectionlogmaster.domain.TieredTaskList;
import com.collectionlogmaster.util.FileUtils;
import com.collectionlogmaster.util.HttpClient;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Singleton
public class TaskListStorage {
	private static final String LOCAL_TASK_LIST_FILE = "task-list.json";

	private static final String REMOTE_TASK_LIST_URL = "https://raw.githubusercontent.com/OSRS-Taskman/collection-log-master/refs/heads/main/src/main/resources/com/collectionlogmaster/task-list.json";

	private final HttpClient httpClient;

	private @NonNull TieredTaskList taskList = new TieredTaskList();

	@Inject
	public TaskListStorage(HttpClient httpClient) {
		this.httpClient = httpClient;
		loadAsync();
	}

	public @NonNull TieredTaskList get() {
		return taskList;
	}

	private void loadAsync() {
		fetchRemoteAsync()
				.exceptionally(t -> fetchLocal())
				.thenAccept(taskList -> this.taskList = taskList);
	}

	private @NonNull TieredTaskList fetchLocal() {
		return FileUtils.loadResource(LOCAL_TASK_LIST_FILE, TieredTaskList.class);
	}

	private CompletableFuture<TieredTaskList> fetchRemoteAsync() {
		return httpClient.getHttpRequestAsync(REMOTE_TASK_LIST_URL, TieredTaskList.class);
	}
}

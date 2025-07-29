package com.logmaster.task;

import com.logmaster.domain.TieredTaskList;
import com.logmaster.util.FileUtils;
import com.logmaster.util.HttpClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Singleton
public class TaskListStorage {
    private static final String LOCAL_TASK_LIST_FILE = "task-list.json";

    private static final String REMOTE_TASK_LIST_URL = "https://raw.githubusercontent.com/OSRS-Taskman/generate-task/refs/heads/main/src/main/resources/com/logmaster/domain/task-list.json";

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
        return FileUtils.loadDefinitionResource(TieredTaskList.class, LOCAL_TASK_LIST_FILE);
    }

    private CompletableFuture<TieredTaskList> fetchRemoteAsync() {
        return httpClient.getHttpRequestAsync(REMOTE_TASK_LIST_URL, TieredTaskList.class);
    }
}

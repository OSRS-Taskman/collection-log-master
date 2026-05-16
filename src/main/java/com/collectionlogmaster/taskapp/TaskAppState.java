package com.collectionlogmaster.taskapp;

import lombok.Data;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TaskAppState {
    private final String activeTaskId;
    private final boolean isOfficial;
    private final boolean isLmsEnabled;
    private final Set<String> completedTasks;

    public TaskAppState() {
        this(null, true, true, Set.of());
    }
}

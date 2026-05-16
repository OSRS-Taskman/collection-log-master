package com.collectionlogmaster.taskapp;

import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
public class TaskAppState {
    private String activeTaskId = null;
    private boolean isOfficial = false;
    private boolean isLmsEnabled = false;
    private Set<String> completedTasks = new HashSet<>();
}

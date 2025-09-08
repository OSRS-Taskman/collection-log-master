package com.collectionlogmaster.domain.savedata;

import com.google.gson.reflect.TypeToken;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.domain.savedata.v0.V0SaveData;
import com.collectionlogmaster.domain.savedata.v0.V0Task;
import com.collectionlogmaster.domain.savedata.v0.V0TaskPointer;
import com.collectionlogmaster.domain.savedata.v1.V1SaveData;
import com.collectionlogmaster.domain.savedata.v1.V1TaskPointer;
import com.collectionlogmaster.domain.savedata.v2.V2SaveData;
import com.collectionlogmaster.task.SaveDataStorage;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.util.FileUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.collectionlogmaster.util.GsonOverride.GSON;

@Singleton
@Slf4j
@SuppressWarnings("deprecation")
public class SaveDataUpdater {
    @Inject
    private SaveDataStorage saveDataStorage;

    @Inject
    private TaskService taskService;

    public SaveData update(String json) {
        BaseSaveData base = GSON.fromJson(json, BaseSaveData.class);
        if (base == null) {
            return new SaveData();
        }

        if (base.getVersion() == V0SaveData.VERSION) {
            V0SaveData v0Save = GSON.fromJson(json, V0SaveData.class);
            return update(update(update(v0Save)));
        }

        if (base.getVersion() == V1SaveData.VERSION) {
            V1SaveData v1Save = GSON.fromJson(json, V1SaveData.class);
            return update(update(v1Save));
        }

        if (base.getVersion() == V2SaveData.VERSION) {
            V2SaveData v2Save = GSON.fromJson(json, V2SaveData.class);
            return update(v2Save);
        }

        if (base.getVersion() == SaveData.VERSION) {
            return GSON.fromJson(json, SaveData.class);
        }

        log.warn("Could not figure out save data version for json {}", json);
        return new SaveData();
    }

    private SaveData update(V2SaveData v2Save) {
        saveDataStorage.saveBackup(v2Save);
        SaveData newSave = new SaveData();

        newSave.getCompletedTasks().addAll(v2Save.getCompletedTasks());

        Task activeTask = v2Save.getActiveTask();
        if (activeTask != null) {
            newSave.setActiveTaskId(activeTask.getId());
        }

        return newSave;
    }

    private V2SaveData update(V1SaveData v1Save) {
        saveDataStorage.saveBackup(v1Save);
        V2SaveData newSave = new V2SaveData();

        V1TaskPointer v1ActiveTaskPointer = v1Save.getActiveTaskPointer();
        if (v1ActiveTaskPointer != null) {
            newSave.setActiveTask(v1ActiveTaskPointer.getTask());
        }

        Set<String> newCompletedTasks = newSave.getCompletedTasks();
        Set<String> v1CompletedTasks = v1Save.getProgress().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());

        newCompletedTasks.addAll(v1CompletedTasks);

        return newSave;
    }

    private V1SaveData update(V0SaveData v0Save) {
        saveDataStorage.saveBackup(v0Save);
        V1SaveData newSave = new V1SaveData();

        Type mapType = new TypeToken<Map<TaskTier, Map<Integer, String>>>() {}.getType();
        Map<TaskTier, Map<Integer, String>> v0MigrationData =
                FileUtils.loadResource("domain/savedata/v0-migration.json", mapType);;

        Map<TaskTier, Set<Integer>> v0Progress = v0Save.getProgress();
        Map<TaskTier, Set<String>> newProgress = newSave.getProgress();

        for (TaskTier tier : TaskTier.values()) {
            Set<Integer> v0TierData = v0Progress.get(tier);
            Set<String> newTierData = newProgress.get(tier);
            Map<Integer, String> tierMigrationData = v0MigrationData.get(tier);

            for (Integer v0TaskId : v0TierData) {
                if (tierMigrationData.containsKey(v0TaskId)) {
                    newTierData.add(tierMigrationData.get(v0TaskId));
                }
            }
        }

        V0TaskPointer v0TaskPointer = v0Save.getActiveTaskPointer();
        if (v0TaskPointer != null) {
            V0Task v0Task = v0TaskPointer.getTask();
            String newTaskId = v0MigrationData.get(v0TaskPointer.getTaskTier()).get(v0Task.getId());
            Task newTask = taskService.getTaskById(newTaskId);

            // if we can't find the task, don't set it to avoid problems
            if (newTask != null) {
                newSave.setActiveTaskPointer(new V1TaskPointer(v0TaskPointer.getTaskTier(), newTask));
            }
        }

        return newSave;
    }
}

package com.logmaster.domain.savedata;

import com.google.gson.reflect.TypeToken;
import com.logmaster.domain.Task;
import com.logmaster.domain.TaskTier;
import com.logmaster.domain.savedata.v0.V0SaveData;
import com.logmaster.domain.savedata.v0.V0Task;
import com.logmaster.domain.savedata.v0.V0TaskPointer;
import com.logmaster.domain.savedata.v1.V1SaveData;
import com.logmaster.domain.savedata.v1.V1TaskPointer;
import com.logmaster.task.SaveDataStorage;
import com.logmaster.util.FileUtils;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.logmaster.util.GsonOverride.GSON;

@Singleton
@Slf4j
@SuppressWarnings("deprecation")
public class SaveDataUpdater {
    @Inject
    private SaveDataStorage saveDataStorage;

    public SaveData update(String json) {
        BaseSaveData base = GSON.fromJson(json, BaseSaveData.class);
        if (base == null) {
            return new SaveData();
        }

        if (base.getVersion() == V0SaveData.VERSION) {
            V0SaveData v0Save = GSON.fromJson(json, V0SaveData.class);
            return update(update(v0Save));
        }

        if (base.getVersion() == V1SaveData.VERSION) {
            V1SaveData v1Save = GSON.fromJson(json, V1SaveData.class);
            return update(v1Save);
        }

        return GSON.fromJson(json, SaveData.class);
    }

    private SaveData update(V1SaveData v1Save) {
        saveDataStorage.saveBackup(v1Save);
        SaveData newSave = new SaveData();

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
            Task newTask = new Task(newTaskId, v0Task.getDescription(), v0Task.getItemID(), null);
            newSave.setActiveTaskPointer(new V1TaskPointer(v0TaskPointer.getTaskTier(), newTask));
        }

        return newSave;
    }
}

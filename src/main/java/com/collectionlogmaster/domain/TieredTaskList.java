package com.collectionlogmaster.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class TieredTaskList {
    private List<Task> easy = new ArrayList<>();
    private List<Task> medium = new ArrayList<>();
    private List<Task> hard = new ArrayList<>();
    private List<Task> elite = new ArrayList<>();
    private List<Task> master = new ArrayList<>();

    public List<Task> getForTier(TaskTier tier) {
        switch (tier) {
            case EASY: return easy;
            case MEDIUM: return medium;
            case HARD: return hard;
            case ELITE: return elite;
            case MASTER: return master;
            default: return Collections.emptyList();
        }
    }
}

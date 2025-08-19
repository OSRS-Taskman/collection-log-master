package com.collectionlogmaster.domain.command;

import lombok.Data;

@Data
public class CommandResponse {
    private CommandTask task;
    private String tier;
    private int progressPercentage;
}

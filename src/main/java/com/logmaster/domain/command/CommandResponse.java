package com.logmaster.domain.command;

import lombok.Data;

@Data
public class CommandResponse {
    private CommandTask task;
    private String tier;
    private int progressPercentage;
}

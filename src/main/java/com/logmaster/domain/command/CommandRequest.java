package com.logmaster.domain.command;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CommandRequest {
    private final String taskId;
    private final String tier;
    private final int progressPercentage;
}

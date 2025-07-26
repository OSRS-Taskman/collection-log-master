package com.logmaster.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TaskPointer {
    private TaskTier taskTier;
    private Task task;
}

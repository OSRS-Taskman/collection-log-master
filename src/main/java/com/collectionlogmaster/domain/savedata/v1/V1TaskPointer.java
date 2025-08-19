package com.collectionlogmaster.domain.savedata.v1;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class V1TaskPointer {
    private TaskTier taskTier;
    private Task task;
}

package com.collectionlogmaster.domain.savedata.v0;

import com.collectionlogmaster.domain.TaskTier;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Deprecated
public class V0TaskPointer {

    private TaskTier taskTier;
    private V0Task task;
}

package com.collectionlogmaster.synchronization;

import com.collectionlogmaster.domain.Task;
import lombok.NonNull;

public interface Verifier {
    boolean supports(@NonNull Task task);
    boolean verify(@NonNull Task task);
}

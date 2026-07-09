package com.collectionlogmaster.synchronization;

import com.collectionlogmaster.domain.Task;
import lombok.NonNull;

public interface Verifier<T> {
	boolean supports(@NonNull Task task);
	boolean verify(@NonNull Task task);
	T verificationData();
}

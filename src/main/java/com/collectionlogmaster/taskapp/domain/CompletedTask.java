package com.collectionlogmaster.taskapp.domain;

import java.time.Instant;
import java.util.Set;
import lombok.Data;

@Data
public class CompletedTask {
	private final String id;
	private final Set<Integer> completedItemIds;
	// TODO: handle the dumb way we serialize dates in taskapp
//	private final Instant assignedDate;
//	private final Instant completedDate;
}

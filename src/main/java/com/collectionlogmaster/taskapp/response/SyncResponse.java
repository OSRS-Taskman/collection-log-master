package com.collectionlogmaster.taskapp.response;

import java.util.Set;
import lombok.Data;

@Data
public class SyncResponse {
	private final Set<String> completed;
	private final Set<String> uncompleted;
}

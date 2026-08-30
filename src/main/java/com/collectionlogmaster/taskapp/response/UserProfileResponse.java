package com.collectionlogmaster.taskapp.response;

import com.collectionlogmaster.taskapp.domain.CompletedTask;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class UserProfileResponse {
	private final String username;
	private final boolean isOfficial;
	private final boolean isLmsEnabled;
	@Accessors(fluent = true)
	private final boolean hasMigrated;
	private final String activeTaskId;
	private final List<CompletedTask> completedTasks;
}

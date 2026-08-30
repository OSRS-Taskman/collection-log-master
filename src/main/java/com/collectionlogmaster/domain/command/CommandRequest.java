package com.collectionlogmaster.domain.command;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CommandRequest {
	@SerializedName("taskId")
	private final String taskId;
	private final String tier;
	@SerializedName("progressPercentage")
	private final int progressPercentage;
}

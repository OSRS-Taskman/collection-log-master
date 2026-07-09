package com.collectionlogmaster.domain.command;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CommandResponse {
	private CommandTask task;
	private String tier;
	@SerializedName("progressPercentage")
	private int progressPercentage;
}

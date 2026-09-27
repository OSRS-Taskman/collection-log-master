package com.collectionlogmaster.domain.command;

import lombok.Data;

@Data
public class CommandResponse {
	private String taskId;
	private String tier;
	private int progress;
}

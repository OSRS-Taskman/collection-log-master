package com.collectionlogmaster.domain.command;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import lombok.Data;

@Data
public class CommandResponse {
	private ArrayList<String> task;
	private String tier;
	@SerializedName("progressPercentage")
	private int progressPercentage;
}

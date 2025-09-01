package com.collectionlogmaster.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class TieredTaskList {
	private List<Task> easy = new ArrayList<>();
	private List<Task> medium = new ArrayList<>();
	private List<Task> hard = new ArrayList<>();
	private List<Task> elite = new ArrayList<>();
	private List<Task> master = new ArrayList<>();

	public List<Task> all() {
		return Stream.of(easy, medium, hard, elite, master)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public List<Task> getForTier(TaskTier tier) {
		switch (tier) {
			case EASY: return easy;
			case MEDIUM: return medium;
			case HARD: return hard;
			case ELITE: return elite;
			case MASTER: return master;
			default: return Collections.emptyList();
		}
	}
}

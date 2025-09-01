package com.collectionlogmaster.ui.state;

import com.collectionlogmaster.domain.Task;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.NonNull;
import net.runelite.client.eventbus.EventBus;

@Singleton
public class StateStore {
	@Inject
	private EventBus eventBus;

	@Getter
	private boolean dashboardEnabled = false;

	@Getter
	private Task activeTask = null;

	Set<@NonNull String> completedTaskIds = new HashSet<>();

	// TODO: maybe add some sort of key to event so subscribers
	//  can more selectively decide how/when to react
	private void postEvent() {
		eventBus.post(new StateChanged());
	}

	public void setDashboardEnabled(boolean enabled) {
		dashboardEnabled = enabled;
		postEvent();
	}

	public void setActiveTask(Task task) {
		activeTask = task;
		postEvent();
	}

	public Set<String> getCompletedTaskIds() {
		return Collections.unmodifiableSet(completedTaskIds);
	}

	public void setCompletedTaskIds(Set<@NonNull String> taskIds) {
		completedTaskIds = new HashSet<>(taskIds);
		postEvent();
	}

	public boolean addCompletedTaskId(String taskId) {
		boolean wasAdded = completedTaskIds.add(taskId);
		postEvent();

		return wasAdded;
	}

	public boolean removeCompletedTaskId(String taskId) {
		boolean wasRemoved = completedTaskIds.remove(taskId);
		postEvent();

		return wasRemoved;
	}
}

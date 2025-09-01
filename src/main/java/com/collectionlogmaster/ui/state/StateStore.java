package com.collectionlogmaster.ui.state;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.client.eventbus.EventBus;

@Singleton
public class StateStore {
	@Inject
	private EventBus eventBus;

	@Getter
	private boolean dashboardEnabled = false;

	// TODO: maybe add some sort of key to event so subscribers
	//  can more selectively decide how/when to react
	private void postEvent() {
		eventBus.post(new StateChanged());
	}

	public void setDashboardEnabled(boolean enabled) {
		dashboardEnabled = enabled;
		postEvent();
	}
}

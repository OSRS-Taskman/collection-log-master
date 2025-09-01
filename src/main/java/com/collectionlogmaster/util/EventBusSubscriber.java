package com.collectionlogmaster.util;

import javax.inject.Inject;
import net.runelite.client.eventbus.EventBus;

public abstract class EventBusSubscriber {
	@Inject
	protected EventBus eventBus = null;

	public void startUp() {
		eventBus.register(this);
	};

	public void shutDown() {
		eventBus.unregister(this);
	}
}

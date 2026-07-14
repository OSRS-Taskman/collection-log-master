package com.collectionlogmaster.util;

import com.google.inject.Inject;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import lombok.experimental.Accessors;

public class SimpleDebouncer {
	private Future<?> future;

	@Inject
	private ScheduledExecutorService executorService;

	@Setter
	@Accessors(chain = true)
	private int delay = 500;

	public synchronized void debounce(Runnable cb) {
		if (future != null) {
			future.cancel(false);
			future = null;
		}

		future = executorService.schedule(cb, delay, TimeUnit.MILLISECONDS);
	}
}

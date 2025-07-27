package com.logmaster.util;

import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleDebouncer {
    public interface Callback {
        void call();
    }

    Future<?> future;

    @Inject
    private ScheduledExecutorService executorService;

    private final static int MS_DELAY = 500;

    public synchronized void debounce(Callback cb) {
        if (future != null) {
            future.cancel(false);
            future = null;
        }

        future = executorService.schedule(cb::call, MS_DELAY, TimeUnit.MILLISECONDS);
    }
}

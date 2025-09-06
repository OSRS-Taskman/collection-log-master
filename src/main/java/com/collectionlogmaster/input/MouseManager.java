/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.collectionlogmaster.input;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.input.MouseWheelListener;

/**
 * We implement our own version of {@link net.runelite.client.input.MouseListener MouseManager}
 * mostly for the two following reasons:
 * <ol>
 * 	<li>It helps us keep track of any lingering listeners registered in our UI classes, allowing
 *   us to more easily identify instances where we forgot to properly unregisters. It also makes
 *   sure that, in the worst case scenario, listeners are unregistered once the plugin is disabled.
 * 	<li>It lets us run listeners in the reverse order they were registered. This is important so
 * 	 that when a new interface is open in front of another, the newest one has priority in its
 * 	 event listeners. See {@link #checkMaxListeners} for more details.
 * </ol>
 */
@Slf4j
@Singleton
public class MouseManager implements net.runelite.client.input.MouseListener, MouseWheelListener {
	public static final int MAX_LISTENERS = 3;

	@Inject
	private net.runelite.client.input.MouseManager mouseManager;

	@Inject
	@Named("developerMode")
	private boolean isDeveloperMode;

	private final CopyOnWriteArrayList<MouseListener> mouseListeners = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<MouseWheelListener> mouseWheelListeners = new CopyOnWriteArrayList<>();

	public void startUp() {
		mouseManager.registerMouseListener(this);
		mouseManager.registerMouseWheelListener(this);
	}

	public void shutDown() {
		mouseManager.unregisterMouseListener(this);
		mouseManager.unregisterMouseWheelListener(this);
	}

	/**
	 * Checks whether the maximum amount of listeners the plugin should register was exceeded. This
	 * amount is defined by the UI hierarchy. When more listeners are registered, it logs a warning.
	 * If in developer mode, it also throws an exception and refuses to register the listener.
	 */
	private void checkMaxListeners(List<?> listeners) {
		if (listeners.size() == MAX_LISTENERS) {
			RuntimeException ex = new RuntimeException("Attempting to register more listeners than expected.");
			if (isDeveloperMode) {
				throw ex;
			}

			log.warn("", ex);
		}
	}

	private <Listener, Event extends InputEvent> Event forwardToListeners(
		List<Listener> listeners,
		Event event,
		BiFunction<Listener, Event, Event> eventMethod
	) {
		// we loop in reverse order so handlers added later take precedence
		var iterator = listeners.listIterator(listeners.size());
		while (iterator.hasPrevious()) {
			Listener listener = iterator.previous();
			event = eventMethod.apply(listener, event);
			if (event.isConsumed()) {
				break;
			}
		}

		return event;
	}

	public void registerMouseListener(MouseListener mouseListener) {
		checkMaxListeners(mouseListeners);
		mouseListeners.addIfAbsent(mouseListener);
	}

	public void unregisterMouseListener(MouseListener mouseListener) {
		mouseListeners.remove(mouseListener);
	}

	public void registerMouseWheelListener(MouseWheelListener mouseWheelListener) {
		checkMaxListeners(mouseWheelListeners);
		mouseWheelListeners.addIfAbsent(mouseWheelListener);
	}

	public void unregisterMouseWheelListener(MouseWheelListener mouseWheelListener) {
		mouseWheelListeners.remove(mouseWheelListener);
	}

	public MouseEvent mousePressed(MouseEvent mouseEvent) {
		return forwardToListeners(mouseListeners, mouseEvent, MouseListener::mousePressed);
	}

	public MouseEvent mouseReleased(MouseEvent mouseEvent) {
		return forwardToListeners(mouseListeners, mouseEvent, MouseListener::mouseReleased);
	}

	public MouseEvent mouseClicked(MouseEvent mouseEvent) {
		return forwardToListeners(mouseListeners, mouseEvent, MouseListener::mouseClicked);
	}

	public MouseEvent mouseEntered(MouseEvent mouseEvent) {
		return forwardToListeners(mouseListeners, mouseEvent, MouseListener::mouseEntered);
	}

	public MouseEvent mouseExited(MouseEvent mouseEvent) {
		return forwardToListeners(mouseListeners, mouseEvent, MouseListener::mouseExited);
	}

	public MouseEvent mouseDragged(MouseEvent mouseEvent) {
		return forwardToListeners(mouseListeners, mouseEvent, MouseListener::mouseDragged);
	}

	public MouseEvent mouseMoved(MouseEvent mouseEvent) {
		return forwardToListeners(mouseListeners, mouseEvent, MouseListener::mouseMoved);
	}

	public MouseWheelEvent mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
		return forwardToListeners(mouseWheelListeners, mouseWheelEvent, MouseWheelListener::mouseWheelMoved);
	}
}

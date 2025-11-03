package com.collectionlogmaster.input;

import java.awt.event.MouseEvent;

/**
 * Extended {@link net.runelite.client.input.MouseListener} interface to provide a default
 * implementation of all handlers so implementers aren't forced to write boilerplate code for
 * the events it doesn't care about.
 */
public interface MouseListener extends net.runelite.client.input.MouseListener {
	default MouseEvent mouseClicked(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	default MouseEvent mousePressed(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	default MouseEvent mouseReleased(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	default MouseEvent mouseEntered(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	default MouseEvent mouseExited(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	default MouseEvent mouseDragged(MouseEvent mouseEvent) {
		return mouseEvent;
	}

	default MouseEvent mouseMoved(MouseEvent mouseEvent) {
		return mouseEvent;
	}
}

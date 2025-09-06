package com.collectionlogmaster.ui.generic;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.input.MouseManager;
import com.google.inject.Inject;
import java.awt.event.MouseWheelEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.MouseWheelListener;

@Accessors(chain = true)
public class UIScrollableContainer extends UIComponent<UIScrollableContainer> implements MouseWheelListener {
	private static final int SCROLLBAR_SENSITIVITY_MULTIPLIER = 4;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MouseManager mouseManager;

	@Getter
	private final Widget content;
	private final UIScrollBar scrollBar;

	@Setter
	private int scrollSensitivity = 4;

	/**
	 * Only supported when scrollAxis is VERTICAL
	 */
	@Setter
	private boolean drawScrollbar = false;

	public static UIScrollableContainer createInside(Widget window) {
		return new UIScrollableContainer(window.createChild(WidgetType.LAYER));
	}

	public UIScrollableContainer(Widget widget) {
		super(widget);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

		mouseManager.registerMouseWheelListener(this);

		content = widget.createChild(WidgetType.LAYER);
		scrollBar = UIScrollBar.createInside(widget, widget, content);

		initializeWidgets();
	}

	public UIScrollableContainer setScrollBuffer(int scrollBuffer) {
		scrollBar.setScrollBuffer(scrollBuffer);
		return this;
	}

	public UIScrollableContainer setScrollAxis(ScrollAxis scrollAxis) {
		scrollBar.setScrollAxis(scrollAxis);
		return this;
	}

	@Override
	public MouseWheelEvent mouseWheelMoved(MouseWheelEvent e) {
		if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
			return e;
		}
		if (!widget.getBounds().contains(e.getPoint())) {
			return e;
		}

		e.consume();

		// scroll faster when scrolling on top of scrollbar
		int multiplier = scrollBar.getBounds().contains(e.getPoint()) ? SCROLLBAR_SENSITIVITY_MULTIPLIER : 1;
		int scrollDistance = e.getUnitsToScroll() * scrollSensitivity * multiplier;

		clientThread.invoke(() -> {
			scrollBar.scroll(scrollDistance)
				.revalidate();
		});

		return e;
	}

	private void initializeWidgets() {
		widget.revalidate();

		scrollBar.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setPos(0, 0)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(UIScrollBar.ARROW_SIZE, 0)
			.revalidate();

		content.setPos(0, 0)
			.revalidate();
	}

	@Override
	public void revalidate() {
		widget.revalidate();

		if (scrollBar.getScrollAxis() == ScrollAxis.VERTICAL) {
			int minusWidth = 0;
			if (drawScrollbar) {
				minusWidth = UIScrollBar.ARROW_SIZE;
			}

			content.setWidthMode(WidgetSizeMode.MINUS)
				.setOriginalWidth(minusWidth)
				.revalidate();

			scrollBar.setHidden(!drawScrollbar)
				.revalidate();
		} else if (scrollBar.getScrollAxis() == ScrollAxis.HORIZONTAL) {
			content.setHeightMode(WidgetSizeMode.MINUS)
				.setOriginalHeight(0)
				.revalidate();

			scrollBar.setHidden(true)
				.revalidate();
		}
	}

	@Override
	public void unregister() {
		mouseManager.unregisterMouseWheelListener(this);
		scrollBar.unregister();
	}
}
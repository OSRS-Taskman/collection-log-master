package com.collectionlogmaster.ui.generic;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.google.inject.Inject;
import java.awt.event.MouseWheelEvent;
import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.input.MouseManager;
import net.runelite.client.input.MouseWheelListener;

// TODO: add scroll bar
public class UIScrollableContainer extends UIComponent<UIScrollableContainer> implements MouseWheelListener {
	public enum ScrollAxis {
		VERTICAL,
		HORIZONTAL;
	}

	@Getter
	private final Widget content;

	@Inject
	private MouseManager mouseManager;

	private ScrollAxis scrollAxis = ScrollAxis.VERTICAL;

	private int scrollBuffer = 0;

	private int scrollSensitivity = 4;

	public static UIScrollableContainer createInside(Widget window) {
		return new UIScrollableContainer(window.createChild(WidgetType.LAYER));
	}

	public UIScrollableContainer(Widget widget) {
		super(widget, WidgetType.LAYER);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

		mouseManager.registerMouseWheelListener(this);

		content = widget.createChild(WidgetType.LAYER);

		applyStaticStyles();
	}

	public UIScrollableContainer setScrollAxis(ScrollAxis scrollAxis) {
		this.scrollAxis = scrollAxis;
		return this;
	}

	public UIScrollableContainer setScrollBuffer(int scrollBuffer) {
		this.scrollBuffer = scrollBuffer;
		return this;
	}

	public UIScrollableContainer setScrollSensitivity(int scrollSensitivity) {
		this.scrollSensitivity = scrollSensitivity;
		return this;
	}

	@Override
	public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event) {
		if (event.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) return event;
		if (!widget.getBounds().contains(event.getPoint())) return event;

		int scrollDistance = event.getUnitsToScroll() * scrollSensitivity;
		if (scrollAxis == ScrollAxis.VERTICAL) {
			int scrollY = content.getScrollY() + scrollDistance;
			scrollY = Math.max(0, Math.min(content.getOriginalHeight() - widget.getHeight() + scrollBuffer, scrollY));
			content.setScrollY(scrollY);
		} else if (scrollAxis == ScrollAxis.HORIZONTAL) {
			int scrollX = content.getScrollX() + scrollDistance;
			scrollX = Math.max(0, Math.min(content.getOriginalWidth() - widget.getWidth() + scrollBuffer, scrollX));
			content.setScrollX(scrollX);
		}

		return event;
	}

	private void applyStaticStyles() {
		widget.revalidate();

		content.setPos(0, 0)
				.revalidate();
	}

	@Override
	public void revalidate() {
		widget.revalidate();

		content.revalidate();
	}
}
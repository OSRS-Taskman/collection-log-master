package com.collectionlogmaster.ui.neww;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.google.inject.Inject;
import java.awt.event.MouseWheelEvent;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.RuneLite;
import net.runelite.client.input.MouseManager;
import net.runelite.client.input.MouseWheelListener;
import org.intellij.lang.annotations.MagicConstant;

// TODO: add scroll bar
public class UIScrollableContainer extends UIComponent<UIScrollableContainer> implements MouseWheelListener {
	@Getter
	private Widget content;

	@Inject
	private MouseManager mouseManager;

	private int scrollBuffer = 0;

	public static UIScrollableContainer createInside(Widget window, @MagicConstant(valuesFromClass = WidgetType.class) int contentType) {
		return new UIScrollableContainer(window.createChild(WidgetType.LAYER), contentType);
	}

	public UIScrollableContainer(Widget widget, @MagicConstant(valuesFromClass = WidgetType.class) int contentType) {
		super(widget, WidgetType.LAYER);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

        mouseManager.registerMouseWheelListener(this);

		content = widget.createChild(contentType);

		applyStaticStyles();
	}

	public UIScrollableContainer setScrollBuffer(int scrollBuffer) {
		this.scrollBuffer = scrollBuffer;
		return this;
	}

	@Override
	public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event) {
		if (event.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) return event;
		if (!widget.getBounds().contains(event.getPoint())) return event;

		int scrollY = content.getScrollY() + event.getUnitsToScroll() * 4;
		scrollY = Math.max(0, Math.min(content.getOriginalHeight() - content.getHeight() + scrollBuffer, scrollY));
		content.setScrollY(scrollY);

		return event;
	}

	private void applyStaticStyles() {
		widget.revalidate();

		content.setPos(0, 0)
				.setWidthMode(WidgetSizeMode.MINUS)
				.setHeightMode(WidgetSizeMode.MINUS)
				.setSize(0, 0)
				.revalidate();
	}

	@Override
	public void revalidate() {
		widget.revalidate();

		content.revalidate();
	}
}
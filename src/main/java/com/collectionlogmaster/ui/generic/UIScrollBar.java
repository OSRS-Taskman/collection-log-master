package com.collectionlogmaster.ui.generic;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.input.MouseListener;
import com.collectionlogmaster.input.MouseManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import org.jetbrains.annotations.Range;

@Accessors(chain = true)
public class UIScrollBar extends UIComponent<UIScrollBar> implements MouseListener {
	public static final int ARROW_SIZE = 16;
	private static final int THUMB_EDGE_HEIGHT = 5;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MouseManager mouseManager;

	private final Widget tracker;
	private final Widget thumbContainer;
	private final Widget thumbMiddle;
	private final Widget thumbTop;
	private final Widget thumbBottom;
	private final Widget upArrow;
	private final Widget downArrow;

	private final Widget content;
	private final Widget container;

	@Getter
	@Setter
	private ScrollAxis scrollAxis = ScrollAxis.VERTICAL;

	@Setter
	private int scrollBuffer = 0;

	private Point thumbDragLast = null;

	public static UIScrollBar createInside(Widget window, Widget container, Widget content) {
		return new UIScrollBar(window.createChild(WidgetType.LAYER), container, content);
	}

	public UIScrollBar(Widget widget, Widget container, Widget content) {
		super(widget);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

		mouseManager.registerMouseListener(this);

		tracker = widget.createChild(WidgetType.GRAPHIC);
		thumbContainer = widget.createChild(WidgetType.LAYER);
		thumbMiddle = thumbContainer.createChild(WidgetType.GRAPHIC);
		thumbTop = thumbContainer.createChild(WidgetType.GRAPHIC);
		thumbBottom = thumbContainer.createChild(WidgetType.GRAPHIC);
		upArrow = widget.createChild(WidgetType.GRAPHIC);
		downArrow = widget.createChild(WidgetType.GRAPHIC);
		this.container = container;
		this.content = content;

		initializeWidgets();
	}

	@Override
	public MouseEvent mouseClicked(MouseEvent e) {
		Point point = e.getPoint();
		if (thumbContainer.getBounds().contains(point)) {
			return e;
		}

		Rectangle trackerBounds = tracker.getBounds();
		if (!trackerBounds.contains(point)) {
			return e;
		}

		e.consume();

		Point boundedPoint = new Point(point.x, point.y);
		boundedPoint.translate(-trackerBounds.x, -trackerBounds.y);

		int thumbPosition = boundedPoint.y - (thumbContainer.getHeight() / 2);
		float scrollPercent = (float) thumbPosition / (tracker.getHeight() - thumbContainer.getHeight());

		clientThread.invoke(() -> {
			scrollToPercent(scrollPercent)
				.revalidate();
		});

		return e;
	}

	@Override
	public MouseEvent mousePressed(MouseEvent e) {
		Point point = e.getPoint();
		if (!thumbContainer.getBounds().contains(point)) {
			return e;
		}

		e.consume();
		thumbDragLast = point;

		return e;
	}

	@Override
	public MouseEvent mouseReleased(MouseEvent e) {
		if (!thumbContainer.getBounds().contains(e.getPoint())) {
			return e;
		}
		if (thumbDragLast == null) {
			return e;
		}

		e.consume();
		thumbDragLast = null;

		return e;
	}

	@Override
	public MouseEvent mouseDragged(MouseEvent e) {
		Point point = e.getPoint();
		if (thumbDragLast == null) {
			return e;
		}

		e.consume();

		int dragPixels = point.y - thumbDragLast.y;
		if (dragPixels == 0) {
			return e;
		}

		int maxScroll = content.getOriginalHeight() - container.getHeight() + scrollBuffer;
		int maxThumbPosition = tracker.getHeight() - thumbContainer.getHeight();
		int dragOffset = dragPixels * maxScroll / maxThumbPosition;

		clientThread.invoke(() -> {
			scroll(dragOffset)
				.revalidate();
		});

		thumbDragLast = point;

		return e;
	}

	public UIScrollBar scroll(int scrollDistance) {
		if (scrollAxis == ScrollAxis.VERTICAL) {
			int scrollY = content.getScrollY() + scrollDistance;
			scrollTo(scrollY);
		} else if (scrollAxis == ScrollAxis.HORIZONTAL) {
			int scrollX = content.getScrollX() + scrollDistance;
			scrollTo(scrollX);
		}

		return this;
	}

	public UIScrollBar scrollTo(@Range(from = 0, to = Integer.MAX_VALUE) int scrollOffset) {
		if (scrollAxis == ScrollAxis.VERTICAL) {
			int maxScroll = content.getOriginalHeight() - container.getHeight() + scrollBuffer;
			int scrollY = Math.max(0, Math.min(maxScroll, scrollOffset));
			content.setScrollY(scrollY);
		} else if (scrollAxis == ScrollAxis.HORIZONTAL) {
			int maxScroll = content.getOriginalWidth() - container.getWidth() + scrollBuffer;
			int scrollX = Math.max(0, Math.min(maxScroll, scrollOffset));
			content.setScrollX(scrollX);
		}

		return this;
	}

	public UIScrollBar scrollToPercent(@Range(from = 0, to = 1) float scrollPercent) {
		if (scrollAxis == ScrollAxis.VERTICAL) {
			int maxScroll = content.getOriginalHeight() - container.getHeight() + scrollBuffer;
			scrollTo((int) (maxScroll * scrollPercent));
		} else if (scrollAxis == ScrollAxis.HORIZONTAL) {
			int maxScroll = content.getOriginalWidth() - container.getWidth() + scrollBuffer;
			scrollTo((int) (maxScroll * scrollPercent));
		}

		return this;
	}

	private void initializeWidgets() {
		widget.setOriginalWidth(ARROW_SIZE)
			.revalidate();

		tracker.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, ARROW_SIZE)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, ARROW_SIZE * 2)
			.setSpriteTiling(true)
			.setSpriteId(SpriteID.ScrollbarDraggerV2.TRACK)
			.revalidate();

		thumbContainer.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, ARROW_SIZE)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setOriginalWidth(0)
			.revalidate();

		thumbMiddle.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, THUMB_EDGE_HEIGHT * 2)
			.setSpriteTiling(true)
			.setSpriteId(SpriteID.ScrollbarDraggerV2.MIDDLE)
			.revalidate();

		thumbTop.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(0, THUMB_EDGE_HEIGHT)
			.setSpriteId(SpriteID.ScrollbarDraggerV2.TOP)
			.revalidate();

		thumbBottom.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(0, THUMB_EDGE_HEIGHT)
			.setSpriteId(SpriteID.ScrollbarDraggerV2.BOTTOM)
			.revalidate();

		upArrow.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(0, ARROW_SIZE)
			.setSpriteId(SpriteID.ScrollbarV2.ARROW_UP)
			.revalidate();

		downArrow.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(0, ARROW_SIZE)
			.setSpriteId(SpriteID.ScrollbarV2.ARROW_DOWN)
			.revalidate();
	}

	@Override
	public void revalidate() {
		super.revalidate();

		content.revalidate();
		container.revalidate();
		tracker.revalidate();

		// TODO: handle horizontal scrollbar
		if (scrollAxis == ScrollAxis.VERTICAL) {
			float scrollRatio = Math.max(1, (float) content.getOriginalHeight() / widget.getHeight());
			int thumbHeight = Math.round(Math.max(THUMB_EDGE_HEIGHT * 2, tracker.getHeight() / scrollRatio));
			int maxScroll = content.getOriginalHeight() - container.getHeight() + scrollBuffer;
			float scrollPercent = (float) content.getScrollY() / maxScroll;
			int thumbPosition = Math.round((tracker.getHeight() - thumbHeight) * scrollPercent);
			thumbContainer.setOriginalY(thumbPosition + ARROW_SIZE)
				.setOriginalHeight(thumbHeight)
				.revalidate();
		}

		thumbMiddle.revalidate();
		thumbTop.revalidate();
		thumbBottom.revalidate();
		upArrow.revalidate();
		downArrow.revalidate();
	}

	public void unregister() {
		mouseManager.unregisterMouseListener(this);
	}
}

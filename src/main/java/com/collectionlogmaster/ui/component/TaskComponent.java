package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.ui.InterfaceManager;
import com.collectionlogmaster.ui.generic.BorderTheme;
import com.collectionlogmaster.ui.generic.UIBorderedContainer;
import com.collectionlogmaster.ui.generic.UIComponent;
import com.collectionlogmaster.ui.generic.UIUtil;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.runelite.api.FontID;
import net.runelite.api.ScriptEvent;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import org.jetbrains.annotations.Range;

@Accessors(chain = true)
public class TaskComponent extends UIComponent<TaskComponent> {
	private final UIBorderedContainer outerContainer;
	private final UIBorderedContainer imageContainer;
	private final Widget image;
	private final Widget name;

	@Getter
	@Setter
	private Task task = null;

	@Setter
	private int paddingSize = 10;

	@Inject
	private InterfaceManager interfaceManager;

	@Inject
	private TaskService taskService;

	public static TaskComponent createInside(Widget window) {
		return new TaskComponent(window.createChild(WidgetType.LAYER));
	}

	protected TaskComponent(Widget widget) {
		super(widget, WidgetType.LAYER);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

		outerContainer = UIBorderedContainer.createInside(widget);
		imageContainer = new UIBorderedContainer(outerContainer.getContent(), WidgetType.GRAPHIC);
		image = imageContainer.getContent();
		name = widget.createChild(WidgetType.TEXT);

		initializeWidgets();
	}

	public TaskComponent setOpacity(@Range(from = 0, to = 255) int transparency) {
		image.setOpacity(transparency);
		name.setOpacity(transparency);
		return this;
	}

	private void onActionSelected(ScriptEvent e) {
		int actionIndex = e.getOp();
		switch (actionIndex) {
			case 1:
				interfaceManager.openTaskInfo(task);
				return;

			case 2:
				taskService.complete(task.getId());
				return;
		}
	}

	private void initializeWidgets() {
		widget.setHasListener(true)
			.revalidate();

		widget.setOnOpListener((JavaScriptCallback) this::onActionSelected);

		outerContainer.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.setTheme(BorderTheme.ETCHED)
			.revalidate();

		imageContainer.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setTheme(BorderTheme.ETCHED)
			.revalidate();

		image.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setBorderType(1);

		image.revalidate();

		name.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setFontId(FontID.BOLD_12)
			.setTextColor(0xFFFFFF)
			.revalidate();
	}

	@Override
	public void revalidate() {
		super.revalidate();

		if (task != null) {
			widget.setName(UIUtil.formatName(task.getName()));
			widget.setAction(0, "View");
			widget.setAction(1, "Complete");
		} else {
			widget.setAction(0, null);
			widget.setAction(1, null);
		}

		outerContainer.revalidate();

		imageContainer.setPos(paddingSize, 0)
			.setSize(outerContainer.getHeight() - (paddingSize * 2), paddingSize * 2)
			.revalidate();

		int itemId = ItemID._100GUIDE_GUIDECAKE;
		String taskName = "No active task";
		if (task != null) {
			itemId = task.getDisplayItemId();
			taskName = task.getName();
		}

		image.setSize(paddingSize, paddingSize)
			.setItemId(itemId)
			.revalidate();

		name.setSize(outerContainer.getHeight() + paddingSize, 0)
			.setPos((outerContainer.getHeight() - paddingSize) / 2, 0)
			.setText(taskName)
			.revalidate();
	}
}

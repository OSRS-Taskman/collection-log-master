package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.ui.generic.UIComponent;
import com.collectionlogmaster.ui.generic.UIGridContainer;
import com.collectionlogmaster.ui.generic.UIScrollableContainer;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetType;


@Slf4j
public class TaskList extends UIComponent<TaskList> {
	public static final int TASK_COMPONENT_WIDTH = 300;
	public static final int TASK_COMPONENT_HEIGHT = 50;
	public static final int TASK_COMPONENT_PADDING = 5;

	@Inject
	private TaskService taskService;

	private final Widget background;
	private final UIScrollableContainer scrollableContainer;
	private final UIGridContainer taskGrid;

	private final List<@NonNull Task> tasks;

	private final List<TaskComponent> taskComponents = new ArrayList<>();

	public static TaskList createInside(Widget window, List<@NonNull Task> tasks) {
		return new TaskList(window.createChild(WidgetType.LAYER), tasks);
	}

	private TaskList(Widget widget, List<@NonNull Task> tasks) {
		super(widget, WidgetType.LAYER);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

		this.tasks = tasks;

		background = widget.createChild(WidgetType.GRAPHIC);
		scrollableContainer = UIScrollableContainer.createInside(widget);
		taskGrid = new UIGridContainer(scrollableContainer.getContent());

		initializeWidgets();
	}

	private void initializeWidgets() {
		widget.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.revalidate();

		background.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.setSpriteId(SpriteID.TRADEBACKING)
			.setSpriteTiling(true)
			.revalidate();

		scrollableContainer.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.revalidate();

		taskGrid.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setOriginalWidth(0)
			.revalidate();

		for (Task task : tasks) {
			TaskComponent taskComponent = new TaskComponent(taskGrid.createItem(WidgetType.LAYER))
				.setPaddingSize(TASK_COMPONENT_PADDING)
				.setSize(TASK_COMPONENT_WIDTH, TASK_COMPONENT_HEIGHT)
				.setTask(task);

			taskComponent.revalidate();
			taskComponents.add(taskComponent);
		}

		taskGrid.revalidate();
	}

	@Override
	public void revalidate() {
		super.revalidate();
		scrollableContainer.revalidate();
		taskGrid.revalidate();

		for (TaskComponent taskComponent : taskComponents) {
			Task task = taskComponent.getTask();
			boolean isComplete = taskService.isComplete(task.getId());

			taskComponent.setOpacity(isComplete ? 0 : 175)
				.revalidate();
		}
	}
}

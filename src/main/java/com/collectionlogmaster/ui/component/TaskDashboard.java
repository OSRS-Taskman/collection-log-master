package com.collectionlogmaster.ui.component;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.CollectionLogMasterPlugin;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.synchronization.SyncService;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.ui.generic.UIComponent;
import com.collectionlogmaster.ui.generic.UIUtil;
import com.collectionlogmaster.ui.generic.button.UIButton.State;
import com.collectionlogmaster.ui.generic.button.UITextButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import net.runelite.api.FontID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetSizeMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class TaskDashboard extends UIComponent<TaskDashboard> {
	public static final int BASE_GAP = 16;
	public static final int TITLE_HEIGHT = 50;
	public static final int BUTTON_HEIGHT = 30;
	public static final int BUTTON_WIDTH = 140;
	public static final int TASK_COMPONENT_WIDTH = 300;
	public static final int TASK_COMPONENT_HEIGHT = 70;
	public static final int MAX_ROLLING_STEPS = 65;

	@Inject
	private ScheduledExecutorService executorService;

	@Inject
	private ClientThread clientThread;

	@Inject
	private CollectionLogMasterConfig config;

	@Inject
	private TaskService taskService;

	@Inject
	private SyncService syncService;

	private final Widget title;
	private final TaskComponent taskComponent;
	private final UITextButton completeButton;
	private final UITextButton generateButton;
	private final UITextButton faqButton;
	private final UITextButton syncButton;
	private final Widget progress;

	public static TaskDashboard createInside(Widget window) {
		return new TaskDashboard(window.createChild(WidgetType.LAYER));
	}

	protected TaskDashboard(Widget widget) {
		super(widget, WidgetType.LAYER);
		CollectionLogMasterPlugin.getStaticInjector().injectMembers(this);

		title = widget.createChild(WidgetType.TEXT);
		taskComponent = TaskComponent.createInside(widget);
		completeButton = UITextButton.createInside(widget);
		generateButton = UITextButton.createInside(widget);
		faqButton = UITextButton.createInside(widget);
		syncButton = UITextButton.createInside(widget);
		progress = widget.createChild(WidgetType.TEXT);

		initializeWidgets();
	}

	private void initializeWidgets() {
		widget.setPos(0, 0)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setHeightMode(WidgetSizeMode.MINUS)
			.setSize(0, 0)
			.revalidate();

		title.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, BASE_GAP)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize(0, TITLE_HEIGHT)
			.setFontId(FontID.QUILL_CAPS_LARGE)
			.setTextColor(0xFFFFFF)
			.setTextShadowed(true)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.setText("Current Task")
			.revalidate();

		taskComponent.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, title.getRelativeY() + title.getHeight() + BASE_GAP)
			.setSize(TASK_COMPONENT_WIDTH, TASK_COMPONENT_HEIGHT)
			.revalidate();

		int actionButtonsY = taskComponent.getRelativeY() + taskComponent.getHeight() + BASE_GAP;
		completeButton.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(-((BUTTON_WIDTH / 2) + BASE_GAP), actionButtonsY)
			.setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
			.setText("Complete Task")
			.revalidate();

		generateButton.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos((BUTTON_WIDTH / 2) + BASE_GAP, actionButtonsY)
			.setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
			.setText("Generate Task")
			.revalidate();

		progress.setXPositionMode(WidgetPositionMode.ABSOLUTE_CENTER)
			.setPos(0, generateButton.getRelativeY() + generateButton.getHeight() + BASE_GAP)
			.setWidthMode(WidgetSizeMode.MINUS)
			.setSize((BUTTON_WIDTH + BASE_GAP) * 2, BUTTON_HEIGHT)
			.setFontId(FontID.BOLD_12)
			.setTextColor(0xFFFFFF)
			.setTextShadowed(true)
			.setXTextAlignment(WidgetTextAlignment.CENTER)
			.setYTextAlignment(WidgetTextAlignment.CENTER)
			.revalidate();

		syncButton.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setPos(BASE_GAP / 2, BASE_GAP / 2)
			.setSize(BUTTON_WIDTH / 2, BUTTON_HEIGHT)
			.setText("Sync")
			.setName(UIUtil.formatName("Sync"))
			.setAction("Visit", () -> {
				syncService.sync();
				revalidate();
			})
			.revalidate();

		faqButton.setXPositionMode(WidgetPositionMode.ABSOLUTE_RIGHT)
			.setYPositionMode(WidgetPositionMode.ABSOLUTE_BOTTOM)
			.setPos(BASE_GAP / 2, BASE_GAP / 2)
			.setSize(BUTTON_WIDTH / 2, BUTTON_HEIGHT)
			.setText("FAQ")
			.setName(UIUtil.formatName("FAQ"))
			.setAction("Check", UIUtil::openFAQ)
			.revalidate();
	}

	private @NotNull String getProgressText() {
		TaskTier tier = taskService.getCurrentTier();
		float percent = taskService.getProgress().get(tier);

		return String.format(
			"<col=%x>%d%%</col> %s Completed",
			UIUtil.getCompletionColor(percent),
			UIUtil.roundCompletionPercent(percent),
			tier.displayName
		);
	}

	private void generateTask() {
		Task generatedTask = taskService.generate();
		List<Task> rollTasks = getRollTasks();

		Stack<Pair<Task, Integer>> stepStack = new Stack<>();
		stepStack.push(Pair.of(generatedTask, 0));

		int timeLeft = config.rollTime();
		while (timeLeft > 0) {
			int stepDelay = calculateStepDelay(stepStack.size() - 1);

			stepStack.push(Pair.of(
				rollTasks.get(stepStack.size()),
				stepDelay
			));

			timeLeft -= stepDelay;
		}

		executeRollStep(stepStack);

		generateButton.setState(State.DISABLED)
			.revalidate();
	}

	/**
	 * Calculates the delay between the current step and the next one, in milliseconds. Uses an
	 * exponential function as to start very start and quickly slow down, trying to emulate a
	 * spinning prize wheel slowly decelerating.
	 */
	private int calculateStepDelay(int stepCount) {
		// a magic constant used to fine tune the total duration of all
		// steps, in order for it to match the configured roll time
		final double K = 250;

		// a magic constant used to fine tune how quickly the "wheel decelerates"
		final double Q = 36;

		// just the roll time divided by 1000
		final double R = config.rollTime() / 1000d;

		// (K * cbrt(R)) * e^(-s / sqrt(Q * R))
		return (int) ((K * Math.cbrt(R)) * Math.exp(-stepCount / Math.sqrt(Q * R)));
	}

	private void executeRollStep(Stack<Pair<Task, Integer>> stepStack) {
		Pair<Task, Integer> step = stepStack.pop();
		Task task = step.getLeft();
		int nextStepDelay = step.getRight();

		clientThread.invoke(() -> {
			taskComponent.setTask(task)
				.revalidate();
		});

		if (stepStack.empty()) {
			clientThread.invoke(this::revalidate);
			return;
		}

		executorService.schedule(() -> executeRollStep(stepStack), nextStepDelay, TimeUnit.MILLISECONDS);
	}

	private @NotNull List<Task> getRollTasks() {
		List<Task> candidateTasks = taskService.getIncompleteTierTasks();
		if (candidateTasks.size() < MAX_ROLLING_STEPS && config.rollPastCompleted()) {
			candidateTasks = taskService.getTierTasks();
		}

		while (candidateTasks.size() < MAX_ROLLING_STEPS) {
			candidateTasks.addAll(new ArrayList<>(candidateTasks));
		}

		Collections.shuffle(candidateTasks);

		return candidateTasks.subList(0, MAX_ROLLING_STEPS);
	}

	@Override
	public void revalidate() {
		super.revalidate();

		title.revalidate();

		Task activeTask = taskService.getActiveTask();
		taskComponent.setTask(activeTask)
			.revalidate();

		if (activeTask != null) {
			completeButton.setState(State.DEFAULT)
				.setName(UIUtil.formatName(activeTask.getName()))
				.setAction("Complete", () -> {
					taskService.complete();
					revalidate();
				})
				.revalidate();

			generateButton.setState(State.DISABLED)
				.setAction(null, null)
				.revalidate();
		} else {
			completeButton.setState(State.DISABLED)
				.setAction(null, null)
				.revalidate();

			generateButton.setState(State.DEFAULT)
				.setName(UIUtil.formatName("new task"))
				.setAction("Generate", this::generateTask)
				.revalidate();
		}

		String progressText = getProgressText();
		progress.setText(progressText)
			.revalidate();

		syncButton.revalidate();
		faqButton.revalidate();
	}
}

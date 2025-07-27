package com.logmaster;

import com.google.inject.Provides;
import com.logmaster.domain.Task;
import com.logmaster.domain.TaskPointer;
import com.logmaster.domain.TaskTier;
import com.logmaster.domain.verification.clog.CollectionLogVerification;
import com.logmaster.persistence.SaveDataManager;
import com.logmaster.synchronization.clog.CollectionLogService;
import com.logmaster.task.TaskService;
import com.logmaster.ui.InterfaceManager;
import com.logmaster.ui.component.TaskOverlay;
import com.logmaster.util.GsonOverride;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.LinkBrowser;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@PluginDescriptor(name = "Collection Log Master")
public class LogMasterPlugin extends Plugin {
    private static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;

	@Inject
	@SuppressWarnings("unused")
	private GsonOverride gsonOverride;

	@Inject
	private Client client;

	@Inject
	private LogMasterConfig config;

	@Inject
	private MouseManager mouseManager;

	@Inject
	protected TaskOverlay taskOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TaskService taskService;

	@Inject
	private SaveDataManager saveDataManager;

	@Inject
	private InterfaceManager interfaceManager;

	@Inject
	public ItemManager itemManager;

	@Inject
	public CollectionLogService collectionLogService;

	@Inject
	public PluginUpdateNotifier pluginUpdateNotifier;

	@Override
	protected void startUp()
	{
		collectionLogService.startUp();
		pluginUpdateNotifier.startUp();

		mouseManager.registerMouseWheelListener(interfaceManager);
		mouseManager.registerMouseListener(interfaceManager);
		interfaceManager.initialise();
		this.taskOverlay.setResizable(true);
		this.overlayManager.add(this.taskOverlay);
		this.taskService.getTaskList();
	}

	@Override
	protected void shutDown() {
		collectionLogService.shutDown();
		pluginUpdateNotifier.shutDown();

		mouseManager.unregisterMouseWheelListener(interfaceManager);
		mouseManager.unregisterMouseListener(interfaceManager);
		this.overlayManager.remove(this.taskOverlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals("log-master")) {
			return;
		}
		interfaceManager.updateAfterConfigChange();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		switch (gameStateChanged.getGameState())
		{
			case LOGGED_IN:
				saveDataManager.getSaveData();
				break;
			case LOGIN_SCREEN:
				saveDataManager.save();
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e) {
		if (e.getGroupId() == InterfaceID.COLLECTION) {
			interfaceManager.handleCollectionLogOpen();
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed e) {
		if (e.getGroupId() == InterfaceID.COLLECTION) {
			interfaceManager.handleCollectionLogClose();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired) {
		if (scriptPostFired.getScriptId() == COLLECTION_LOG_SETUP_SCRIPT_ID) {
			interfaceManager.handleCollectionLogScriptRan();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		interfaceManager.updateTaskListBounds();
	}

	public void generateTask() {
		TaskPointer pointer =this.saveDataManager.getSaveData().getActiveTaskPointer();
		if ((pointer != null && pointer.getTask() != null) || taskService.getTaskList() == null) {
			interfaceManager.disableGenerateTaskButton();
			return;
		}

		this.client.playSoundEffect(SoundEffectID.UI_BOOP);
		List<Task> uniqueTasks = findAvailableTasks();

		if (uniqueTasks.isEmpty()) {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "No more tasks left. Looks like you win?", "");
			playFailSound();

			return;
		}

		Task selectedTask = pickRandomTask(uniqueTasks);
		TaskPointer newTaskPointer = new TaskPointer();
		newTaskPointer.setTask(selectedTask);
		newTaskPointer.setTaskTier(getCurrentTier());
		this.saveDataManager.getSaveData().setActiveTaskPointer(newTaskPointer);
		this.saveDataManager.save();
		interfaceManager.rollTask(newTaskPointer.getTask().getName(), newTaskPointer.getTask().getDisplayItemId(), config.rollPastCompleted() ? taskService.getForTier(getCurrentTier()) : uniqueTasks);
		log.debug("Task generated: {} - {}", newTaskPointer.getTask().getName(), newTaskPointer.getTask().getId());

		this.saveDataManager.save();
	}

	private static Task pickRandomTask(List<Task> uniqueTasks) {
		int index = (int) Math.floor(Math.random() * uniqueTasks.size());
		Task task = uniqueTasks.get(index);

		if (!(task.getVerification() instanceof CollectionLogVerification)) {
			return task;
		}

		// get first of similarly named tasks
		String taskName = task.getName();
		Stream<Task> similarTasks = uniqueTasks.stream()
				.filter(t -> taskName.equals(t.getName()))
				.filter(t -> t.getVerification() instanceof CollectionLogVerification);

		return similarTasks.min(Comparator.comparingInt(
			t -> ((CollectionLogVerification) t.getVerification()).getCount()
		)).orElse(task);
	}

	public void completeTask() {
		TaskPointer activeTaskPointer = saveDataManager.getSaveData().getActiveTaskPointer();
		if (activeTaskPointer != null && activeTaskPointer.getTask() != null) {
			completeTask(activeTaskPointer.getTask().getId(), activeTaskPointer.getTaskTier());
		}
	}

	public boolean isTaskCompleted(String taskID, TaskTier tier) {
		return saveDataManager.getSaveData().getProgress().get(tier).contains(taskID);
	}

	public void completeTask(String taskID, TaskTier tier) {
		completeTask(taskID, tier, true);
	}

	public void completeTask(String taskID, TaskTier tier, boolean playSound) {
		if (playSound) {
			this.client.playSoundEffect(SoundEffectID.UI_BOOP);
		}

		if (saveDataManager.getSaveData().getProgress().get(tier).contains(taskID)) {
			saveDataManager.getSaveData().getProgress().get(tier).remove(taskID);
		} else {
			addCompletedTask(taskID, tier);
			TaskPointer activePointer = saveDataManager.getSaveData().getActiveTaskPointer();
			if (activePointer != null && activePointer.getTask() != null && taskID.equals(activePointer.getTask().getId())) {
				nullCurrentTask();
			}
		}
		this.saveDataManager.save();
		interfaceManager.completeTask();
	}

	public void nullCurrentTask() {
		this.saveDataManager.getSaveData().setActiveTaskPointer(null);
		this.saveDataManager.save();
		interfaceManager.clearCurrentTask();
	}

	public static int getCenterX(Widget window, int width) {
		return (window.getWidth() / 2) - (width / 2);
	}

	public static int getCenterY(Widget window, int height) {
		return (window.getHeight() / 2) - (height / 2);
	}

	public void addCompletedTask(String taskID, TaskTier tier) {
		this.saveDataManager.getSaveData().getProgress().get(tier).add(taskID);
		this.saveDataManager.save();
	}

	public TaskTier getCurrentTier() {
		TaskTier[] allTiers = TaskTier.values();
		int firstVisibleTier = 0;
		for (int i = 0; i < allTiers.length; i++) {
			if (config.hideBelow() == allTiers[i]) {
				firstVisibleTier = i;
			}
		}

		Map<TaskTier, Integer> tierPercentages = taskService.completionPercentages(saveDataManager.getSaveData());
		for (int i = firstVisibleTier; i < allTiers.length; i++) {
			TaskTier tier = allTiers[i];
			if (tierPercentages.get(tier) < 100) {
				return tier;
			}
		}


		return TaskTier.MASTER;
	}

	public TaskTier getSelectedTier() {
		return this.saveDataManager.getSaveData().getSelectedTier();
	}

	public List<Task> findAvailableTasks() {
		return taskService.getTaskList().getForTier(getCurrentTier()).stream().filter(t -> !this.saveDataManager.getSaveData().getProgress().get(getCurrentTier()).contains(t.getId())).collect(Collectors.toList());
	}

	public void playFailSound() {
		client.playSoundEffect(2277);
	}

	@Provides
	LogMasterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LogMasterConfig.class);
	}

	public void visitFaq() {
		LinkBrowser.browse("https://docs.google.com/document/d/e/2PACX-1vTHfXHzMQFbt_iYAP-O88uRhhz3wigh1KMiiuomU7ftli-rL_c3bRqfGYmUliE1EHcIr3LfMx2UTf2U/pub");
	}
}

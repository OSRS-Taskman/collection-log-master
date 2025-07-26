package com.logmaster;

import com.google.inject.Provides;
import com.logmaster.domain.Task;
import com.logmaster.domain.TaskTier;
import com.logmaster.synchronization.clog.CollectionLogService;
import com.logmaster.task.TaskService;
import com.logmaster.ui.InterfaceManager;
import com.logmaster.ui.component.TaskOverlay;
import com.logmaster.util.GsonOverride;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.LinkBrowser;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(name = "Collection Log Master")
public class LogMasterPlugin extends Plugin {
	@Inject
	@SuppressWarnings("unused")
	private GsonOverride gsonOverride;

	@Inject
	private Client client;

	@Inject
	protected TaskOverlay taskOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InterfaceManager interfaceManager;

	@Inject
	public ItemManager itemManager;

	@Inject
	public CollectionLogService collectionLogService;

	@Inject
	public PluginUpdateNotifier pluginUpdateNotifier;

	@Inject
	public TaskService taskService;

	@Getter
	@Setter
	// TODO: this is UI state, move it somewhere else
	private TaskTier selectedTier;

	@Override
	protected void startUp()
	{
		taskService.startUp();
		collectionLogService.startUp();
		pluginUpdateNotifier.startUp();
		interfaceManager.startUp();
		this.taskOverlay.setResizable(true);
		this.overlayManager.add(this.taskOverlay);
	}

	@Override
	protected void shutDown() {
		taskService.shutDown();
		collectionLogService.shutDown();
		pluginUpdateNotifier.shutDown();
		this.overlayManager.remove(this.taskOverlay);
	}

	@Provides
	LogMasterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(LogMasterConfig.class);
	}

	public void completeTask() {
		Task activeTask = taskService.getActiveTask();
		completeTask(activeTask.getId());
	}

	public void completeTask(String taskId) {
		completeTask(taskId, true);
	}

	public void completeTask(String taskId, boolean playSound) {
		if (playSound) {
			this.client.playSoundEffect(SoundEffectID.UI_BOOP);
		}

		taskService.toggleComplete(taskId);
		if (taskService.getActiveTask() == null) {
			interfaceManager.clearCurrentTask();
		}

		interfaceManager.completeTask();
	}

	public void visitFaq() {
		LinkBrowser.browse("https://docs.google.com/document/d/e/2PACX-1vTHfXHzMQFbt_iYAP-O88uRhhz3wigh1KMiiuomU7ftli-rL_c3bRqfGYmUliE1EHcIr3LfMx2UTf2U/pub");
	}
}

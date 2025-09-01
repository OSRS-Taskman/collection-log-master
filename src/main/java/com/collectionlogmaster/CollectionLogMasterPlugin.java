package com.collectionlogmaster;

import com.collectionlogmaster.command.DevCommandsManager;
import com.collectionlogmaster.command.TaskmanCommandManager;
import com.collectionlogmaster.synchronization.clog.CollectionLogService;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.ui.InterfaceManager;
import com.collectionlogmaster.ui.TaskOverlay;
import com.collectionlogmaster.util.GsonOverride;
import com.google.inject.Injector;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
		name = "Collection Log Master",
		conflicts = {"[DEPRECATED] Collection Log Master"})
public class CollectionLogMasterPlugin extends Plugin {
	@Inject
	@SuppressWarnings("unused")
	private GsonOverride gsonOverride;

	@Getter
	private static Injector staticInjector;

	@Inject
	protected TaskOverlay taskOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InterfaceManager interfaceManager;

	@Inject
	public CollectionLogService collectionLogService;

	@Inject
	public PluginUpdateNotifier pluginUpdateNotifier;

	@Inject
	public TaskService taskService;

	@Inject
	public TaskmanCommandManager taskmanCommand;

	@Inject
	public DevCommandsManager devCommands;

	@Override
	protected void startUp() {
		CollectionLogMasterPlugin.staticInjector = getInjector();

		taskService.startUp();
		collectionLogService.startUp();
		pluginUpdateNotifier.startUp();
		interfaceManager.startUp();
		taskmanCommand.startUp();
		devCommands.startUp();
		this.taskOverlay.setResizable(true);
		this.overlayManager.add(this.taskOverlay);
	}

	@Override
	protected void shutDown() {
		taskService.shutDown();
		collectionLogService.shutDown();
		pluginUpdateNotifier.shutDown();
		interfaceManager.shutDown();
		taskmanCommand.shutDown();
		devCommands.shutDown();
		this.overlayManager.remove(this.taskOverlay);
	}

	@Provides
	CollectionLogMasterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(CollectionLogMasterConfig.class);
	}
}

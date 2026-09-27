package com.collectionlogmaster.command;


import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.command.CommandResponse;
import com.collectionlogmaster.taskapp.TaskAppClient;
import com.collectionlogmaster.taskapp.TaskService;
import com.collectionlogmaster.util.EventBusSubscriber;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class TaskmanCommandManager extends EventBusSubscriber {
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatCommandManager chatCommandManager;

	@Inject
	private CollectionLogMasterConfig config;

	@Inject
	private TaskService taskService;

	@Inject
	private TaskAppClient taskAppClient;

	private final String COLLECTION_LOG_COMMAND = "!taskman";

	public void startUp() {
		super.startUp();

		if (config.isCommandEnabled()) {
			chatCommandManager.registerCommand(COLLECTION_LOG_COMMAND, this::executeCommand);
		}
	}

	public void shutDown() {
		super.shutDown();

		if (config.isCommandEnabled()) {
			chatCommandManager.unregisterCommand(COLLECTION_LOG_COMMAND);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(CollectionLogMasterConfig.CONFIG_GROUP)) return;
		if (!event.getKey().equals(CollectionLogMasterConfig.IS_COMMAND_ENABLED_KEY)) return;

		if (config.isCommandEnabled()) {
			chatCommandManager.registerCommand(COLLECTION_LOG_COMMAND, this::executeCommand);
		} else {
			chatCommandManager.unregisterCommand(COLLECTION_LOG_COMMAND);
		}
	}

	private void executeCommand(ChatMessage chatMessage, String message) {
		log.debug("Executing taskman command: {}", message);

		String senderName = chatMessage.getType().equals(ChatMessageType.PRIVATECHATOUT)
				? client.getLocalPlayer().getName()
				: Text.sanitize(chatMessage.getName());

		if (senderName == null) {
			log.debug("Couldn't identify message sender");
			return;
		}

		taskAppClient.fetchCommandData(senderName)
			.thenAccept(res ->
				clientThread.invokeLater(() -> replaceChatMessage(chatMessage, res))
			);
	}

	private void replaceChatMessage(ChatMessage chatMessage, CommandResponse res) {
		// TODO: still replace message but with an error
		if (res == null) return;

		String taskId = res.getTaskId();
		String taskName = "completed";
		if (taskId != null) {
			Task task = taskService.getTaskById(taskId);
			if (task != null) {
				taskName = task.getName();
			}
		}

		final String msg = new ChatMessageBuilder()
				.append(ChatColorType.NORMAL)
				.append("Progress: ")
				.append(ChatColorType.HIGHLIGHT)
				.append(res.getProgress() + "% " + res.getTier())
				.append(ChatColorType.NORMAL)
				.append(" Current task: ")
				.append(ChatColorType.HIGHLIGHT)
				.append(taskName)
				.build();

		final MessageNode messageNode = chatMessage.getMessageNode();
		messageNode.setRuneLiteFormatMessage(msg);
		client.refreshChat();
	}
}

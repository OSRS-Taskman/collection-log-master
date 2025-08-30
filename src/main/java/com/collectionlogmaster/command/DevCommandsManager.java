package com.collectionlogmaster.command;

import static com.collectionlogmaster.util.GsonOverride.GSON;

import com.collectionlogmaster.CollectionLogMasterConfig;
import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.domain.command.CommandRequest;
import com.collectionlogmaster.domain.command.CommandResponse;
import com.collectionlogmaster.task.SaveDataStorage;
import com.collectionlogmaster.task.TaskListStorage;
import com.collectionlogmaster.task.TaskService;
import com.collectionlogmaster.util.EventBusSubscriber;
import java.time.Instant;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Text;
import okhttp3.HttpUrl;

@Slf4j
@Singleton
public class DevCommandsManager extends EventBusSubscriber {
    private final String SET_ACTIVE_TASK_COMMAND = "set-active-task";

    @Inject
    private SaveDataStorage saveDataStorage;

    @Inject
    private TaskListStorage taskListStorage;

    @Inject
    @Named("developerMode")
    private boolean isDeveloperMode;

	@Subscribe
	public void onCommandExecuted(CommandExecuted e) {
        if (!isDeveloperMode) return;

        String command = e.getCommand();
        String[] args = e.getArguments();

		log.debug("Command executed: ::{} {}", command, args);
        if (command.equals(SET_ACTIVE_TASK_COMMAND)) {
		    executeSecActiveTaskCommand(args);
		}
	}

	private void executeSecActiveTaskCommand(String[] args) {
        if (args.length != 1) return;
		String taskPrefix = args[0];

        for (Task task : taskListStorage.get().all()) {
			String taskId = task.getId();
			if (taskId.startsWith(taskPrefix)) {
				log.debug("Setting active task to {}", taskId);
                saveDataStorage.get().setActiveTaskId(taskId);
				return;
			}
		}

		log.debug("Unable to find task with prefix {}", taskPrefix);
	}
}

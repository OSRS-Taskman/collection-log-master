package com.collectionlogmaster.command;


import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.task.SaveDataStorage;
import com.collectionlogmaster.task.TaskListStorage;
import com.collectionlogmaster.util.EventBusSubscriber;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.eventbus.Subscribe;

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

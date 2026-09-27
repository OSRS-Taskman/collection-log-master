package com.collectionlogmaster.synchronization;

import com.collectionlogmaster.domain.Task;
import com.collectionlogmaster.domain.TaskTier;
import com.collectionlogmaster.synchronization.clog.CollectionLogVerifier;
import com.collectionlogmaster.synchronization.diary.AchievementDiaryVerifier;
import com.collectionlogmaster.synchronization.skill.SkillVerifier;
import com.collectionlogmaster.taskapp.TaskAppClient;
import com.collectionlogmaster.taskapp.TaskAppStateStorage;
import com.collectionlogmaster.taskapp.TaskService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

@Slf4j
@Singleton
public class SyncService {
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private TaskService taskService;

	@Inject
	private TaskAppStateStorage taskAppStateStorage;

	@Inject
	private TaskAppClient taskAppClient;

	@Inject
	private CollectionLogVerifier collectionLogVerifier;

	@Inject
	private AchievementDiaryVerifier achievementDiaryVerifier;

	@Inject
	private SkillVerifier skillVerifier;

	private @NonNull Verifier<?>[] getVerifiers() {
		return new Verifier[] {
				this.collectionLogVerifier,
				this.achievementDiaryVerifier,
				this.skillVerifier
		};
	}

	private Boolean verify(Task task) {
		for (Verifier<?> verif : this.getVerifiers()) {
			if (verif.supports(task)) {
				return verif.verify(task);
			}
		}

		return null;
	}

	public List<Task> check() {
		return check(false);
	}

	public List<Task> check(boolean excludeActive) {
		Task activeTask = taskService.getActiveTask();
		List<Task> desyncedTasks = new ArrayList<>();

		for (TaskTier tier : TaskTier.values()) {
			for (Task task : taskService.getTierTasks(tier)) {
				if (excludeActive && task.equals(activeTask)) {
					continue;
				}

				Boolean isVerified = verify(task);
				if (isVerified == null) {
					continue;
				}

				boolean isSynced = isVerified == taskService.isComplete(task.getId());
				if (isSynced) {
					continue;
				}

				desyncedTasks.add(task);
			}
		}

		return desyncedTasks;
	}

	public CompletableFuture<Void> sync() {
		return taskAppClient.sync(
			collectionLogVerifier.verificationData(),
			achievementDiaryVerifier.verificationData(),
			skillVerifier.verificationData()
		).thenAccept(res -> {
			int updatedCount = res.getCompleted().size() + res.getUncompleted().size();
			List<String> messages = new ArrayList<>(updatedCount);

			for (String taskId : res.getCompleted()) {
				Task task = taskService.getTaskById(taskId);
				messages.add(String.format("Task '%s' marked as <col=27ae60>complete</col>", task.getName()));
			}

			for (String taskId : res.getUncompleted()) {
				Task task = taskService.getTaskById(taskId);
				messages.add(String.format("Task '%s' marked as <col=c0392b>incomplete</col>", task.getName()));
			}

			messages.add(String.format("Task synchronization finalized; %d tasks updated", updatedCount));

			clientThread.invoke(() -> {
				for (String msg : messages) {
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, null);
				}
			});
		}).thenCompose((v) -> taskAppStateStorage.fetch());
	}
}

package com.logmaster.ui;

import com.logmaster.LogMasterConfig;
import com.logmaster.domain.Task;
import com.logmaster.domain.TaskTier;
import com.logmaster.domain.command.CommandRequest;
import com.logmaster.domain.command.CommandResponse;
import com.logmaster.task.TaskService;
import com.logmaster.util.EventBusSubscriber;
import com.logmaster.util.HttpClient;
import com.logmaster.util.SimpleDebouncer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Text;
import okhttp3.HttpUrl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;

import static com.logmaster.util.GsonOverride.GSON;

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
    private LogMasterConfig config;

    @Inject
    private HttpClient httpClient;

    @Inject
    private TaskService taskService;

    @Inject
    private SimpleDebouncer updateDebouncer;

    private final HttpUrl baseApiUrl = new HttpUrl.Builder()
            .scheme("https")
            .host("taskman.up.railway.app")
            .addPathSegment("task")
            .addPathSegment("command")
            .build();

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
        if (!event.getGroup().equals(LogMasterConfig.CONFIG_GROUP)) return;
        if (!event.getKey().equals(LogMasterConfig.IS_COMMAND_ENABLED_KEY)) return;

        if (config.isCommandEnabled()) {
            chatCommandManager.registerCommand(COLLECTION_LOG_COMMAND, this::executeCommand);
            updateServerImmediately(true);
        } else {
            chatCommandManager.unregisterCommand(COLLECTION_LOG_COMMAND);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged e) {
        if (e.getGameState() != GameState.LOGGED_IN) return;

        clientThread.invokeAtTickEnd(this::updateServer);
    }

    public void updateServer() {
        updateServer(false);
    }

    public void updateServer(boolean skipReminder) {
        log.debug("Scheduling command update; {}", Instant.now());
        updateDebouncer.debounce(() -> updateServerImmediately(skipReminder));
    }

    public void updateServerImmediately(boolean skipReminder) {
        if (!config.isCommandEnabled() && !skipReminder) {
            remind();
            return;
        }

        log.debug("Executing command update; {}", Instant.now());

        String rsn = client.getLocalPlayer().getName();
        if (rsn == null) return;

        HttpUrl url = baseApiUrl.newBuilder().addPathSegment(rsn).build();

        String taskId = "complete";
        Task currentTask = taskService.getActiveTask();
        if (currentTask != null) {
            taskId = currentTask.getId();
        }

        TaskTier currentTier = taskService.getCurrentTier();
        float currentProgress = taskService.getProgress().get(currentTier);

        CommandRequest data = new CommandRequest(taskId, taskService.getCurrentTier().displayName, (int) currentProgress);
        httpClient.putHttpRequestAsync(url.toString(), GSON.toJson(data), null);
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

        HttpUrl url = baseApiUrl.newBuilder().addPathSegment(senderName).build();
        httpClient.getHttpRequestAsync(url.toString(), CommandResponse.class)
                .thenAccept(res -> {
                    clientThread.invokeLater(() -> replaceChatMessage(chatMessage, res));
                });
    }

    private void replaceChatMessage(ChatMessage chatMessage, CommandResponse res) {
        if (res == null) return;

        final String msg = new ChatMessageBuilder()
                .append(ChatColorType.NORMAL)
                .append("Progress: ")
                .append(ChatColorType.HIGHLIGHT)
                .append(res.getProgressPercentage() + "% " + res.getTier())
                .append(ChatColorType.NORMAL)
                .append(" Current task: ")
                .append(ChatColorType.HIGHLIGHT)
                .append(res.getTask().getName())
                .build();

        final MessageNode messageNode = chatMessage.getMessageNode();
        messageNode.setRuneLiteFormatMessage(msg);
        client.refreshChat();
    }

    private void remind() {
        if (!config.isCommandReminderEnabled()) {
            return;
        }

        clientThread.invoke(() -> {
            String msg = "<col=ff392b>Your data hasn't been synchronized with the command server because this feature is disabled."
                    + " You can enable it in the plugin config; you can also disable this reminder in the plugin config.";
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, "");
            client.playSoundEffect(2277);
        });
    }
}

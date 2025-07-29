package com.logmaster.ui;

import com.logmaster.LogMasterConfig;
import com.logmaster.domain.command.CommandResponse;
import com.logmaster.util.EventBusSubscriber;
import com.logmaster.util.HttpClient;
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
import okhttp3.HttpUrl;

import javax.inject.Inject;
import javax.inject.Singleton;

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

        HttpUrl url = baseApiUrl.newBuilder().addPathSegment(senderName).build();
        httpClient.getHttpRequestAsync(url.toString(), CommandResponse.class)
                .thenAccept(res -> {
                    clientThread.invokeLater(() -> replaceChatMessage(chatMessage, res));
                });
    }

    private void replaceChatMessage(ChatMessage chatMessage, CommandResponse res) {
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
}

package com.collectionlogmaster.synchronization.clog;

import com.collectionlogmaster.util.EventBusSubscriber;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton

// Code from: RuneProfile
// Repository: https://github.com/ReinhardtR/runeprofile-plugin
// License: BSD 2-Clause License
public class CollectionLogWidgetSubscriber extends EventBusSubscriber {
    private static final int COLLECTION_DELAYED_TRANSMIT_SCRIPT_ID = 4100;
    private static final int COLLECTION_LOG_SETUP_SCRIPT_ID = 7797;
    private static final int COLLECTION_INIT_SCRIPT_ID = 2240;

    @Inject
    private Client client;

    @Inject
    private CollectionLogService collectionLogService;

    private int tickCollectionLogScriptFired = -1;

    private boolean isAutoClogRetrieval = false;

    public void reset() {
        isAutoClogRetrieval = false;
        tickCollectionLogScriptFired = -1;
    }

    // Code from: WikiSync
    // Repository: https://github.com/weirdgloop/WikiSync
    // License: BSD 2-Clause License
    @Subscribe
    public void onGameStateChanged(GameStateChanged e) {
        switch (e.getGameState()) {
            case HOPPING:
			case LOGGING_IN:
			case CONNECTION_LOST:
                collectionLogService.reset();
                reset();
                break;
        }
    }

    // Code from: WikiSync
    // Repository: https://github.com/weirdgloop/WikiSync
    // License: BSD 2-Clause License
    @Subscribe
    public void onGameTick(GameTick gameTick) {
        int tick = client.getTickCount();
        boolean hasClogScriptFired = tickCollectionLogScriptFired != -1;
        boolean hasBufferPassed = tickCollectionLogScriptFired + 2 < tick;
        if (hasClogScriptFired && hasBufferPassed) {
            log.info("Collection log search script has finalized; {} items in storage", collectionLogService.getObtainedItems().size());
            tickCollectionLogScriptFired = -1;
            isAutoClogRetrieval = false;
        }
    }

    // Code from: WikiSync
    // Repository: https://github.com/weirdgloop/WikiSync
    // License: BSD 2-Clause License
    @Subscribe
    public void onScriptPreFired(ScriptPreFired preFired) {
        if (preFired.getScriptId() == COLLECTION_DELAYED_TRANSMIT_SCRIPT_ID) {
            log.debug("Collection log search script pre fired");
            tickCollectionLogScriptFired = client.getTickCount();

            Object[] args = preFired.getScriptEvent().getArguments();
            int itemId = (int) args[1];
            int quantity = (int) args[2];

            if (quantity > 0) {
                collectionLogService.storeItem(itemId);
            }
        }
    }

    // When the collection log is opened, automatically make the server transmit every clog
    // entry so the full collection log is stored for the next auto-sync (no profile update is
    // triggered here). The menuAction "Search" op is what requests the data from the server;
    // re-running the collection log init script then resets the view, closing the search again.
    @Subscribe
    public void onScriptPostFired(ScriptPostFired scriptPostFired) {
        if (scriptPostFired.getScriptId() == COLLECTION_LOG_SETUP_SCRIPT_ID) {
            log.info("Collection log setup script post fired; isAutoClogRetrieval: {}", isAutoClogRetrieval);

            // disallow updating from the adventure log, to avoid players updating their profile
            // while viewing other players collection logs using the POH adventure log.
            if (isOpenedFromAdventureLog()) {
                log.info("Resetting collection log data on post fired; window was opened from adventure log");
                collectionLogService.reset();
                return;
            }

             // guard against re-triggering from the init script we run below (which re-fires setup)
            if (isAutoClogRetrieval) {
                return;
            }

            // TODO: handle possible double fire when both this and RuneProfile are installed
            isAutoClogRetrieval = true;
            client.menuAction(
                    -1,
                    InterfaceID.Collection.SEARCH_TOGGLE,
                    MenuAction.CC_OP,
                    1,
                    -1,
                    "Search",
                    null
            );
            client.runScript(COLLECTION_INIT_SCRIPT_ID);
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        if (varbitChanged.getVarbitId() == VarbitID.COLLECTION_POH_HOST_BOOK_OPEN) {
            if (isOpenedFromAdventureLog()) {
                log.info("Resetting collection log data on varbit changed; window was opened from adventure log");
                collectionLogService.reset();
            }
        }
    }

    private boolean isOpenedFromAdventureLog() {
        return client.getVarbitValue(VarbitID.COLLECTION_POH_HOST_BOOK_OPEN) == 1;
    }
}

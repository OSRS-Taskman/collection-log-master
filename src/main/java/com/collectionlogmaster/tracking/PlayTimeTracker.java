package com.collectionlogmaster.tracking;

import com.collectionlogmaster.util.EventBusSubscriber;
import com.collectionlogmaster.util.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.gameval.VarClientID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class PlayTimeTracker extends EventBusSubscriber {
    private static final int TICKS_PER_MINUTE = 100;

    @Inject
    private Client client;

    @Getter
    private int playTimeTicks = 0;

    @Subscribe
    public void onVarClientIntChanged(VarClientIntChanged e) {
        if (e.getIndex() != VarClientID.ACCOUNT_SUMMARY_PLAYTIME) {
            return;
        }

        int newPlayTimeTicks = client.getVarcIntValue(VarClientID.ACCOUNT_SUMMARY_PLAYTIME) * TICKS_PER_MINUTE;
        int diff = newPlayTimeTicks - playTimeTicks;

        // the VarClient only has minute precision; to avoid overwriting the tick progress we tracked
        // so far, wq only update when the VarClient is ahead or if we drifted more than the maximum
        // expected of 1 minute; there are tricks we could use to be more precise, but I don't think
        // we'll need them; for now, we'll just log and if it becomes a problem we can address it
        // https://github.com/muffyn/time-played/blob/master/src/main/java/com/timeplayed/TimePlayedPlugin.java#L51
        if (newPlayTimeTicks < playTimeTicks && Math.abs(diff) <= TICKS_PER_MINUTE) {
            log.debug("Play time update skipped; current value {}t ({}); rejected diff: {}", playTimeTicks, StringUtils.ticksToDuration(playTimeTicks), diff);
            return;
        }

        if (playTimeTicks != 0 && Math.abs(diff) > TICKS_PER_MINUTE) {
            log.warn("Play time tracking drifted by {}t ({})", diff, StringUtils.ticksToDuration(diff));
        }

        playTimeTicks = newPlayTimeTicks;
        log.debug("Play time set to {}t ({}); diff: {}", playTimeTicks, StringUtils.ticksToDuration(playTimeTicks), diff);
    }

	@Subscribe
	public void onGameTick(GameTick event) {
        playTimeTicks++;
	}
}

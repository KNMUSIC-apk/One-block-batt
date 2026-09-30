package com.oneblock.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameCountdownEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public enum Phase { QUEUE, LAUNCH }

    private final Phase phase;
    private final int secondsLeft;

    public GameCountdownEvent(Phase phase, int secondsLeft) {
        this.phase = phase;
        this.secondsLeft = secondsLeft;
    }

    public Phase getPhase() { return phase; }
    public int getSecondsLeft() { return secondsLeft; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

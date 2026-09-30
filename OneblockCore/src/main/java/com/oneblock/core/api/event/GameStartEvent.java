package com.oneblock.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

public class GameStartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final List<UUID> participants;
    private final double startingBorderSize;

    public GameStartEvent(List<UUID> participants, double startingBorderSize) {
        this.participants = participants;
        this.startingBorderSize = startingBorderSize;
    }

    public List<UUID> getParticipants() { return participants; }
    public double getStartingBorderSize() { return startingBorderSize; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

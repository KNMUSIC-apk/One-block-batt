package com.oneblock.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

public class GameEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID winner;                 // null nếu hoà / huỷ
    private final List<UUID> participants;
    private final EndReason reason;

    public enum EndReason { LAST_MAN_STANDING, FORCE_STOP, NOT_ENOUGH_PLAYERS, ERROR }

    public GameEndEvent(UUID winner, List<UUID> participants, EndReason reason) {
        this.winner = winner;
        this.participants = participants;
        this.reason = reason;
    }

    public UUID getWinner() { return winner; }
    public List<UUID> getParticipants() { return participants; }
    public EndReason getReason() { return reason; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

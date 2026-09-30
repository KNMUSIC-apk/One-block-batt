package com.oneblock.core.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerLeaveArenaEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public enum LeaveReason { QUIT_COMMAND, DISCONNECT, ELIMINATED, GAME_END, KICK }

    private final Player player;
    private final LeaveReason reason;

    public PlayerLeaveArenaEvent(Player player, LeaveReason reason) {
        this.player = player;
        this.reason = reason;
    }

    public Player getPlayer() { return player; }
    public LeaveReason getReason() { return reason; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

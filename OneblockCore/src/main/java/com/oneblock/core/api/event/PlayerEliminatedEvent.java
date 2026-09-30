package com.oneblock.core.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerEliminatedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final UUID killer;      // nullable
    private final String cause;
    private final int remaining;

    public PlayerEliminatedEvent(Player player, UUID killer, String cause, int remaining) {
        this.player = player;
        this.killer = killer;
        this.cause = cause;
        this.remaining = remaining;
    }

    public Player getPlayer() { return player; }
    public UUID getKiller() { return killer; }
    public String getCause() { return cause; }
    public int getRemaining() { return remaining; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

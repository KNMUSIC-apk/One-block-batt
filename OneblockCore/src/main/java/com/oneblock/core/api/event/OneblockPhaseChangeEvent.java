package com.oneblock.core.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OneblockPhaseChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final int oldPhase;
    private final int newPhase;
    private final String newPhaseName;

    public OneblockPhaseChangeEvent(Player player, int oldPhase, int newPhase, String newPhaseName) {
        this.player = player;
        this.oldPhase = oldPhase;
        this.newPhase = newPhase;
        this.newPhaseName = newPhaseName;
    }

    public Player getPlayer() { return player; }
    public int getOldPhase() { return oldPhase; }
    public int getNewPhase() { return newPhase; }
    public String getNewPhaseName() { return newPhaseName; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

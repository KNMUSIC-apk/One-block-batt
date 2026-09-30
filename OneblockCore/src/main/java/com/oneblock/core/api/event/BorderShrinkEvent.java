package com.oneblock.core.api.event;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BorderShrinkEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final World world;
    private final double from;
    private final double to;
    private final long transitionMillis;

    public BorderShrinkEvent(World world, double from, double to, long transitionMillis) {
        this.world = world;
        this.from = from;
        this.to = to;
        this.transitionMillis = transitionMillis;
    }

    public World getWorld() { return world; }
    public double getFrom() { return from; }
    public double getTo() { return to; }
    public long getTransitionMillis() { return transitionMillis; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

package com.oneblock.core.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Bắn ra khi /oneblock reload — mọi plugin con lắng nghe để reload config riêng. */
public class OneblockReloadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

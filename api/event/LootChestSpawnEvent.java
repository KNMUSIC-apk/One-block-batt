package com.oneblock.core.api.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LootChestSpawnEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location location;
    private final int phase;
    private final List<ItemStack> loot;

    public LootChestSpawnEvent(Player player, Location location, int phase, List<ItemStack> loot) {
        this.player = player;
        this.location = location;
        this.phase = phase;
        this.loot = loot;
    }

    public Player getPlayer() { return player; }
    public Location getLocation() { return location; }
    public int getPhase() { return phase; }
    public List<ItemStack> getLoot() { return loot; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

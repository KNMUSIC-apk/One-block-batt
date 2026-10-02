package com.oneblock.arena.listener;

import com.oneblock.arena.OneblockArena;
import com.oneblock.arena.arena.ArenaManager;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.PlayerState;
import com.oneblock.core.api.event.PlayerJoinArenaEvent;
import com.oneblock.core.api.event.PlayerLeaveArenaEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArenaListener implements Listener {

    private final OneblockArena plugin;
    private final ArenaManager arena;

    public ArenaListener(OneblockArena plugin, ArenaManager arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    @EventHandler
    public void onJoinQueue(PlayerJoinArenaEvent e) {
        arena.onPlayerJoinedQueue(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerLeaveArenaEvent e) {
        Player p = e.getPlayer();

        if (e.getReason() == PlayerLeaveArenaEvent.LeaveReason.DISCONNECT && arena.isRunning()) {
            // Rớt đồ tại vị trí hiện tại khi thoát giữa trận
            dropInventory(p);
            arena.eliminate(p, "disconnect");
            return;
        }

        if (e.getReason() == PlayerLeaveArenaEvent.LeaveReason.QUIT_COMMAND) {
            arena.onPlayerLeftQueue();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        var ap = OneblockAPI.getPlayers().getIfPresent(p.getUniqueId());
        if (ap.isEmpty() || !ap.get().isPlaying()) return;
        if (!arena.isRunning()) return;

        // Cho phép rớt đồ tự nhiên, Arena sẽ chuyển spectator sau respawn
        p.setGameMode(GameMode.SPECTATOR);
        arena.eliminate(p, "death");
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        var ap = OneblockAPI.getPlayers().getIfPresent(p.getUniqueId());
        if (ap.isEmpty()) return;

        if (ap.get().getState() == PlayerState.SPECTATOR && arena.getArenaWorld() != null) {
            Location loc = ap.get().getPadLocation();
            if (loc != null) e.setRespawnLocation(loc.clone().add(0, 5, 0));
            else e.setRespawnLocation(arena.getArenaWorld().getSpawnLocation().add(0, 10, 0));
            Bukkit.getScheduler().runTask(plugin, () -> p.setGameMode(GameMode.SPECTATOR));
        }
    }

    private void dropInventory(Player p) {
        Location loc = p.getLocation();
        if (loc.getWorld() == null) return;
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack is : p.getInventory().getContents()) if (is != null) drops.add(is);
        for (ItemStack is : p.getInventory().getArmorContents()) if (is != null) drops.add(is);
        if (p.getInventory().getItemInOffHand() != null) drops.add(p.getInventory().getItemInOffHand());
        p.getInventory().clear();
        for (ItemStack is : drops) {
            loc.getWorld().dropItemNaturally(loc, is);
        }
    }
}

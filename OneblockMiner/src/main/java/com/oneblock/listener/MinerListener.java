package com.oneblock.miner.listener;

import com.oneblock.core.api.OneblockAPI;
import com.oneblock.miner.OneblockMiner;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MinerListener implements Listener {

    private final OneblockMiner plugin;

    public MinerListener(OneblockMiner plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            e.setCancelled(true);
            return;
        }
        boolean handled = plugin.getMinerManager().handleBreak(e.getPlayer(), e.getBlock());
        if (handled) {
            // Chặn rớt đồ mặc định (chúng ta set block mới ở tick sau)
            // Nếu bạn muốn giữ drop tự nhiên, bỏ dòng dưới.
            // e.setDropItems(true); // mặc định đã true
        }
    }

    /** Chặn đặt block đè lên pad (phá vỡ vòng lặp oneblock) */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (plugin.getMinerManager().isPad(e.getBlockPlaced().getLocation())) {
            e.setCancelled(true);
        }
    }

    /** Chặn click phải vào rương của pad người khác */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        var owner = plugin.getMinerManager().getOwner(e.getClickedBlock().getLocation());
        if (owner == null) return;
        if (!owner.equals(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }
}

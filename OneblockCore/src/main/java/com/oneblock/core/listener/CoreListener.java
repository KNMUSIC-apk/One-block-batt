package com.oneblock.core.listener;

import com.oneblock.core.OneblockCore;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.PlayerState;
import com.oneblock.core.api.event.PlayerLeaveArenaEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class CoreListener implements Listener {

    private final OneblockCore plugin;

    public CoreListener(OneblockCore plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Nếu đang trong hàng đợi → rời queue
        if (plugin.getGameManager().isQueued(player.getUniqueId())) {
            plugin.getGameManager().removeFromQueue(player.getUniqueId());
        }

        // Nếu đang chơi → thông báo cho Arena xử lý (rớt đồ, loại khỏi trận)
        var ap = OneblockAPI.getPlayers().getIfPresent(player.getUniqueId());
        if (ap.isPresent() && ap.get().isInGame()) {
            new PlayerLeaveArenaEvent(player, PlayerLeaveArenaEvent.LeaveReason.DISCONNECT).callEvent();
        }

        // Nếu ở lobby → chỉ dọn cache sau 1 tick
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                var data = OneblockAPI.getPlayers().getIfPresent(player.getUniqueId());
                if (data.isEmpty() || data.get().getState() == PlayerState.LOBBY) {
                    OneblockAPI.getPlayers().remove(player.getUniqueId());
                }
            }
        }, 40L);
    }
}

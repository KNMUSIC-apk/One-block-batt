package com.oneblock.core.command;

import com.oneblock.core.OneblockCore;
import com.oneblock.core.api.GameState;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.PlayerState;
import com.oneblock.core.api.event.PlayerJoinArenaEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public class JoinCommand implements CommandExecutor, TabCompleter {

    private final OneblockCore plugin;

    public JoinCommand(OneblockCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String prefix = color(plugin.getConfig().getString("messages.prefix", ""));
        if (!(sender instanceof Player player)) {
            sender.sendMessage(prefix + color(plugin.getConfig().getString("messages.only-player", "")));
            return true;
        }

        var gm = plugin.getGameManager();

        if (!gm.isAcceptingPlayers()) {
            player.sendMessage(prefix + color(plugin.getConfig().getString("messages.game-in-progress", "")));
            return true;
        }
        if (gm.isQueued(player.getUniqueId())) {
            player.sendMessage(prefix + "&eBạn đã ở trong phòng chờ rồi.");
            return true;
        }
        if (gm.getQueueSize() >= gm.getMaxPlayers()) {
            player.sendMessage(prefix + color(plugin.getConfig().getString("messages.queue-full", "")));
            return true;
        }

        PlayerJoinArenaEvent event = new PlayerJoinArenaEvent(player);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        // Lưu state + xoá túi đồ
        OneblockAPI.getPlayers().saveAndClear(player);
        var ap = OneblockAPI.getPlayers().get(player);
        ap.setState(PlayerState.QUEUE);
        ap.setJoinedAt(System.currentTimeMillis());

        gm.addToQueue(player.getUniqueId());

        var spawn = plugin.getLocationManager().getJoinSpawn();
        if (spawn != null) player.teleport(spawn);

        String msg = color(plugin.getConfig().getString("messages.joined-queue", ""))
                .replace("{current}", String.valueOf(gm.getQueueSize()))
                .replace("{max}", String.valueOf(gm.getMaxPlayers()));
        player.sendMessage(prefix + msg);

        return true;
    }

    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) { return List.of(); }
}

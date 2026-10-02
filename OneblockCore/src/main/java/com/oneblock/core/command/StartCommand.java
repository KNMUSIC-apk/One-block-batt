package com.oneblock.core.command;

import com.oneblock.core.OneblockCore;
import com.oneblock.core.api.OneblockAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

import java.util.List;

public class StartCommand implements CommandExecutor, TabCompleter {

    private final OneblockCore plugin;
    public StartCommand(OneblockCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        var arena = OneblockAPI.getArenaService();
        if (arena.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "OneblockArena chưa được load.");
            return true;
        }
        if (arena.get().isRunning()) {
            sender.sendMessage(ChatColor.RED + "Trận đấu đang diễn ra.");
            return true;
        }
        if (plugin.getGameManager().getQueueSize() < 1) {
            sender.sendMessage(ChatColor.RED + "Không có ai trong phòng chờ.");
            return true;
        }
        arena.get().forceStart();
        sender.sendMessage(ChatColor.GREEN + "Đã bắt đầu trận đấu.");
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) { return List.of(); }
}

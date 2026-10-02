package com.oneblock.core.command;

import com.oneblock.core.OneblockCore;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public class SetLobbyCommand implements CommandExecutor, TabCompleter {

    private final OneblockCore plugin;
    public SetLobbyCommand(OneblockCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Player only."); return true; }
        plugin.getLocationManager().setLobby(p.getLocation());
        p.sendMessage(ChatColor.GREEN + "Đã đặt vị trí lobby.");
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) { return List.of(); }
}

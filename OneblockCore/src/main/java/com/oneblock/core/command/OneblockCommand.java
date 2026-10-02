package com.oneblock.core.command;

import com.oneblock.core.OneblockCore;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

import java.util.List;

public class OneblockCommand implements CommandExecutor, TabCompleter {

    private final OneblockCore plugin;
    public OneblockCommand(OneblockCore plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /oneblock reload");
            return true;
        }
        plugin.reloadAll();
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", ""));
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.reloaded", "&aReloaded.")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        return a.length == 1 ? List.of("reload") : List.of();
    }
}

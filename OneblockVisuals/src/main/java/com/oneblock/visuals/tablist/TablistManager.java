package com.oneblock.visuals.visual;

import com.oneblock.core.api.ArenaPlayer;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.visuals.OneblockVisuals;
import com.oneblock.visuals.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TablistManager {

    private final OneblockVisuals plugin;
    private BukkitTask task;

    public TablistManager(OneblockVisuals plugin) { this.plugin = plugin; }

    public void start() {
        stop();
        if (!plugin.getConfig().getBoolean("tablist.enabled", true)) return;
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) update(p);
        }, 20L, 40L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    public void update(Player player) {
        var gm = OneblockAPI.getGame();
        ArenaPlayer ap = OneblockAPI.getPlayers().get(player);

        List<String> headerLines = plugin.getConfig().getStringList("tablist.header");
        List<String> footerLines = plugin.getConfig().getStringList("tablist.footer");

        Component header = build(headerLines, player, ap, gm);
        Component footer = build(footerLines, player, ap, gm);

        player.sendPlayerListHeaderAndFooter(header, footer);
    }

    private Component build(List<String> lines, Player p, ArenaPlayer ap,
                            com.oneblock.core.manager.GameManager gm) {
        if (lines == null || lines.isEmpty()) return Component.empty();
        List<Component> comps = new ArrayList<>();
        for (String raw : lines) {
            String s = Text.color(raw);
            s = s.replace("{alive}", String.valueOf(gm.getAlivePlayers()));
            s = s.replace("{total}", String.valueOf(gm.getParticipants().size()));
            s = s.replace("{mined}", String.valueOf(ap.getBlocksMined()));
            s = s.replace("{phase_name}", "&e#" + ap.getPhase());
            s = s.replace("{player}", p.getName());
            comps.add(Text.c(s.replace('§', '&')));
        }
        Component out = comps.get(0);
        for (int i = 1; i < comps.size(); i++) {
            out = out.append(Component.newline()).append(comps.get(i));
        }
        return out;
    }
}

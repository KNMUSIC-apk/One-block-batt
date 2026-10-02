package com.oneblock.visuals.task;

import com.oneblock.core.api.ArenaPlayer;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.PlayerState;
import com.oneblock.visuals.OneblockVisuals;
import com.oneblock.visuals.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ActionBarTask {

    private final OneblockVisuals plugin;
    private BukkitTask task;

    public ActionBarTask(OneblockVisuals plugin) { this.plugin = plugin; }

    public void start() {
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            var gm = OneblockAPI.getGame();
            if (!gm.isRunning()) return;

            String playingFmt = plugin.getConfig().getString("actionbar.playing", "");
            String specFmt = plugin.getConfig().getString("actionbar.spectator", "");

            for (Player p : Bukkit.getOnlinePlayers()) {
                ArenaPlayer ap = OneblockAPI.getPlayers().get(p);

                String fmt = switch (ap.getState()) {
                    case PLAYING -> playingFmt;
                    case SPECTATOR -> specFmt;
                    default -> null;
                };
                if (fmt == null || fmt.isEmpty()) continue;

                String s = Text.color(fmt)
                        .replace("{phase_name}", "&e#" + ap.getPhase())
                        .replace("{mined}", String.valueOf(ap.getBlocksMined()))
                        .replace("{border}", String.valueOf((int) gm.getCurrentBorderSize()))
                        .replace("{alive}", String.valueOf(gm.getAlivePlayers()));

                p.sendActionBar(Text.c(s.replace('§', '&')));
            }
        }, 20L, 20L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }
}

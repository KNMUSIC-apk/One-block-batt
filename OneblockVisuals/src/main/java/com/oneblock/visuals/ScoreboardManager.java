package com.oneblock.visuals.visual;

import com.oneblock.core.api.ArenaPlayer;
import com.oneblock.core.api.GameState;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.visuals.OneblockVisuals;
import com.oneblock.visuals.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.List;

public class ScoreboardManager {

    private final OneblockVisuals plugin;
    private BukkitTask task;

    public ScoreboardManager(OneblockVisuals plugin) { this.plugin = plugin; }

    public void start() {
        stop();
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) return;
        long period = plugin.getConfig().getLong("scoreboard.update-ticks", 10);

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                update(p);
            }
        }, 20L, period);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    public void update(Player player) {
        var gm = OneblockAPI.getGame();
        ArenaPlayer ap = OneblockAPI.getPlayers().get(player);

        Scoreboard board = player.getScoreboard();
        // Nếu đang dùng scoreboard của người khác (team) → tạo mới
        if (board == null || board.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(board);
        }

        Objective obj = board.getObjective("ob");
        if (obj == null) {
            obj = board.registerNewObjective("ob", Criteria.DUMMY, Text.c(
                    plugin.getConfig().getString("scoreboard.title", "&6&lONEBLOCK")));
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Xoá toàn bộ entry cũ
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int score = lines.size();
        int i = 0;
        for (String raw : lines) {
            String line = format(raw, player, ap, gm);
            String entry = uniqueEntry(board, line, i++);
            obj.getScore(entry).setScore(score--);
        }
    }

    private String uniqueEntry(Scoreboard board, String line, int index) {
        // Đảm bảo entry không trùng (scoreboard yêu cầu unique)
        String base = Text.color(line);
        if (base.isEmpty()) base = " ";
        String candidate = base;
        int suffix = 0;
        while (board.getEntries().contains(candidate) && suffix < 100) {
            candidate = base + "§r".repeat(++suffix);
        }
        return candidate;
    }

    private String format(String raw, Player p, ArenaPlayer ap, com.oneblock.core.manager.GameManager gm) {
        String s = Text.color(raw);

        s = s.replace("{state}", gm.getState().name());
        s = s.replace("{alive}", String.valueOf(gm.getAlivePlayers()));
        s = s.replace("{total}", String.valueOf(gm.getParticipants().size()));
        s = s.replace("{border}", String.valueOf((int) gm.getCurrentBorderSize()));
        s = s.replace("{shrink}", formatShrink(gm.getNextBorderShrinkAt()));
        s = s.replace("{mined}", String.valueOf(ap.getBlocksMined()));
        s = s.replace("{phase}", String.valueOf(ap.getPhase()));
        s = s.replace("{phase_name}", phaseName(ap.getPhase()));
        s = s.replace("{player}", p.getName());
        s = s.replace("{website}", plugin.getConfig().getString("website", ""));

        return s;
    }

    private String phaseName(int phase) {
        var miner = OneblockAPI.getMinerService();
        if (miner.isEmpty()) return "&f" + phase;
        // Dùng reflection-free: đọc từ event phase name đã lưu, hoặc fallback
        return "&e#" + phase;
    }

    private String formatShrink(long nextAt) {
        if (nextAt <= 0) return "—";
        long left = nextAt - System.currentTimeMillis();
        if (left <= 0) return "0s";
        long sec = left / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }
}

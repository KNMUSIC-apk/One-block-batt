package com.oneblock.visuals.listener;

import com.oneblock.core.api.GameState;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.event.*;
import com.oneblock.visuals.OneblockVisuals;
import com.oneblock.visuals.util.Text;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

public class VisualListener implements Listener {

    private final OneblockVisuals plugin;

    public VisualListener(OneblockVisuals plugin) { this.plugin = plugin; }

    // ------------------------------------------------------------------
    @EventHandler
    public void onCountdown(GameCountdownEvent e) {
        String path = e.getPhase() == GameCountdownEvent.Phase.QUEUE
                ? "titles.countdown-queue" : "titles.countdown-launch";

        // Title cho tất cả người trong queue
        for (var uuid : OneblockAPI.getGame().getQueue()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            sendTitleFromConfig(p, path, "{time}", String.valueOf(e.getSecondsLeft()));

            // Sound tick mỗi giây
            playSound(p, "sounds.countdown-tick", "sounds.countdown-tick-pitch");
        }
    }

    // ------------------------------------------------------------------
    @EventHandler
    public void onStart(GameStartEvent e) {
        for (var uuid : e.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            sendTitleFromConfig(p, "titles.game-start", null, null);
            playSound(p, "sounds.game-start", "sounds.game-start-pitch");
        }
    }

    // ------------------------------------------------------------------
    @EventHandler
    public void onEliminated(PlayerEliminatedEvent e) {
        Player p = e.getPlayer();
        sendTitleFromConfig(p, "titles.eliminated", null, null);
        playSound(p, "sounds.player-eliminated", "sounds.player-eliminated-pitch");

        // Thông báo cho người còn sống
        for (var uuid : OneblockAPI.getGame().getParticipants()) {
            if (uuid.equals(p.getUniqueId())) continue;
            Player other = Bukkit.getPlayer(uuid);
            if (other == null) continue;
            other.sendMessage(Text.c("&8[&6Oneblock&8] &c" + p.getName()
                    + " &7đã bị loại. &eCòn lại: &f" + e.getRemaining()));
        }
    }

    // ------------------------------------------------------------------
    @EventHandler
    public void onShrink(BorderShrinkEvent e) {
        for (Player p : e.getWorld().getPlayers()) {
            sendTitleFromConfig(p, "titles.border-shrink", "{size}", String.valueOf((int) e.getTo()));
            playSound(p, "sounds.border-shrink", "sounds.border-shrink-pitch");
        }
    }

    // ------------------------------------------------------------------
    @EventHandler
    public void onChest(LootChestSpawnEvent e) {
        playSound(e.getPlayer(), "sounds.chest-spawn", "sounds.chest-spawn-pitch");
    }

    // ------------------------------------------------------------------
    @EventHandler
    public void onPhase(OneblockPhaseChangeEvent e) {
        Player p = e.getPlayer();
        playSound(p, "sounds.phase-change", "sounds.phase-change-pitch");
        p.sendActionBar(Text.c("&6✦ Chu kỳ mới: " + e.getNewPhaseName()));
    }

    // ------------------------------------------------------------------
    @EventHandler
    public void onEnd(GameEndEvent e) {
        if (e.getWinner() == null) return;
        Player winner = Bukkit.getPlayer(e.getWinner());
        for (var uuid : e.getParticipants()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            String name = winner != null ? winner.getName() : "?";
            sendTitleFromConfig(p, "titles.victory", "{player}", name);
            playSound(p, "sounds.victory", "sounds.victory-pitch");
        }
    }

    // ==================================================================
    //  HELPERS
    // ==================================================================
    private void sendTitleFromConfig(Player p, String path, String placeholder, String value) {
        String title = plugin.getConfig().getString(path + ".title", "");
        String subtitle = plugin.getConfig().getString(path + ".subtitle", "");
        int fadeIn = plugin.getConfig().getInt(path + ".fadeIn", 5);
        int stay = plugin.getConfig().getInt(path + ".stay", 30);
        int fadeOut = plugin.getConfig().getInt(path + ".fadeOut", 10);

        if (placeholder != null && value != null) {
            title = title.replace(placeholder, value);
            subtitle = subtitle.replace(placeholder, value);
        }

        Title t = Title.title(
                Text.c(title.replace('&', '&')),
                Text.c(subtitle.replace('&', '&')),
                Title.Times.times(
                        Duration.ofMillis(fadeIn * 50L),
                        Duration.ofMillis(stay * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                ));
        p.showTitle(t);
    }

    private void playSound(Player p, String path, String pitchPath) {
        String soundName = plugin.getConfig().getString(path);
        if (soundName == null) return;
        Sound sound;
        try { sound = Sound.valueOf(soundName.toUpperCase()); }
        catch (IllegalArgumentException ex) { return; }

        float pitch = (float) plugin.getConfig().getDouble(pitchPath, 1.0);
        p.playSound(p.getLocation(), sound, 1.0f, pitch);
    }
}

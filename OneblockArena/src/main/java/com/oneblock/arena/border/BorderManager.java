package com.oneblock.arena.border;

import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.event.BorderShrinkEvent;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;

/**
 * Quản lý vòng bo. Dùng cơ chế "step shrink" để có thể bắn event cho Visuals
 * và tránh việc setSize liên tục gây lag.
 */
public class BorderManager {

    private final Plugin plugin;
    private BorderConfig config;

    private BukkitRunnable task;
    private World world;
    private double currentSize;

    public BorderManager(Plugin plugin, BorderConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void setConfig(BorderConfig config) { this.config = config; }

    public void start(World world, int playerCount) {
        stop();
        this.world = world;
        this.currentSize = config.initialSizeFor(playerCount);

        WorldBorder wb = world.getWorldBorder();
        wb.setCenter(0.5, 0.5);
        wb.setSize(currentSize);
        wb.setDamageAmount(config.damageAmount());
        wb.setDamageBuffer(config.damageBuffer());
        wb.setWarningDistance(config.warningDistance());

        OneblockAPI.getGame().setCurrentBorderSize(currentSize);
        long intervalMs = config.shrinkIntervalSeconds() * 1000L;
        OneblockAPI.getGame().setNextBorderShrinkAt(System.currentTimeMillis() + intervalMs);

        long period = config.shrinkIntervalSeconds() * 20L;

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (world == null || !OneblockAPI.getGame().isRunning()) { cancel(); return; }
                if (currentSize <= config.minSize()) { cancel(); return; }

                double from = currentSize;
                double to = Math.max(config.minSize(), from - config.shrinkStep());
                if (to == from) { cancel(); return; }

                // Thu mượt trong shrinkDurationSeconds
                world.getWorldBorder().setSize(to, config.shrinkDurationSeconds());
                currentSize = to;

                OneblockAPI.getGame().setCurrentBorderSize(to);
                OneblockAPI.getGame().setNextBorderShrinkAt(
                        System.currentTimeMillis() + config.shrinkIntervalSeconds() * 1000L);

                new BorderShrinkEvent(world, from, to, config.shrinkDurationSeconds() * 1000L).callEvent();

                if (to <= config.minSize()) cancel();
            }
        };
        task.runTaskTimer(plugin, period, period);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    public double getCurrentSize() { return currentSize; }
}

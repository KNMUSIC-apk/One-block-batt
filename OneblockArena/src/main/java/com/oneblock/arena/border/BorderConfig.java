package com.oneblock.arena.border;

import org.bukkit.configuration.file.FileConfiguration;

public record BorderConfig(
        double baseSize,
        double perPlayer,
        double minSize,
        int shrinkIntervalSeconds,
        int shrinkStep,
        int shrinkDurationSeconds,
        double damageAmount,
        double damageBuffer,
        int warningDistance
) {
    public static BorderConfig from(FileConfiguration cfg) {
        return new BorderConfig(
                cfg.getDouble("border.base-size", 50),
                cfg.getDouble("border.per-player", 30),
                cfg.getDouble("border.min-size", 10),
                cfg.getInt("border.shrink-interval-seconds", 45),
                cfg.getInt("border.shrink-step", 5),
                cfg.getInt("border.shrink-duration-seconds", 10),
                cfg.getDouble("border.damage-amount", 1.0),
                cfg.getDouble("border.damage-buffer", 5.0),
                cfg.getInt("border.warning-distance", 5)
        );
    }

    public double initialSizeFor(int players) {
        return Math.max(minSize, baseSize + Math.max(0, players - 2) * perPlayer);
    }
}

package com.oneblock.core.api.service;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface OneblockArenaService {
    boolean isRunning();
    void forceStart();
    int getAliveCount();
    void eliminate(Player player, String reason);
    void endGame(UUID winner);
}

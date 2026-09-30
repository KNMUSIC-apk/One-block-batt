package com.oneblock.core.api.service;

import org.bukkit.Location;

import java.util.UUID;

public interface OneblockMinerService {
    void registerPad(Location location, UUID owner);
    void clearPads();
    int getBlocksMined(UUID uuid);
    int getPhase(UUID uuid);
}

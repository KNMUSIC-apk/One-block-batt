package com.oneblock.miner.miner;

import com.oneblock.core.api.ArenaPlayer;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.event.LootChestSpawnEvent;
import com.oneblock.core.api.event.OneblockBreakEvent;
import com.oneblock.core.api.event.OneblockPhaseChangeEvent;
import com.oneblock.core.api.service.OneblockMinerService;
import com.oneblock.miner.OneblockMiner;
import com.oneblock.miner.phase.Phase;
import com.oneblock.miner.phase.PhaseRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class MinerManager implements OneblockMinerService {

    private final OneblockMiner plugin;
    private final PhaseRegistry registry;

    /** Vị trí pad (block toạ độ nguyên) → chủ sở hữu */
    private final Map<Location, UUID> padOwners = new ConcurrentHashMap<>();

    public MinerManager(OneblockMiner plugin, PhaseRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public PhaseRegistry getRegistry() { return registry; }

    // ==========================================================
    //  PAD REGISTRY
    // ==========================================================
    @Override
    public void registerPad(Location location, UUID owner) {
     

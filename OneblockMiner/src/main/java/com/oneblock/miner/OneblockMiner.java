package com.oneblock.miner;

import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.event.OneblockReloadEvent;
import com.oneblock.miner.listener.MinerListener;
import com.oneblock.miner.miner.MinerManager;
import com.oneblock.miner.phase.PhaseRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneblockMiner extends JavaPlugin implements Listener {

    private PhaseRegistry phaseRegistry;
    private MinerManager minerManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.phaseRegistry = new PhaseRegistry(this);
        this.phaseRegistry.load();

        this.minerManager = new MinerManager(this, phaseRegistry);

        OneblockAPI.registerMinerService(minerManager);

        getServer().getPluginManager().registerEvents(new MinerListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("OneblockMiner đã khởi động với " + phaseRegistry.getPhases().size() + " chu kỳ.");
    }

    @Override
    public void onDisable() {
        OneblockAPI.unregisterMinerService();
    }

    public PhaseRegistry getPhaseRegistry() { return phaseRegistry; }
    public MinerManager getMinerManager() { return minerManager; }

    @EventHandler
    public void onReload(OneblockReloadEvent e) {
        reloadConfig();
        phaseRegistry.load();
    }
}

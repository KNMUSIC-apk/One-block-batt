package com.oneblock.arena;

import com.oneblock.arena.arena.ArenaManager;
import com.oneblock.arena.border.BorderConfig;
import com.oneblock.arena.listener.ArenaListener;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.event.OneblockReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneblockArena extends JavaPlugin implements Listener {

    private ArenaManager arenaManager;
    private BorderConfig borderConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.borderConfig = BorderConfig.from(getConfig());
        this.arenaManager = new ArenaManager(this);

        OneblockAPI.registerArenaService(arenaManager);

        getServer().getPluginManager().registerEvents(new ArenaListener(this, arenaManager), this);
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("OneblockArena đã khởi động.");
    }

    @Override
    public void onDisable() {
        OneblockAPI.unregisterArenaService();
        getLogger().info("OneblockArena đã tắt.");
    }

    public ArenaManager getArenaManager() { return arenaManager; }
    public BorderConfig getBorderConfig() { return borderConfig; }

    @EventHandler
    public void onReload(OneblockReloadEvent e) {
        reloadConfig();
        this.borderConfig = BorderConfig.from(getConfig());
        arenaManager.getBorderManager().setConfig(b

package com.oneblock.core;

import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.event.OneblockReloadEvent;
import com.oneblock.core.command.*;
import com.oneblock.core.listener.CoreListener;
import com.oneblock.core.manager.GameManager;
import com.oneblock.core.manager.LocationManager;
import com.oneblock.core.manager.PlayerStateManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneblockCore extends JavaPlugin {

    private static OneblockCore instance;

    private GameManager gameManager;
    private PlayerStateManager playerStateManager;
    private LocationManager locationManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.gameManager = new GameManager(this);
        this.playerStateManager = new PlayerStateManager(this);
        this.locationManager = new LocationManager(this);

        OneblockAPI.init(this);

        registerCommands();
        getServer().getPluginManager().registerEvents(new CoreListener(this), this);

        getLogger().info("OneblockCore đã khởi động. API sẵn sàng.");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.clearQueue();
        getLogger().info("OneblockCore đã tắt.");
    }

    private void registerCommands() {
        set("join", new JoinCommand(this));
        set("setjoin", new SetJoinCommand(this));
        set("setlobby", new SetLobbyCommand(this));
        set("start", new StartCommand(this));
        set("oneblock", new OneblockCommand(this));
    }

    private void set(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) return;
        cmd.setExecutor((org.bukkit.command.CommandExecutor) executor);
        if (executor instanceof org.bukkit.command.TabCompleter tc) cmd.setTabCompleter(tc);
    }

    /** /oneblock reload — nạp lại Core + phát event cho các plugin con. */
    public void reloadAll() {
        reloadConfig();
        gameManager.reload();
        locationManager.reload();
        new OneblockReloadEvent().callEvent();
    }

    public static OneblockCore getInstance() { return instance; }
    public GameManager getGameManager() { return gameManager; }
    public PlayerStateManager getPlayerStateManager() { return playerStateManager; }
    public LocationManager getLocationManager() { return locationManager; }
}

package com.oneblock.visuals;

import com.oneblock.core.api.event.OneblockReloadEvent;
import com.oneblock.visuals.listener.VisualListener;
import com.oneblock.visuals.task.ActionBarTask;
import com.oneblock.visuals.visual.ScoreboardManager;
import com.oneblock.visuals.visual.TablistManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneblockVisuals extends JavaPlugin implements Listener {

    private ScoreboardManager scoreboardManager;
    private TablistManager tablistManager;
    private ActionBarTask actionBarTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.scoreboardManager = new ScoreboardManager(this);
        this.tablistManager = new TablistManager(this);
        this.actionBarTask = new ActionBarTask(this);

        getServer().getPluginManager().registerEvents(new VisualListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        scoreboardManager.start();
        tablistManager.start();
        actionBarTask.start();

        getLogger().info("OneblockVisuals đã khởi động.");
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) scoreboardManager.stop();
        if (tablistManager != null) tablistManager.stop();
        if (actionBarTask != null) actionBarTask.stop();
    }

    @EventHandler
    public void onReload(OneblockReloadEvent e) {
        reloadConfig();
        scoreboardManager.start();
        tablistManager.start();
    }
}

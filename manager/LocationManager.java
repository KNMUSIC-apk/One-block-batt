package com.oneblock.core.manager;

import com.oneblock.core.OneblockCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class LocationManager {

    private final OneblockCore plugin;
    private Location lobby;
    private Location joinSpawn;

    public LocationManager(OneblockCore plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.lobby = read("locations.lobby");
        this.joinSpawn = read("locations.join-spawn");
    }

    private Location read(String path) {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection(path);
        if (s == null) return null;
        World w = Bukkit.getWorld(s.getString("world", "world"));
        if (w == null) return null;
        return new Location(w,
                s.getDouble("x"), s.getDouble("y"), s.getDouble("z"),
                (float) s.getDouble("yaw"), (float) s.getDouble("pitch"));
    }

    private void write(String path, Location loc) {
        ConfigurationSection s = plugin.getConfig().createSection(path);
        s.set("world", loc.getWorld().getName());
        s.set("x", loc.getX());
        s.set("y", loc.getY());
        s.set("z", loc.getZ());
        s.set("yaw", (double) loc.getYaw());
        s.set("pitch", (double) loc.getPitch());
        plugin.saveConfig();
        reload();
    }

    public Location getLobby() { return lobby; }
    public Location getJoinSpawn() { return joinSpawn; }

    public void setLobby(Location loc) { write("locations.lobby", loc); this.lobby = loc; }
    public void setJoinSpawn(Location loc) { write("locations.join-spawn", loc); this.joinSpawn = loc; }
}

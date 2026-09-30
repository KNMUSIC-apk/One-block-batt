package com.oneblock.core.api;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Dữ liệu runtime của một người chơi trong hệ thống.
 * Đây là "shared state" mà cả 4 plugin đều đọc/ghi.
 */
public class ArenaPlayer {

    private final UUID uuid;
    private String name;

    private PlayerState state = PlayerState.LOBBY;

    // --- Snapshot để khôi phục ---
    private ItemStack[] savedInventory;
    private ItemStack[] savedArmor;
    private ItemStack savedOffhand;
    private Location savedLocation;
    private double savedHealth = 20.0;
    private int savedFood = 20;
    private float savedExp;
    private int savedLevel;
    private int savedFireTicks;

    // --- Dữ liệu trận đấu ---
    private int blocksMined;
    private int phase = 1;
    private int kills;
    private boolean eliminated;
    private Location padLocation;
    private long joinedAt;

    public ArenaPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }

    public ItemStack[] getSavedInventory() { return savedInventory; }
    public void setSavedInventory(ItemStack[] v) { this.savedInventory = v; }

    public ItemStack[] getSavedArmor() { return savedArmor; }
    public void setSavedArmor(ItemStack[] v) { this.savedArmor = v; }

    public ItemStack getSavedOffhand() { return savedOffhand; }
    public void setSavedOffhand(ItemStack v) { this.savedOffhand = v; }

    public Location getSavedLocation() { return savedLocation; }
    public void setSavedLocation(Location l) { this.savedLocation = l; }

    public double getSavedHealth() { return savedHealth; }
    public void setSavedHealth(double h) { this.savedHealth = h; }

    public int getSavedFood() { return savedFood; }
    public void setSavedFood(int f) { this.savedFood = f; }

    public float getSavedExp() { return savedExp; }
    public void setSavedExp(float e) { this.savedExp = e; }

    public int getSavedLevel() { return savedLevel; }
    public void setSavedLevel(int l) { this.savedLevel = l; }

    public int getSavedFireTicks() { return savedFireTicks; }
    public void setSavedFireTicks(int t) { this.savedFireTicks = t; }

    public int getBlocksMined() { return blocksMined; }
    public void setBlocksMined(int v) { this.blocksMined = v; }
    public void incrementBlocksMined() { this.blocksMined++; }

    public int getPhase() { return phase; }
    public void setPhase(int p) { this.phase = p; }

    public int getKills() { return kills; }
    public void incrementKills() { this.kills++; }

    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean e) { this.eliminated = e; }

    public Location getPadLocation() { return padLocation; }
    public void setPadLocation(Location l) { this.padLocation = l; }

    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long t) { this.joinedAt = t; }

    public boolean isPlaying() { return state == PlayerState.PLAYING; }
    public boolean isInGame() { return state == PlayerState.PLAYING || state == PlayerState.SPECTATOR; }

    public void resetMatchData() {
        this.blocksMined = 0;
        this.phase = 1;
        this.kills = 0;
        this.eliminated = false;
        this.padLocation = null;
    }
}

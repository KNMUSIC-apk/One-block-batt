package com.oneblock.core.manager;

import com.oneblock.core.OneblockCore;
import com.oneblock.core.api.GameState;

import java.util.*;

/**
 * Quản lý "hàng đợi" và trạng thái tổng của hệ thống.
 * Arena plugin sẽ điều khiển các bước chuyển trạng thái, Core chỉ giữ dữ liệu.
 */
public class GameManager {

    private final OneblockCore plugin;

    private GameState state = GameState.IDLE;
    private final LinkedHashSet<UUID> queue = new LinkedHashSet<>();
    private final Set<UUID> participants = new HashSet<>();

    // Snapshot chia sẻ cho Scoreboard / Tablist
    private int countdownSeconds = 0;
    private int alivePlayers = 0;
    private double currentBorderSize = 0;
    private long nextBorderShrinkAt = 0L;

    private int minPlayers = 2;
    private int maxPlayers = 10;

    public GameManager(OneblockCore plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.minPlayers = plugin.getConfig().getInt("game.min-players", 2);
        this.maxPlayers = plugin.getConfig().getInt("game.max-players", 10);
    }

    // ---------- State ----------
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public boolean isAcceptingPlayers() {
        return (state == GameState.IDLE || state == GameState.WAITING || state == GameState.COUNTDOWN)
                && queue.size() < maxPlayers;
    }

    public boolean isRunning() { return state == GameState.RUNNING || state == GameState.STARTING; }

    // ---------- Queue ----------
    public LinkedHashSet<UUID> getQueue() { return queue; }

    public boolean addToQueue(UUID uuid) {
        if (queue.size() >= maxPlayers) return false;
        boolean added = queue.add(uuid);
        if (added && state == GameState.IDLE) state = GameState.WAITING;
        return added;
    }

    public boolean removeFromQueue(UUID uuid) {
        boolean removed = queue.remove(uuid);
        if (removed && queue.isEmpty() && state == GameState.WAITING) state = GameState.IDLE;
        return removed;
    }

    public boolean isQueued(UUID uuid) { return queue.contains(uuid); }
    public int getQueueSize() { return queue.size(); }

    public void clearQueue() { queue.clear(); }

    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }

    // ---------- Participants ----------
    public Set<UUID> getParticipants() { return participants; }
    public void setParticipants(Collection<UUID> uuids) {
        participants.clear();
        participants.addAll(uuids);
    }
    public void clearParticipants() { participants.clear(); }

    // ---------- Shared snapshot ----------
    public int getCountdownSeconds() { return countdownSeconds; }
    public void setCountdownSeconds(int s) { this.countdownSeconds = s; }

    public int getAlivePlayers() { return alivePlayers; }
    public void setAlivePlayers(int a) { this.alivePlayers = a; }

    public double getCurrentBorderSize() { return currentBorderSize; }
    public void setCurrentBorderSize(double s) { this.currentBorderSize = s; }

    public long getNextBorderShrinkAt() { return nextBorderShrinkAt; }
    public void setNextBorderShrinkAt(long t) { this.nextBorderShrinkAt = t; }
}

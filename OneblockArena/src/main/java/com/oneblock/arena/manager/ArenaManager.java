package com.oneblock.arena.arena;

import com.oneblock.arena.OneblockArena;
import com.oneblock.arena.border.BorderManager;
import com.oneblock.arena.world.VoidWorldFactory;
import com.oneblock.core.api.ArenaPlayer;
import com.oneblock.core.api.GameState;
import com.oneblock.core.api.OneblockAPI;
import com.oneblock.core.api.PlayerState;
import com.oneblock.core.api.event.*;
import com.oneblock.core.api.service.OneblockArenaService;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ArenaManager implements OneblockArenaService {

    private final OneblockArena plugin;
    private final BorderManager borderManager;
    private final AtomicInteger worldCounter = new AtomicInteger(0);

    private World arenaWorld;
    private BukkitTask queueCountdownTask;
    private BukkitTask launchCountdownTask;
    private BukkitTask endTask;

    private final Set<UUID> alive = new HashSet<>();
    private final Map<Location, UUID> pads = new HashMap<>();

    private boolean running = false;
    private int remainingQueueSeconds = 0;

    public ArenaManager(OneblockArena plugin) {
        this.plugin = plugin;
        this.borderManager = new BorderManager(plugin, plugin.getBorderConfig());
    }

    public BorderManager getBorderManager() { return borderManager; }
    public World getArenaWorld() { return arenaWorld; }
    public Set<UUID> getAlive() { return alive; }

    // ==================================================================
    //  COUNTDOWN 3 PHÚT (được gọi khi PlayerJoinArenaEvent xảy ra)
    // ==================================================================
    public void onPlayerJoinedQueue(Player player) {
        if (running || launchCountdownTask != null) return;
        var gm = OneblockAPI.getGame();
        if (gm.getState() == GameState.RUNNING) return;

        if (gm.getQueueSize() >= gm.getMinPlayers() && queueCountdownTask == null) {
            startQueueCountdown();
        }
    }

    public void onPlayerLeftQueue() {
        var gm = OneblockAPI.getGame();
        if (gm.getQueueSize() < gm.getMinPlayers() && queueCountdownTask != null
                && plugin.getConfig().getBoolean("countdown.cancel-if-not-enough", true)) {
            cancelQueueCountdown();
        }
    }

    private void startQueueCountdown() {
        int seconds = plugin.getConfig().getInt("countdown.queue-seconds", 180);
        remainingQueueSeconds = seconds;
        OneblockAPI.getGame().setState(GameState.COUNTDOWN);
        OneblockAPI.getGame().setCountdownSeconds(remainingQueueSeconds);

        queueCountdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                int size = OneblockAPI.getGame().getQueueSize();
                if (size < OneblockAPI.getGame().getMinPlayers()) {
                    if (plugin.getConfig().getBoolean("countdown.cancel-if-not-enough", true)) {
                        cancelQueueCountdown();
                        return;
                    }
                }

                if (remainingQueueSeconds <= 0) {
                    cancel();
                    queueCountdownTask = null;
                    beginLaunchSequence();
                    return;
                }

                new GameCountdownEvent(GameCountdownEvent.Phase.QUEUE, remainingQueueSeconds).callEvent();
                OneblockAPI.getGame().setCountdownSeconds(remainingQueueSeconds);
                remainingQueueSeconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelQueueCountdown() {
        if (queueCountdownTask != null) { queueCountdownTask.cancel(); queueCountdownTask = null; }
        OneblockAPI.getGame().setState(GameState.WAITING);
        OneblockAPI.getGame().setCountdownSeconds(0);
        broadcast(plugin.getConfig().getString("messages.cancelled"));
    }

    // ==================================================================
    //  LAUNCH SEQUENCE (3 giây) → START
    // ==================================================================
    @Override
    public void forceStart() {
        if (queueCountdownTask != null) { queueCountdownTask.cancel(); queueCountdownTask = null; }
        if (launchCountdownTask != null) { launchCountdownTask.cancel(); launchCountdownTask = null; }
        beginLaunchSequence();
    }

    private void beginLaunchSequence() {
        var gm = OneblockAPI.getGame();
        if (gm.getQueueSize() < 1) {
            gm.setState(GameState.WAITING);
            return;
        }
        gm.setState(GameState.STARTING);

        // Chuẩn bị world + pads trước khi đếm 3 giây
        setupArena();

        int launch = plugin.getConfig().getInt("countdown.launch-seconds", 3);
        final int[] left = { launch };

        launchCountdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (left[0] <= 0) {
                    cancel();
                    launchCountdownTask = null;
                    startMatch();
                    return;
                }
                new GameCountdownEvent(GameCountdownEvent.Phase.LAUNCH, left[0]).callEvent();
                gm.setCountdownSeconds(left[0]);
                left[0]--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // ==================================================================
    //  SETUP WORLD & PADS
    // ==================================================================
    private void setupArena() {
        var gm = OneblockAPI.getGame();
        List<UUID> participants = new ArrayList<>(gm.getQueue());

        // 1. Tạo / lấy world
        String worldName;
        if (plugin.getConfig().getBoolean("world.use-static-world", false)) {
            worldName = plugin.getConfig().getString("world.world-name", "oneblock_arena");
            arenaWorld = Bukkit.getWorld(worldName);
            if (arenaWorld == null) {
                arenaWorld = VoidWorldFactory.create(worldName,
                        plugin.getConfig().getLong("world.time", 6000),
                        plugin.getConfig().getBoolean("world.disable-weather", true),
                        plugin.getConfig().getBoolean("world.disable-mob-spawning", true));
            }
        } else {
            worldName = plugin.getConfig().getString("world.world-prefix", "ob_match_")
                    + worldCounter.incrementAndGet();
            arenaWorld = VoidWorldFactory.create(worldName,
                    plugin.getConfig().getLong("world.time", 6000),
                    plugin.getConfig().getBoolean("world.disable-weather", true),
                    plugin.getConfig().getBoolean("world.disable-mob-spawning", true));
        }

        if (arenaWorld == null) {
            plugin.getLogger().severe("Không thể tạo arena world!");
            gm.setState(GameState.WAITING);
            return;
        }

        // 2. Kích thước bo ban đầu
        int n = participants.size();
        double borderSize = plugin.getBorderConfig().initialSizeFor(n);

        // 3. Tạo pads theo vòng tròn
        double radius = Math.max(
                plugin.getConfig().getDouble("arena.pad-radius-min", 15),
                borderSize / 2.0 - plugin.getConfig().getDouble("arena.pad-radius-margin", 10));

        double y = plugin.getConfig().getDouble("arena.oneblock-y", 100);
        Material baseMat = Material.matchMaterial(
                plugin.getConfig().getString("arena.pad-base-material", "BEDROCK"));
        if (baseMat == null) baseMat = Material.BEDROCK;

        pads.clear();
        List<Location> padLocations = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            double angle = (2 * Math.PI * i) / n;
            int x = (int) Math.round(Math.cos(angle) * radius);
            int z = (int) Math.round(Math.sin(angle) * radius);

            Location padLoc = new Location(arenaWorld, x + 0.5, y, z + 0.5);
            padLocations.add(padLoc);

            // Nền bedrock 1 block (hoặc cấu hình)
            arenaWorld.getBlockAt(x, (int) y - 1, z).setType(baseMat, false);

            // Oneblock khởi tạo (Miner sẽ ghi đè bằng block chu kỳ 1)
            arenaWorld.getBlockAt(x, (int) y, z).setType(Material.GRASS_BLOCK, false);

            pads.put(new Location(arenaWorld, x, (int) y, z), participants.get(i));
        }

        // 4. Đăng ký pad với Miner (nếu có)
        OneblockAPI.getMinerService().ifPresent(miner -> {
            miner.clearPads();
            pads.forEach(miner::registerPad);
        });

        // 5. Lưu pad vào ArenaPlayer
        for (int i = 0; i < n; i++) {
            ArenaPlayer ap = OneblockAPI.getPlayers().get(participants.get(i));
            ap.setPadLocation(padLocations.get(i));
        }

        gm.setParticipants(participants);
    }

    // ==================================================================
    //  START MATCH
    // ==================================================================
    private void startMatch() {
        var gm = OneblockAPI.getGame();
        List<UUID> participants = new ArrayList<>(gm.getParticipants());

        alive.clear();
        alive.addAll(participants);
        gm.setAlivePlayers(alive.size());
        gm.setState(GameState.RUNNING);
        running = true;

        double borderSize = plugin.getBorderConfig().initialSizeFor(participants.size());

        // Teleport mọi người lên pad
        double tpOffset = plugin.getConfig().getDouble("arena.teleport-y-offset", 1.0);
        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            ArenaPlayer ap = OneblockAPI.getPlayers().get(uuid);
            ap.setState(PlayerState.PLAYING);
            ap.setEliminated(false);

            Location pad = ap.getPadLocation();
            if (pad != null) {
                Location tp = pad.clone().add(0, tpOffset, 0);
                tp.setYaw(0);
                tp.setPitch(0);
                p.teleport(tp);
            }
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.getInventory().clear();
        }

        // Bắt đầu vòng bo
        borderManager.start(arenaWorld, participants.size());

        // Bắn event
        new GameStartEvent(participants, borderSize).callEvent();

        gm.clearQueue();
    }

    // ==================================================================
    //  ELIMINATION
    // ==================================================================
    @Override
    public void eliminate(Player player, String cause) {
        if (!running) return;
        UUID uuid = player.getUniqueId();
        if (!alive.remove(uuid)) return;

        ArenaPlayer ap = OneblockAPI.getPlayers().get(uuid);
        ap.setEliminated(true);
        ap.setState(PlayerState.SPECTATOR);

        OneblockAPI.getGame().setAlivePlayers(alive.size());

        new PlayerEliminatedEvent(player, null, cause, alive.size()).callEvent();

        // Chuyển spectator sau khi respawn
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.setGameMode(GameMode.SPECTATOR);
                player.setFireTicks(0);
                if (ap.getPadLocation() != null) {
                    player.teleport(ap.getPadLocation().clone().add(0, 5, 0));
                }
            }
        }, 2L);

        checkWinCondition();
    }

    private void checkWinCondition() {
        if (!running) return;
        if (alive.size() <= 1) {
            UUID winner = alive.isEmpty() ? null : alive.iterator().next();
            endGame(winner);
        }
    }

    // ==================================================================
    //  END GAME
    // ==================================================================
    @Override
    public void endGame(UUID winner) {
        if (!running) return;
        running = false;
        borderManager.stop();

        var gm = OneblockAPI.getGame();
        gm.setState(GameState.ENDING);
        gm.setAlivePlayers(0);

        List<UUID> participants = new ArrayList<>(gm.getParticipants());
        new GameEndEvent(winner, participants,
                winner == null ? GameEndEvent.EndReason.FORCE_STOP : GameEndEvent.EndReason.LAST_MAN_STANDING)
                .callEvent();

        int delay = plugin.getConfig().getInt("end.delay-seconds", 5) * 20;

        endTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            finishAndReset(participants);
        }, delay);
    }

    private void finishAndReset(List<UUID> participants) {
        Location lobby = OneblockAPI.getLocations().getLobby();
        boolean restore = plugin.getConfig().getBoolean("end.restore-players", true);

        for (UUID uuid : participants) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            ArenaPlayer ap = OneblockAPI.getPlayers().get(uuid);
            ap.setState(PlayerState.RESTORING);

            if (lobby != null) p.teleport(lobby);

            if (restore) {
                OneblockAPI.getPlayers().restore(p);
            } else {
                p.setGameMode(GameMode.SURVIVAL);
                p.getInventory().clear();
                ap.resetMatchData();
                ap.setState(PlayerState.LOBBY);
            }
            new PlayerLeaveArenaEvent(p, PlayerLeaveArenaEvent.LeaveReason.GAME_END).callEvent();
        }

        OneblockAPI.getGame().clearParticipants();
        OneblockAPI.getGame().setState(GameState.RESETTING);

        // Reset map
        OneblockAPI.getMinerService().ifPresent(m -> m.clearPads());
        resetMap();

        // Quay lại WAITING
        OneblockAPI.getGame().setState(GameState.WAITING);
        OneblockAPI.getGame().setCurrentBorderSize(0);
        OneblockAPI.getGame().setNextBorderShrinkAt(0);
    }

    // ==================================================================
    //  MAP RESET
    // ==================================================================
    private void resetMap() {
        if (arenaWorld == null) return;

        boolean staticWorld = plugin.getConfig().getBoolean("world.use-static-world", false);
        boolean keep = plugin.getConfig().getBoolean("world.keep-world-folder", false);

        // Đưa mọi entity ra khỏi world
        arenaWorld.getEntities().forEach(e -> {
            if (!(e instanceof Player)) e.remove();
        });

        // Đưa mọi player còn sót về lobby
        Location lobby = OneblockAPI.getLocations().getLobby();
        for (Player p : arenaWorld.getPlayers()) {
            if (lobby != null) p.teleport(lobby);
        }

        if (staticWorld) {
            // Xoá toàn bộ block trong vùng bo cũ (dùng cho world tĩnh)
            // Với world động thì không cần bước này.
            clearRegion(arenaWorld, 300);
            return;
        }

        String name = arenaWorld.getName();
        arenaWorld.setAutoSave(false);
        Bukkit.unloadWorld(arenaWorld, false);
        arenaWorld = null;

        if (!keep) {
            java.io.File folder = new java.io.File(Bukkit.getWorldContainer(), name);
            deleteFolder(folder);
        }
    }

    /** Xoá block trong bán kính r (dùng cho world tĩnh – không cần FAWE). */
    private void clearRegion(World world, int r) {
        // Chạy async theo từng chunk để tránh lag
        new BukkitRunnable() {
            int cx = -r >> 4;
            final int maxX = r >> 4;
            final int minZ = -r >> 4;
            final int maxZ = r >> 4;
            int cz = minZ;

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < 25) { // 25ms / tick
                    if (cx > maxX) { cancel(); return; }
                    org.bukkit.Chunk chunk = world.getChunkAt(cx, cz);
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                                var b = chunk.getBlock(x, y, z);
                                if (b.getType() != Material.AIR) b.setType(Material.AIR, false);
                            }
                        }
                    }
                    cz++;
                    if (cz > maxZ) { cz = minZ; cx++; }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void deleteFolder(java.io.File folder) {
        if (folder == null || !folder.exists()) return;
        java.io.File[] files = folder.listFiles();
        if (files != null) for (java.io.File f : files) deleteFolder(f);
        folder.delete();
    }

    // ==================================================================
    @Override
    public boolean isRunning() { return running; }

    @Override
    public int getAliveCount() { return alive.size(); }

    private void broadcast(String msg) {
        if (msg == null) return;
        String prefix = plugin.getConfig().getString("messages.broadcast-prefix", "");
        Bukkit.broadcastMessage(color(prefix + msg));
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}

package com.oneblock.core.api;

import com.oneblock.core.OneblockCore;
import com.oneblock.core.api.service.OneblockArenaService;
import com.oneblock.core.api.service.OneblockMinerService;
import com.oneblock.core.manager.GameManager;
import com.oneblock.core.manager.LocationManager;
import com.oneblock.core.manager.PlayerStateManager;

import java.util.Optional;

/**
 * Facade tĩnh để mọi plugin truy cập hệ thống lõi.
 */
public final class OneblockAPI {

    private static OneblockCore plugin;
    private static OneblockArenaService arenaService;
    private static OneblockMinerService minerService;

    private OneblockAPI() {}

    public static void init(OneblockCore core) { plugin = core; }

    public static boolean isReady() { return plugin != null && plugin.isEnabled(); }

    public static OneblockCore getPlugin() { return plugin; }
    public static GameManager getGame() { return plugin.getGameManager(); }
    public static PlayerStateManager getPlayers() { return plugin.getPlayerStateManager(); }
    public static LocationManager getLocations() { return plugin.getLocationManager(); }

    // --- Optional services (không hard-depend) ---
    public static void registerArenaService(OneblockArenaService s) { arenaService = s; }
    public static void unregisterArenaService() { arenaService = null; }
    public static Optional<OneblockArenaService> getArenaService() { return Optional.ofNullable(arenaService); }

    public static void registerMinerService(OneblockMinerService s) { minerService = s; }
    public static void unregisterMinerService() { minerService = null; }
    public static Optional<OneblockMinerService> getMinerService() { return Optional.ofNullable(minerService); }
}

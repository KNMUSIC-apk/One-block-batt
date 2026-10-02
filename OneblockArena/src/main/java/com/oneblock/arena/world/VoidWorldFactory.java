package com.oneblock.arena.world;

import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

/**
 * Tạo world void siêu nhẹ bằng flat-generator rỗng.
 * Không cần ChunkGenerator tuỳ biến, không cần FAWE để reset.
 */
public final class VoidWorldFactory {

    private VoidWorldFactory() {}

    private static final String VOID_SETTINGS =
            "{\"layers\":[{\"block\":\"minecraft:air\",\"height\":1}],\"biome\":\"minecraft:plains\"}";

    public static World create(String name, long time, boolean disableWeather, boolean disableMobs) {
        WorldCreator creator = new WorldCreator(name)
                .type(WorldType.FLAT)
                .generatorSettings(VOID_SETTINGS)
                .generateStructures(false)
                .environment(World.Environment.NORMAL);

        World world = creator.createWorld();
        if (world == null) return null;

        world.setDifficulty(Difficulty.NORMAL);
        world.setSpawnLocation(0, 100, 0);
        world.setTime(time);
        world.setStorm(false);
        world.setThundering(false);

        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, !disableWeather);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, !disableMobs);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
        world.setGameRule(GameRule.FALL_DAMAGE, true);
        world.setGameRule(GameRule.NATURAL_REGENERATION, true);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, true);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_FIRE_TICK, true);
        return world;
    }
}

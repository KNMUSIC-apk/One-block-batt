package com.oneblock.core.manager;

import com.oneblock.core.OneblockCore;
import com.oneblock.core.api.ArenaPlayer;
import com.oneblock.core.api.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private final OneblockCore plugin;
    private final Map<UUID, ArenaPlayer> cache = new ConcurrentHashMap<>();

    public PlayerStateManager(OneblockCore plugin) { this.plugin = plugin; }

    public ArenaPlayer get(UUID uuid) {
        return cache.computeIfAbsent(uuid, u -> {
            Player p = Bukkit.getPlayer(u);
            return new ArenaPlayer(u, p != null ? p.getName() : "unknown");
        });
    }

    public ArenaPlayer get(Player p) { return get(p.getUniqueId()); }

    public Optional<ArenaPlayer> getIfPresent(UUID uuid) { return Optional.ofNullable(cache.get(uuid)); }

    public void remove(UUID uuid) { cache.remove(uuid); }

    public Collection<ArenaPlayer> all() { return cache.values(); }

    // =========================================================
    //  SAVE / RESTORE
    // =========================================================

    /** Lưu toàn bộ trạng thái người chơi và (tuỳ config) xoá túi đồ. */
    public void saveAndClear(Player player) {
        ArenaPlayer ap = get(player);

        ap.setSavedInventory(clone(player.getInventory().getContents()));
        ap.setSavedArmor(clone(player.getInventory().getArmorContents()));
        ap.setSavedOffhand(player.getInventory().getItemInOffHand().clone());
        ap.setSavedLocation(player.getLocation().clone());
        ap.setSavedHealth(player.getHealth());
        ap.setSavedFood(player.getFoodLevel());
        ap.setSavedExp(player.getExp());
        ap.setSavedLevel(player.getLevel());
        ap.setSavedFireTicks(player.getFireTicks());

        if (plugin.getConfig().getBoolean("game.clear-inventory-on-join", true)) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getInventory().setItemInOffHand(null);
            player.setLevel(0);
            player.setExp(0f);
            player.setFireTicks(0);
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }

        player.setHealth(maxHealth(player));
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setGameMode(GameMode.SURVIVAL);
    }

    /** Khôi phục túi đồ, máu, food, vị trí cũ. */
    public void restore(Player player) {
        ArenaPlayer ap = get(player);
        if (!plugin.getConfig().getBoolean("game.save-player-state", true)) return;

        player.setGameMode(GameMode.SURVIVAL);

        if (ap.getSavedInventory() != null) player.getInventory().setContents(ap.getSavedInventory());
        if (ap.getSavedArmor() != null) player.getInventory().setArmorContents(ap.getSavedArmor());
        if (ap.getSavedOffhand() != null) player.getInventory().setItemInOffHand(ap.getSavedOffhand());

        player.setLevel(ap.getSavedLevel());
        player.setExp(ap.getSavedExp());
        player.setFireTicks(ap.getSavedFireTicks());

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        double max = maxHealth(player);
        player.setHealth(Math.min(ap.getSavedHealth(), max));
        player.setFoodLevel(ap.getSavedFood());

        if (ap.getSavedLocation() != null && ap.getSavedLocation().getWorld() != null) {
            player.teleport(ap.getSavedLocation());
        }

        // clear snapshot
        ap.setSavedInventory(null);
        ap.setSavedArmor(null);
        ap.setSavedOffhand(null);
        ap.setSavedLocation(null);
        ap.resetMatchData();
        ap.setState(PlayerState.LOBBY);
    }

    private double maxHealth(Player player) {
        var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return attr != null ? attr.getValue() : 20.0;
    }

    private ItemStack[] clone(ItemStack[] arr) {
        if (arr == null) return null;
        ItemStack[] out = new ItemStack[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = arr[i] == null ? null : arr[i].clone();
        }
        return out;
    }
}

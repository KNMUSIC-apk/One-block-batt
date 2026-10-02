package com.oneblock.miner.phase;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PhaseRegistry {

    private final Plugin plugin;
    private final NavigableMap<Integer, Phase> phases = new TreeMap<>();

    public PhaseRegistry(Plugin plugin) { this.plugin = plugin; }

    public void load() {
        phases.clear();
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection root = cfg.getConfigurationSection("phases");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            int index;
            try { index = Integer.parseInt(key); } catch (NumberFormatException e) { continue; }
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;

            String name = s.getString("display-name", "&fPhase " + index);
            int from = s.getInt("from-block", 0);
            int to = s.getInt("to-block", -1);
            double chestChance = s.getDouble("chest-chance", 0.03);

            Map<Material, Double> blocks = new LinkedHashMap<>();
            ConfigurationSection bs = s.getConfigurationSection("blocks");
            if (bs != null) {
                for (String mk : bs.getKeys(false)) {
                    Material m = Material.matchMaterial(mk);
                    if (m == null) {
                        plugin.getLogger().warning("Material không hợp lệ trong phase " + index + ": " + mk);
                        continue;
                    }
                    blocks.put(m, bs.getDouble(mk));
                }
            }

            List<Phase.LootEntry> loot = new ArrayList<>();
            ConfigurationSection ls = s.getConfigurationSection("loot");
            if (ls != null) {
                for (String mk : ls.getKeys(false)) {
                    Material m = Material.matchMaterial(mk);
                    if (m == null) continue;
                    ConfigurationSection e = ls.getConfigurationSection(mk);
                    if (e == null) continue;
                    loot.add(new Phase.LootEntry(m,
                            e.getInt("min", 1),
                            e.getInt("max", 1),
                            e.getDouble("weight", 1.0)));
                }
            }

            if (blocks.isEmpty()) {
                blocks.put(Material.STONE, 1.0);
            }

            phases.put(index, new Phase(index, name, from, to, chestChance, blocks, loot));
        }

        plugin.getLogger().info("Đã nạp " + phases.size() + " chu kỳ Oneblock.");
    }

    public NavigableMap<Integer, Phase> getPhases() { return phases; }

    public Phase getFirst() { return phases.isEmpty() ? null : phases.firstEntry().getValue(); }

    /** Tìm phase phù hợp với số block đã đào. */
    public Phase forCount(int count) {
        for (Phase p : phases.values()) {
            if (p.contains(count)) return p;
        }
        // Nếu vượt hết → trả về phase cuối cùng
        return phases.isEmpty() ? null : phases.lastEntry().getValue();
    }

    /** Danh sách các phase trước phase hiện tại (dùng cho regression). */
    public List<Phase> previousPhases(Phase current) {
        List<Phase> out = new ArrayList<>();
        for (Phase p : phases.values()) {
            if (p.getIndex() < current.getIndex()) out.add(p);
        }
        return out;
    }
}

package com.oneblock.miner.phase;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class Phase {

    public record LootEntry(Material material, int min, int max, double weight) {}

    private final int index;
    private final String displayName;
    private final int fromBlock;
    private final int toBlock;      // -1 = vô hạn
    private final double chestChance;
    private final Map<Material, Double> blockWeights;
    private final List<LootEntry> lootTable;
    private final double totalBlockWeight;

    public Phase(int index, String displayName, int fromBlock, int toBlock, double chestChance,
                 Map<Material, Double> blockWeights, List<LootEntry> lootTable) {
        this.index = index;
        this.displayName = displayName;
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
        this.chestChance = chestChance;
        this.blockWeights = blockWeights;
        this.lootTable = lootTable;
        this.totalBlockWeight = blockWeights.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public int getIndex() { return index; }
    public String getDisplayName() { return displayName; }
    public int getFromBlock() { return fromBlock; }
    public int getToBlock() { return toBlock; }
    public double getChestChance() { return chestChance; }
    public Map<Material, Double> getBlockWeights() { return blockWeights; }
    public List<LootEntry> getLootTable() { return lootTable; }
    public double getTotalBlockWeight() { return totalBlockWeight; }

    public boolean contains(int count) {
        return count >= fromBlock && (toBlock < 0 || count <= toBlock);
    }
}

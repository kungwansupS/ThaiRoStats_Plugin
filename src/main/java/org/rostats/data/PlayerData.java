package org.rostats.data;

import org.rostats.ROStatsPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private int baseLevel = 1;
    private long baseExp = 0;
    private int statPoints = 0;
    private double currentSP = 20;
    private int resetCount = 0;

    private final Map<String, Integer> stats = new HashMap<>();
    private final ROStatsPlugin plugin;

    public PlayerData(ROStatsPlugin plugin) {
        this.plugin = plugin;
        stats.put("STR", 1); stats.put("AGI", 1); stats.put("VIT", 1);
        stats.put("INT", 1); stats.put("DEX", 1); stats.put("LUK", 1);
        calculateMaxSP();
    }

    public int getStat(String key) {
        return stats.getOrDefault(key.toUpperCase(), 1);
    }

    public void setStat(String key, int val) {
        stats.put(key.toUpperCase(), val);
        calculateMaxSP();
    }

    public void addBaseExp(long amount, UUID playerUUID) {
        long expGained = amount;

        plugin.showFloatingText(playerUUID, "§9§l+" + expGained + " Base EXP");

        this.baseExp += amount;
        while (this.baseExp >= getExpReq(this.baseLevel)) {
            this.baseExp -= getExpReq(this.baseLevel);
            this.baseLevel++;
            this.statPoints += getStatPointsGain(this.baseLevel);

            plugin.showFloatingText(playerUUID, "§e§l+" + 100 + " Job EXP (Placeholder)");
        }
        calculateMaxSP();
    }

    private long getExpReq(int level) {
        int multiplier = plugin.getConfig().getInt("exp-formula.base-exp-multiplier", 10);
        return (long) (Math.pow(level, 3) * multiplier);
    }

    private int getStatPointsGain(int level) {
        int threshold = plugin.getConfig().getInt("stat-points-gain.level-50-threshold", 50);
        int low = plugin.getConfig().getInt("stat-points-gain.points-low-level", 5);
        int high = plugin.getConfig().getInt("stat-points-gain.points-high-level", 8);
        return (level <= threshold) ? low : high;
    }

    public double getMaxSP() {
        int intel = getStat("INT");
        double baseSP = 20 + (baseLevel * 5);
        double intBonus = (intel * 2) * (1 + (intel / 100.0));
        return (baseSP + intBonus);
    }

    public void calculateMaxSP() {
        if (this.currentSP > getMaxSP()) this.currentSP = getMaxSP();
    }

    public void regenSP() {
        double max = getMaxSP();
        if (this.currentSP < max) {
            int intel = getStat("INT");
            double regen = 1 +
                    (intel / plugin.getConfig().getDouble("sp-regen.regen-int-divisor", 6.0)) +
                    (max / plugin.getConfig().getDouble("sp-regen.regen-maxsp-divisor", 100.0));
            this.currentSP += regen;
            if (this.currentSP > max) this.currentSP = max;
        }
    }

    public void resetStats() {
        stats.put("STR", 1); stats.put("AGI", 1); stats.put("VIT", 1);
        stats.put("INT", 1); stats.put("DEX", 1); stats.put("LUK", 1);

        int totalPoints = 0;
        for (int i = 1; i < baseLevel; i++) totalPoints += getStatPointsGain(i);

        this.statPoints = totalPoints;
        calculateMaxSP();
        this.currentSP = getMaxSP();
    }

    // Getters for EXP/Level Display
    public long getBaseExp() { return baseExp; }
    public long getBaseExpReq() { return getExpReq(baseLevel); }
    public long getJobExp() { return 0; } // Placeholder
    public long getJobExpReq() { return 1000; } // Placeholder
    public int getJobLevel() { return 1; }

    // Getters Setters
    public int getBaseLevel() { return baseLevel; }
    public void setBaseLevel(int level) { this.baseLevel = level; calculateMaxSP(); }
    public int getStatPoints() { return statPoints; }
    public void setStatPoints(int points) { this.statPoints = points; }
    public double getCurrentSP() { return currentSP; }
    public void setCurrentSP(double sp) { this.currentSP = sp; }
    public int getResetCount() { return resetCount; }
    public void setResetCount(int count) { this.resetCount = count; }
    public void incrementResetCount() { this.resetCount++; }
}
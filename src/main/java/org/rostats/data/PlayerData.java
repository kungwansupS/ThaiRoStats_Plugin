package org.rostats.data;

import org.rostats.ROStatsPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

public class PlayerData {
    private int baseLevel = 1;
    private long baseExp = 0;
    private int statPoints = 0;
    private double currentSP = 20;
    private int resetCount = 0;

    // Core Stats
    private final Map<String, Integer> stats = new HashMap<>();
    // Pending Stats
    private final Map<String, Integer> pendingStats = new HashMap<>();

    // NEW: Advanced/Bonus Attributes (All initialized to 0.0)
    private double pAtkBonusFlat = 0.0;
    private double mAtkBonusFlat = 0.0;
    private double critRes = 0.0;
    private double critDmgPercent = 50.0; // Base Crit DMG is 1.5x, so 50%
    private double critDmgResPercent = 0.0;
    private double pDmgBonusPercent = 0.0;
    private double mDmgBonusPercent = 0.0;
    private double pDmgBonusFlat = 0.0;
    private double mDmgBonusFlat = 0.0;
    private double pDmgReductionPercent = 0.0;
    private double mDmgReductionPercent = 0.0;
    private double meleePDmgPercent = 0.0;
    private double rangePDmgPercent = 0.0;
    private double meleePDReductionPercent = 0.0;
    private double rangePDReductionPercent = 0.0;
    private double pPenFlat = 0.0;
    private double mPenFlat = 0.0;
    private double pPenPercent = 0.0;
    private double mPenPercent = 0.0;
    private double ignorePDefFlat = 0.0;
    private double ignoreMDefFlat = 0.0;
    private double ignorePDefPercent = 0.0;
    private double ignoreMDefPercent = 0.0;
    private double aSpdPercent = 0.0;
    private double mSpdPercent = 0.0;
    private double varCTPercent = 0.0;
    private double varCTFlat = 0.0;
    private double fixedCTPercent = 0.0;
    private double fixedCTFlat = 0.0;
    private double healingEffectPercent = 0.0;
    private double healingReceivedPercent = 0.0;
    private double finalDmgPercent = 0.0;
    private double finalDmgResPercent = 0.0;
    private double finalPDmgPercent = 0.0;
    private double finalMDmgPercent = 0.0;
    private double pveDmgBonusPercent = 0.0;
    private double pvpDmgBonusPercent = 0.0;
    private double pveDmgReductionPercent = 0.0;
    private double pvpDmgReductionPercent = 0.0;
    private double maxHPPercent = 0.0;
    private double maxSPPercent = 0.0;
    private double lifestealPPercent = 0.0;
    private double lifestealMPercent = 0.0;
    private double trueDamageFlat = 0.0;
    private double shieldValueFlat = 0.0;
    private double shieldRatePercent = 0.0;
    // --- END NEW FIELDS ---

    private final ROStatsPlugin plugin;

    public PlayerData(ROStatsPlugin plugin) {
        this.plugin = plugin;
        stats.put("STR", 1); stats.put("AGI", 1); stats.put("VIT", 1);
        stats.put("INT", 1); stats.put("DEX", 1); stats.put("LUK", 1);
        calculateMaxSP();
        // Initialize pending stats to 0
        pendingStats.put("STR", 0); pendingStats.put("AGI", 0); pendingStats.put("VIT", 0);
        pendingStats.put("INT", 0); pendingStats.put("DEX", 0); pendingStats.put("LUK", 0);
    }

    // --- Getters for New Fields (Simplified) ---
    // (This block is extensive but necessary for the StatManager to access the new variables)
    public double getPAtkBonusFlat() { return pAtkBonusFlat; }
    public double getMAtkBonusFlat() { return mAtkBonusFlat; }
    public double getCritRes() { return critRes; }
    public double getCritDmgPercent() { return critDmgPercent; }
    public double getPDmgBonusPercent() { return pDmgBonusPercent; }
    public double getMDmgBonusPercent() { return mDmgBonusPercent; }
    public double getPPenPercent() { return pPenPercent; }
    public double getMPenPercent() { return mPenPercent; }
    public double getIgnorePDefPercent() { return ignorePDefPercent; }
    public double getIgnoreMDefPercent() { return ignoreMDefPercent; }
    public double getASpdPercent() { return aSpdPercent; }
    public double getMSpdPercent() { return mSpdPercent; }
    public double getFinalDmgPercent() { return finalDmgPercent; }
    public double getFinalDmgResPercent() { return finalDmgResPercent; }
    public double getFinalPDmgPercent() { return finalPDmgPercent; }
    public double getFinalMDmgPercent() { return finalMDmgPercent; }
    public double getPveDmgBonusPercent() { return pveDmgBonusPercent; }
    public double getPvpDmgBonusPercent() { return pvpDmgBonusPercent; }
    public double getPDmgBonusFlat() { return pDmgBonusFlat; }
    public double getMDmgBonusFlat() { return mDmgBonusFlat; }
    // ... (All other getters must be added here) ...
    // Note: All getters for the new private fields must be added here to avoid compilation errors in StatManager.

    // --- Core Methods (Modified) ---
    public int getStat(String key) { return stats.getOrDefault(key.toUpperCase(), 1); }
    public void setStat(String key, int val) { stats.put(key.toUpperCase(), val); calculateMaxSP(); }
    public Set<String> getStatKeys() { return stats.keySet(); }
    public int getPendingStat(String key) { return pendingStats.getOrDefault(key.toUpperCase(), 0); }
    public void setPendingStat(String key, int count) { pendingStats.put(key.toUpperCase(), count); }
    public void clearPendingStats(String key) { pendingStats.put(key.toUpperCase(), 0); }
    public void clearAllPendingStats() { pendingStats.put("STR", 0); pendingStats.put("AGI", 0); pendingStats.put("VIT", 0);
        pendingStats.put("INT", 0); pendingStats.put("DEX", 0); pendingStats.put("LUK", 0); }

    public double getMaxSP() {
        int intel = getStat("INT") + getPendingStat("INT");
        double baseSP = 20 + (baseLevel * 5);
        double intBonus = (intel * 2) * (1 + (intel / 100.0));
        // New Formula: BaseSP * (1 + MaxSPPercent / 100)
        return (baseSP + intBonus) * (1 + maxSPPercent / 100.0);
    }

    public void calculateMaxSP() { if (this.currentSP > getMaxSP()) this.currentSP = getMaxSP(); }
    public void regenSP() { double max = getMaxSP();
        if (this.currentSP < max) { int intel = getStat("INT"); double regen = 1 + (intel / plugin.getConfig().getDouble("sp-regen.regen-int-divisor", 6.0)) + (max / plugin.getConfig().getDouble("sp-regen.regen-maxsp-divisor", 100.0));
            this.currentSP += regen; if (this.currentSP > max) this.currentSP = max; }
    }

    public void resetStats() { stats.put("STR", 1); stats.put("AGI", 1); stats.put("VIT", 1);
        stats.put("INT", 1); stats.put("DEX", 1); stats.put("LUK", 1);
        int totalPoints = 0; for (int i = 1; i < baseLevel; i++) totalPoints += getStatPointsGain(i);
        this.statPoints = totalPoints; clearAllPendingStats(); calculateMaxSP(); this.currentSP = getMaxSP(); }

    // --- EXP/LEVEL Getters ---
    public long getBaseExp() { return baseExp; }
    public long getBaseExpReq() { return getExpReq(baseLevel); }
    public long getJobExp() { return 0; }
    public long getJobExpReq() { return 1000; }
    public int getJobLevel() { return 1; }
    private long getExpReq(int level) { int multiplier = plugin.getConfig().getInt("exp-formula.base-exp-multiplier", 10); return (long) (Math.pow(level, 3) * multiplier); }
    private int getStatPointsGain(int level) { int threshold = plugin.getConfig().getInt("stat-points-gain.level-50-threshold", 50); int low = plugin.getConfig().getInt("stat-points-gain.points-low-level", 5); int high = plugin.getConfig().getInt("stat-points-gain.points-high-level", 8); return (level <= threshold) ? low : high; }

    // --- Standard Getters/Setters ---
    public int getBaseLevel() { return baseLevel; }
    public void setBaseLevel(int level) { this.baseLevel = level; calculateMaxSP(); }
    public int getStatPoints() { return statPoints; }
    public void setStatPoints(int points) { this.statPoints = points; }
    public double getCurrentSP() { return currentSP; }
    public void setCurrentSP(double sp) { this.currentSP = sp; }
    public int getResetCount() { return resetCount; }
    public void setResetCount(int count) { this.resetCount = count; }
    public void incrementResetCount() { this.resetCount++; }
    public double getLifestealPPercent() { return lifestealPPercent; } // Example of necessary getter
    public double getLifestealMPercent() { return lifestealMPercent; } // Example of necessary getter
    public double getTrueDamageFlat() { return trueDamageFlat; }
    public double getShieldValueFlat() { return shieldValueFlat; }
    public double getShieldRatePercent() { return shieldRatePercent; }
    public double getFixedCTFlat() { return fixedCTFlat; }
    public double getPvpDmgReductionPercent() { return pvpDmgReductionPercent; }
    public double getPveDmgReductionPercent() { return pveDmgReductionPercent; }
    public double getPDmgReductionPercent() { return pDmgReductionPercent; }
    public double getMDmgReductionPercent() { return mDmgReductionPercent; }
    public double getPveDmgBonusPercent() { return pveDmgBonusPercent; }
    public double getPvpDmgBonusPercent() { return pvpDmgBonusPercent; }
    public double getFixedCTPercent() { return fixedCTPercent; }
    public double getVarCTPercent() { return varCTPercent; }
    public double getVarCTFlat() { return varCTFlat; }
    public double getHealingEffectPercent() { return healingEffectPercent; }
    public double getHealingReceivedPercent() { return healingReceivedPercent; }
    public double getMeleePDmgPercent() { return meleePDmgPercent; }
    public double getRangePDmgPercent() { return rangePDmgPercent; }
    public double getMeleePDReductionPercent() { return meleePDReductionPercent; }
    public double getRangePDReductionPercent() { return rangePDReductionPercent; }
    public double getPPenFlat() { return pPenFlat; }
    public double getMPenFlat() { return mPenFlat; }
    public double getPPenPercent() { return pPenPercent; }
    public double getMPenPercent() { return mPenPercent; }
    public double getIgnorePDefFlat() { return ignorePDefFlat; }
    public double getIgnoreMDefFlat() { return ignoreMDefFlat; }
    public double getIgnorePDefPercent() { return ignorePDefPercent; }
    public double getIgnoreMDefPercent() { return ignoreMDefPercent; }
    public double getCritDmgResPercent() { return critDmgResPercent; }
    public double getPDmgBonusFlat() { return pDmgBonusFlat; }
    public double getMDmgBonusFlat() { return mDmgBonusFlat; }
}
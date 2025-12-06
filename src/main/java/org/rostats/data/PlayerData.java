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

    // Advanced/Bonus Attributes (All initialized to 0.0)
    private double pAtkBonusFlat = 0.0;
    private double mAtkBonusFlat = 0.0;
    private double critRes = 0.0;
    private double critDmgPercent = 50.0;
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
    private double weaponPAtk = 0.0;
    private double weaponMAtk = 0.0;
    private double hitBonusFlat = 0.0;
    private double fleeBonusFlat = 0.0;
    private double baseMSPD = 0.1;

    private final ROStatsPlugin plugin;

    public PlayerData(ROStatsPlugin plugin) {
        this.plugin = plugin;
        stats.put("STR", 1); stats.put("AGI", 1); stats.put("VIT", 1);
        stats.put("INT", 1); stats.put("DEX", 1); stats.put("LUK", 1);
        calculateMaxSP();
        pendingStats.put("STR", 0); pendingStats.put("AGI", 0); pendingStats.put("VIT", 0);
        pendingStats.put("INT", 0); pendingStats.put("DEX", 0); pendingStats.put("LUK", 0);
    }

    // --- Getters for New Fields ---
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
    public double getMaxHPPercent() { return maxHPPercent; }
    public double getMaxSPPercent() { return maxSPPercent; }
    public double getLifestealPPercent() { return lifestealPPercent; }
    public double getLifestealMPercent() { return lifestealMPercent; }
    public double getTrueDamageFlat() { return trueDamageFlat; }
    public double getShieldValueFlat() { return shieldValueFlat; }
    public double getShieldRatePercent() { return shieldRatePercent; }
    public double getFixedCTFlat() { return fixedCTFlat; }
    public double getPvpDmgReductionPercent() { return pvpDmgReductionPercent; }
    public double getPveDmgReductionPercent() { return pveDmgReductionPercent; }
    public double getPDmgReductionPercent() { return pDmgReductionPercent; }
    public double getMDmgReductionPercent() { return mDmgReductionPercent; }
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
    public double getCritDmgResPercent() { return critDmgResPercent; }
    public double getIgnorePDefFlat() { return ignorePDefFlat; }
    public double getIgnoreMDefFlat() { return ignoreMDefFlat; }
    public double getWeaponPAtk() { return weaponPAtk; }
    public double getWeaponMAtk() { return weaponMAtk; }
    public double getHitBonusFlat() { return hitBonusFlat; }
    public double getFleeBonusFlat() { return fleeBonusFlat; }
    public double getBaseMSPD() { return baseMSPD; }

    // --- Core Methods (Modified) ---
    public int getStat(String key) { return stats.getOrDefault(key.toUpperCase(), 1); }
    public void setStat(String key, int val) { stats.put(key.toUpperCase(), val); calculateMaxSP(); }
    public Set<String> getStatKeys() { return stats.keySet(); }
    public int getPendingStat(String key) { return pendingStats.getOrDefault(key.toUpperCase(), 0); }
    public void setPendingStat(String key, int count) { pendingStats.put(key.toUpperCase(), count); }
    public void clearAllPendingStats() { pendingStats.put("STR", 0); pendingStats.put("AGI", 0); pendingStats.put("VIT", 0);
        pendingStats.put("INT", 0); pendingStats.put("DEX", 0); pendingStats.put("LUK", 0); }

    // Formula A.1 (Max HP) - Corrected
    public double getMaxHP() {
        int vit = getStat("VIT") + getPendingStat("VIT"); // Base Stat + Pending Stat
        int baseLevel = getBaseLevel();

        // MaxHP = (BaseHP + BaseHP × VIT × 0.01) × (1 + MaxHP% / 100)
        // Assume BaseHP = 100 + baseLevel * 10
        double baseHealth = 100.0 + (baseLevel * 10.0);
        double vitMultiplier = 1.0 + (vit * 0.01);

        double finalMaxHealth = baseHealth * vitMultiplier;
        return Math.floor(finalMaxHealth * (1 + getMaxHPPercent() / 100.0));
    }

    // Formula A.2 (Max SP) - Corrected
    public double getMaxSP() {
        int intel = getStat("INT") + getPendingStat("INT");
        int baseLevel = getBaseLevel();

        // MaxSP = (BaseSP + BaseSP × INT × 0.01) × (1 + MaxSP% / 100)
        // Assume BaseSP = 20 + baseLevel * 3
        double baseSP = 20.0 + (baseLevel * 3.0);
        double intMultiplier = 1.0 + (intel * 0.01);

        double finalMaxSP = baseSP * intMultiplier;
        return Math.floor(finalMaxSP * (1 + getMaxSPPercent() / 100.0));
    }

    // NEW: HP Recovery Formula
    // HP Recovery = (BaseHPRecovery + VIT × 0.2) × (1 + HealingReceive% / 100)
    public double getHPRegen() {
        int vit = getStat("VIT");
        double baseHPRecovery = 1.0; // Assume BaseHPRecovery = 1.0

        double baseRegen = baseHPRecovery + (vit * 0.2);
        return baseRegen * (1 + getHealingReceivedPercent() / 100.0);
    }

    // SP Regen - Corrected to include HealingReceive% and exclude maxSpBonus
    public void calculateMaxSP() { if (this.currentSP > getMaxSP()) this.currentSP = getMaxSP(); }
    public void regenSP() { double max = getMaxSP();
        if (this.currentSP < max) {
            int intel = getStat("INT");

            // SP Recovery = (BaseSPRecovery + INT × small_bonus) * (1 + HealingReceive% / 100)
            double baseRegen = 1.0;
            double intelBonus = intel / plugin.getConfig().getDouble("sp-regen.regen-int-divisor", 6.0);

            double regen = baseRegen + intelBonus; // Removed maxSpBonus
            regen *= (1 + getHealingReceivedPercent() / 100.0); // Apply HealingReceive%

            this.currentSP += regen;
            if (this.currentSP > max) this.currentSP = max; }
    }
    public void resetStats() { stats.put("STR", 1); stats.put("AGI", 1); stats.put("VIT", 1);
        stats.put("INT", 1); stats.put("DEX", 1); stats.put("LUK", 1);
        int totalPoints = 0; for (int i = 1; i < baseLevel; i++) totalPoints += getStatPointsGain(i);
        this.statPoints = totalPoints; clearAllPendingStats(); calculateMaxSP(); this.currentSP = getMaxSP(); }
    public int getBaseLevel() { return baseLevel; }
    public void setBaseLevel(int level) { this.baseLevel = level; calculateMaxSP(); }
    public int getStatPoints() { return statPoints; }
    public void setStatPoints(int points) { this.statPoints = points; }
    public double getCurrentSP() { return currentSP; }
    public void setCurrentSP(double sp) { this.currentSP = sp; }
    public int getResetCount() { return resetCount; }
    public void setResetCount(int count) { this.resetCount = count; }
    public void incrementResetCount() { this.resetCount++; }
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
    public long getBaseExp() { return baseExp; }
    public long getBaseExpReq() { return getExpReq(baseLevel); }
    public long getJobExp() { return 0; }
    public long getJobExpReq() { return 1000; }
    public int getJobLevel() { return 1; }
    private long getExpReq(int level) { int multiplier = plugin.getConfig().getInt("exp-formula.base-exp-multiplier", 10); return (long) (Math.pow(level, 3) * multiplier); }
    private int getStatPointsGain(int level) { int threshold = plugin.getConfig().getInt("stat-points-gain.level-50-threshold", 50); int low = plugin.getConfig().getInt("stat-points-gain.points-low-level", 5); int high = plugin.getConfig().getInt("stat-points-gain.points-high-level", 8); return (level <= threshold) ? low : high; }
}
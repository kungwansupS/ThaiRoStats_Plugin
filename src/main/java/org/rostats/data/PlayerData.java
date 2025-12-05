package org.rostats.data;

import org.rostats.ROStatsPlugin; // NEW import (need this if passing plugin to PlayerData, but better to pass config values)
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    private int baseLevel = 1;
    // Job/Skill fields are removed
    private long baseExp = 0;
    private int statPoints = 0;
    private double currentSP = 20;
    private int resetCount = 0;

    private final Map<String, Integer> stats = new HashMap<>();
    private final ROStatsPlugin plugin; // NEW: Assume plugin is now passed during DataManager load

    // NEW: Constructor
    public PlayerData(ROStatsPlugin plugin) {
        this.plugin = plugin;
        stats.put("STR", 1); stats.put("AGI", 1); stats.put("VIT", 1);
        stats.put("INT", 1); stats.put("DEX", 1); stats.put("LUK", 1);
        calculateMaxSP();
    }

    // ... (getStat, setStat, addBaseExp methods) ...

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
        // ... (Formula remains simplified, without Job Bonus) ...
        int intel = getStat("INT");
        double baseSP = 20 + (baseLevel * 5);
        double intBonus = (intel * 2) * (1 + (intel / 100.0));
        return (baseSP + intBonus);
    }

    // ... (calculateMaxSP, regenSP, incrementResetCount) ...

    // UPDATE: resetStats (รีค่า Stats และ Pts ตาม BaseLevel)
    public void resetStats() {
        stats.put("STR", 1); stats.put("AGI", 1); stats.put("VIT", 1);
        stats.put("INT", 1); stats.put("DEX", 1); stats.put("LUK", 1);

        // คำนวณ Pts คืนทั้งหมดจาก Base Level (ไม่รวม Pts ที่ใช้อัพไปแล้ว)
        int totalPoints = 0;
        for (int i = 1; i < baseLevel; i++) totalPoints += getStatPointsGain(i);

        this.statPoints = totalPoints;
        calculateMaxSP();
        this.currentSP = getMaxSP();
    }

    // Getters Setters (Simplified)
    public int getBaseLevel() { return baseLevel; }
    public void setBaseLevel(int level) { this.baseLevel = level; calculateMaxSP(); }
    public int getStatPoints() { return statPoints; }
    public void setStatPoints(int points) { this.statPoints = points; }
    public double getCurrentSP() { return currentSP; }
    public void setCurrentSP(double sp) { this.currentSP = sp; }
    public int getResetCount() { return resetCount; }
    public void setResetCount(int count) { this.resetCount = count; }
}
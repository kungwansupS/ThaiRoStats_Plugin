package org.rostats.data;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    private int baseLevel = 1;
    private int jobLevel = 1;
    private long baseExp = 0;
    private int statPoints = 0;
    private double currentSP = 20;
    private int resetCount = 0;

    private Job job = Job.NOVICE;

    // Active Skill
    private Skill activeSkill = Skill.NONE;
    private final Map<Skill, Long> skillCooldowns = new HashMap<>();

    private final Map<String, Integer> stats = new HashMap<>();

    public PlayerData() {
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

    public void addBaseExp(long amount) {
        this.baseExp += amount;
        while (this.baseExp >= getExpReq(this.baseLevel)) {
            this.baseExp -= getExpReq(this.baseLevel);
            this.baseLevel++;
            this.statPoints += getStatPointsGain(this.baseLevel);
        }
        calculateMaxSP();
    }

    private long getExpReq(int level) { return (long) (Math.pow(level, 3) * 10); }
    private int getStatPointsGain(int level) { return (level <= 50) ? 5 : 8; }

    public double getMaxSP() {
        int intel = getStat("INT");
        double baseSP = 20 + (baseLevel * 5);
        double intBonus = (intel * 2) * (1 + (intel / 100.0));
        double jobBonus = 1.0 + job.getSpMultiplier();
        return (baseSP + intBonus) * jobBonus;
    }

    public void calculateMaxSP() {
        if (this.currentSP > getMaxSP()) this.currentSP = getMaxSP();
    }

    public void regenSP() {
        double max = getMaxSP();
        if (this.currentSP < max) {
            int intel = getStat("INT");
            double regen = 1 + (intel / 6.0) + (max / 100.0);
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

    // --- SKILL METHODS (แก้ Error cannot find symbol) ---
    public Skill getActiveSkill() { return activeSkill; }
    public void setActiveSkill(Skill skill) { this.activeSkill = skill; }

    public boolean isSkillReady(Skill skill) {
        if (!skillCooldowns.containsKey(skill)) return true;
        return System.currentTimeMillis() >= skillCooldowns.get(skill);
    }

    public void setSkillCooldown(Skill skill) {
        skillCooldowns.put(skill, System.currentTimeMillis() + skill.getCooldownMs());
    }

    public long getRemainingCooldown(Skill skill) {
        if (!skillCooldowns.containsKey(skill)) return 0;
        return Math.max(0, skillCooldowns.get(skill) - System.currentTimeMillis());
    }

    // Getters Setters
    public int getBaseLevel() { return baseLevel; }
    public void setBaseLevel(int level) { this.baseLevel = level; calculateMaxSP(); }
    public int getJobLevel() { return jobLevel; }
    public void setJobLevel(int level) { this.jobLevel = level; }
    public int getStatPoints() { return statPoints; }
    public void setStatPoints(int points) { this.statPoints = points; }
    public double getCurrentSP() { return currentSP; }
    public void setCurrentSP(double sp) { this.currentSP = sp; }
    public int getResetCount() { return resetCount; }
    public void setResetCount(int count) { this.resetCount = count; }
    public void incrementResetCount() { this.resetCount++; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; calculateMaxSP(); }
}
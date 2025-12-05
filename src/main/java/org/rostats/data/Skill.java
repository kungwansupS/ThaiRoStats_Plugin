package org.rostats.data;

public enum Skill {
    NONE("None", Job.NOVICE, 0, 0),
    BASH("Bash", Job.SWORDSMAN, 15, 2000),
    FIRE_BOLT("Fire Bolt", Job.MAGE, 20, 1500),
    DOUBLE_STRAFE("Double Strafe", Job.ARCHER, 12, 1000),
    HIDING("Hiding", Job.THIEF, 10, 10000),
    MAMMONITE("Mammonite", Job.MERCHANT, 5, 1000),
    HEAL("Heal", Job.ACOLYTE, 20, 3000);

    private final String displayName;
    private final Job requiredJob;
    private final double spCost;
    private final long cooldownMs;

    Skill(String displayName, Job requiredJob, double spCost, long cooldownMs) {
        this.displayName = displayName;
        this.requiredJob = requiredJob;
        this.spCost = spCost;
        this.cooldownMs = cooldownMs;
    }

    public String getDisplayName() { return displayName; }
    public Job getRequiredJob() { return requiredJob; }
    public double getSpCost() { return spCost; }
    public long getCooldownMs() { return cooldownMs; }
}
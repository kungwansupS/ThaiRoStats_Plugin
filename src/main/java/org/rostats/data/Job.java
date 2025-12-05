package org.rostats.data;

import org.bukkit.Material;

public enum Job {
    // === NOVICE (ผู้เริ่มต้น) ===
    NOVICE("Novice", null, Material.WOODEN_SWORD, "The beginning of the journey.", 0, 0, 0, 0),

    // === 1-1 CLASS (สายบู๊) ===
    SWORDSMAN("Swordsman", NOVICE, Material.IRON_SWORD, "Strong body & Bash skill.", 0.4, 0.0, 0.0, 0), // HP+40%

    // === 1-2 CLASS (สายเวท) ===
    MAGE("Mage", NOVICE, Material.BLAZE_ROD, "Master of elemental magic.", 0.0, 0.4, 0.0, 0), // SP+40%

    // === 1-3 CLASS (สายยิง) ===
    ARCHER("Archer", NOVICE, Material.BOW, "Long range precision.", 0.0, 0.05, 0.0, 15), // Hit+15, SP+5%

    // === 1-4 CLASS (สายความเร็ว/โจร) ===
    THIEF("Thief", NOVICE, Material.IRON_NUGGET, "High evasion & Critical.", 0.0, 0.0, 15.0, 5), // Flee+15, Hit+5

    // === 1-5 CLASS (สายพ่อค้า) ===
    MERCHANT("Merchant", NOVICE, Material.GOLD_INGOT, "Master of zeny & items.", 0.1, 0.0, 0.0, 0), // HP+10%

    // === 1-6 CLASS (สายพระ/ซัพพอร์ต) ===
    ACOLYTE("Acolyte", NOVICE, Material.BOOK, "Holy light & Healing.", 0.1, 0.2, 0.0, 0); // HP+10%, SP+20%

    private final String displayName;
    private final Job parent;
    private final Material icon;
    private final String description;

    // Bonuses
    private final double hpMultiplier; // 0.1 = +10%
    private final double spMultiplier; // 0.1 = +10%
    private final double fleeBonus;    // Flat Value (หน่วย)
    private final double hitBonus;     // Flat Value (หน่วย)

    Job(String displayName, Job parent, Material icon, String description, double hpMultiplier, double spMultiplier, double fleeBonus, double hitBonus) {
        this.displayName = displayName;
        this.parent = parent;
        this.icon = icon;
        this.description = description;
        this.hpMultiplier = hpMultiplier;
        this.spMultiplier = spMultiplier;
        this.fleeBonus = fleeBonus;
        this.hitBonus = hitBonus;
    }

    public String getDisplayName() { return displayName; }
    public Job getParent() { return parent; }
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }
    public double getHpMultiplier() { return hpMultiplier; }
    public double getSpMultiplier() { return spMultiplier; }
    public double getFleeBonus() { return fleeBonus; }
    public double getHitBonus() { return hitBonus; }

    public int getTier() {
        if (this == NOVICE) return 0;
        return 1;
    }
}
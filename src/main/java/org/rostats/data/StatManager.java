package org.rostats.data;

import org.bukkit.entity.Player;
import org.rostats.ROStatsPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatManager {
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final ROStatsPlugin plugin;

    public StatManager(ROStatsPlugin plugin) { // UPDATE: Constructor
        this.plugin = plugin;
    }

    // ... (getData, getStat, setStat, upgradeStat remain the same) ...
    // Note: upgradeStat logic is still simple increment/decrement points.

    public int getStatCost(int currentVal) {
        int costBase = plugin.getConfig().getInt("stat-cost.base", 2);
        int costDivisor = plugin.getConfig().getInt("stat-cost.divisor", 10);
        int costStartLevel = plugin.getConfig().getInt("stat-cost.cost-start-level", 2);

        if (currentVal < costStartLevel) return costBase;
        return ((currentVal - 1) / costDivisor) + costBase;
    }

    // === FORMULAS ===
    // (Hit, Flee, ATK, etc. formulas remain the simplified core version from previous step)

    public double getPhysicalAttack(Player player) {
        PlayerData data = getData(player.getUniqueId());
        return (data.getStat("STR") * 2.0) + (data.getStat("DEX") * 0.5) + (data.getStat("LUK") * 0.5) + data.getBaseLevel();
    }
    // ... (other combat formulas remain the same) ...

    public int getHit(Player player) {
        PlayerData data = getData(player.getUniqueId());
        int dex = getStat(player.getUniqueId(), "DEX");
        return data.getBaseLevel() + dex;
    }

    // NEW: Power Calculation (Custom Formula for ROO style display)
    public double calculatePower(Player player) {
        PlayerData data = getData(player.getUniqueId());
        double str = data.getStat("STR");
        double intel = data.getStat("INT");
        double agi = data.getStat("AGI");
        double vit = data.getStat("VIT");
        double dex = data.getStat("DEX");
        double luk = data.getStat("LUK");
        int baseLevel = data.getBaseLevel();

        // Custom Formula: (STR+INT) * 5 + (AGI+VIT+DEX+LUK) * 2 + BaseLevel * 10
        double coreStatPower = (str + intel) * 5.0;
        double secondaryStatPower = (agi + vit + dex + luk) * 2.0;
        double levelPower = baseLevel * 10.0;

        return coreStatPower + secondaryStatPower + levelPower;
    }
}
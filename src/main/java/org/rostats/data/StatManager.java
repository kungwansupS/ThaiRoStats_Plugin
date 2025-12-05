package org.rostats.data;

import org.bukkit.entity.Player;
import org.rostats.ROStatsPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatManager {
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final ROStatsPlugin plugin;

    public StatManager(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    public PlayerData getData(UUID uuid) {
        // FIX: Ensure this calls the constructor correctly (as fixed in PlayerData.java)
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(plugin));
    }

    // FIX START: RE-ADDING MISSING METHODS
    public int getStat(UUID uuid, String statName) {
        return getData(uuid).getStat(statName);
    }

    public void setStat(UUID uuid, String statName, int value) {
        getData(uuid).setStat(statName, value);
    }
    // FIX END

    public boolean upgradeStat(Player player, String statName) {
        PlayerData data = getData(player.getUniqueId());
        int currentVal = data.getStat(statName);
        int cost = getStatCost(currentVal);

        if (data.getStatPoints() >= cost) {
            // Note: In a full ROO system, this should temporarily store pending upgrades.
            // For now, it applies immediately as requested.
            data.setStatPoints(data.getStatPoints() - cost);
            data.setStat(statName, currentVal + 1);
            return true;
        }
        return false;
    }

    public int getStatCost(int currentVal) {
        int costBase = plugin.getConfig().getInt("stat-cost.base", 2);
        int costDivisor = plugin.getConfig().getInt("stat-cost.divisor", 10);
        int costStartLevel = plugin.getConfig().getInt("stat-cost.cost-start-level", 2);

        if (currentVal < costStartLevel) return costBase;
        return ((currentVal - 1) / costDivisor) + costBase;
    }

    // === FORMULAS ===
    public double getPhysicalAttack(Player player) {
        PlayerData data = getData(player.getUniqueId());
        return (data.getStat("STR") * 2.0) + (data.getStat("DEX") * 0.5) + (data.getStat("LUK") * 0.5) + data.getBaseLevel();
    }

    public double getMagicAttack(Player player) {
        PlayerData data = getData(player.getUniqueId());
        return (data.getStat("INT") * 2.0) + (data.getStat("DEX") * 0.5) + (data.getStat("LUK") * 0.5) + data.getBaseLevel();
    }

    public double getPhysicalDamageBonus(Player player) {
        return (getStat(player.getUniqueId(), "STR") * 0.5) / 100.0;
    }

    public double getMagicDamageBonus(Player player) {
        return (getStat(player.getUniqueId(), "INT") * 0.5) / 100.0;
    }

    public int getHit(Player player) {
        PlayerData data = getData(player.getUniqueId());
        int dex = getStat(player.getUniqueId(), "DEX");
        return data.getBaseLevel() + dex;
    }

    public int getFlee(Player player) {
        PlayerData data = getData(player.getUniqueId());
        int agi = getStat(player.getUniqueId(), "AGI");
        return data.getBaseLevel() + agi;
    }

    public double getAspdBonus(Player player) {
        int agi = getStat(player.getUniqueId(), "AGI");
        int dex = getStat(player.getUniqueId(), "DEX");
        return (agi * 0.01) + (dex * 0.002);
    }

    public double getSoftDef(Player player) {
        return getStat(player.getUniqueId(), "VIT") * 0.5;
    }

    public double getSoftMDef(Player player) {
        return getStat(player.getUniqueId(), "INT") * 0.5;
    }

    public double getCritChance(Player player) {
        return getStat(player.getUniqueId(), "LUK") * 0.3;
    }

    public double getPhysicalPenetration(Player player) {
        return (getStat(player.getUniqueId(), "LUK") * 0.1) / 100.0;
    }

    public double getCriticalDamage(Player player) {
        int str = getStat(player.getUniqueId(), "STR");
        return 1.4 + ((str * 0.2) / 100.0);
    }

    // NEW: Power Calculation (ROO style)
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
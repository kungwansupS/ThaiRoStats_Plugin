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
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(plugin));
    }

    public int getStat(UUID uuid, String statName) {
        return getData(uuid).getStat(statName);
    }

    public void setStat(UUID uuid, String statName, int value) {
        getData(uuid).setStat(statName, value);
    }

    // Helper method to get the total cost of pending points for one stat
    public int getPendingCost(PlayerData data, String statName) {
        int pendingCount = data.getPendingStat(statName);
        if (pendingCount == 0) return 0;

        int currentVal = data.getStat(statName);
        int totalCost = 0;

        for (int i = 0; i < pendingCount; i++) {
            totalCost += getStatCost(currentVal + i);
        }
        return totalCost;
    }

    // Helper method to get the total cost of ALL pending points
    public int getTotalPendingCost(PlayerData data) {
        int totalCost = 0;
        for (String stat : data.getStatKeys()) {
            totalCost += getPendingCost(data, stat);
        }
        return totalCost;
    }

    public boolean upgradeStat(Player player, String statName) {
        PlayerData data = getData(player.getUniqueId());
        int pendingCount = data.getPendingStat(statName);
        int currentVal = data.getStat(statName);

        // Check if adding one more pending point exceeds available total points
        int costOfNextPoint = getStatCost(currentVal + pendingCount);
        int totalPendingCost = getTotalPendingCost(data);

        if (data.getStatPoints() < (totalPendingCost + costOfNextPoint)) {
            // Cannot afford even if the click is temporary
            return false;
        }

        // Update pending count (no point deduction yet)
        data.setPendingStat(statName, pendingCount + 1);
        return true;
    }

    public boolean downgradeStat(Player player, String statName) {
        PlayerData data = getData(player.getUniqueId());
        int pendingCount = data.getPendingStat(statName);

        if (pendingCount <= 0) {
            return false; // Cannot reduce allocated stats (Req must be > 0)
        }

        // Update pending count (no point refund yet)
        data.setPendingStat(statName, pendingCount - 1);
        return true;
    }

    // NEW: Apply all pending stats and deduct cost
    public void allocateStats(Player player) {
        PlayerData data = getData(player.getUniqueId());
        int totalCost = getTotalPendingCost(data);

        if (data.getStatPoints() >= totalCost) {
            for (String stat : data.getStatKeys()) {
                int pendingCount = data.getPendingStat(stat);
                if (pendingCount > 0) {
                    data.setStat(stat, data.getStat(stat) + pendingCount);
                }
            }
            // Deduct cost and clear pending
            data.setStatPoints(data.getStatPoints() - totalCost);
            data.clearAllPendingStats();
            plugin.getAttributeHandler().updatePlayerStats(player); // Recalculate Max HP/ASPD
            player.sendMessage("§a[Allocate] Stats applied! Cost: " + totalCost);
        } else {
            player.sendMessage("§c[Allocate] Not enough points! Required: " + totalCost);
        }
    }


    public int getStatCost(int currentVal) {
        int costBase = plugin.getConfig().getInt("stat-cost.base", 2);
        int costDivisor = plugin.getConfig().getInt("stat-cost.divisor", 10);
        int costStartLevel = plugin.getConfig().getInt("stat-cost.cost-start-level", 2);

        if (currentVal < costStartLevel) return costBase;
        return ((currentVal - 1) / costDivisor) + costBase;
    }

    // === FORMULAS (RESTORED) ===
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

    public double calculatePower(Player player) {
        PlayerData data = getData(player.getUniqueId());
        double str = data.getStat("STR");
        double intel = data.getStat("INT");
        double agi = data.getStat("AGI");
        double vit = data.getStat("VIT");
        double dex = data.getStat("DEX");
        double luk = data.getStat("LUK");
        int baseLevel = data.getBaseLevel();

        double coreStatPower = (str + intel) * 5.0;
        double secondaryStatPower = (agi + vit + dex + luk) * 2.0;
        double levelPower = baseLevel * 10.0;

        return coreStatPower + secondaryStatPower + levelPower;
    }
}
package org.rostats.data; // <--- ต้องมีบรรทัดนี้

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatManager {
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public PlayerData getData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public int getStat(UUID uuid, String statName) {
        return getData(uuid).getStat(statName);
    }

    public void setStat(UUID uuid, String statName, int value) {
        getData(uuid).setStat(statName, value);
    }

    public boolean upgradeStat(Player player, String statName) {
        PlayerData data = getData(player.getUniqueId());
        int currentVal = data.getStat(statName);
        int cost = getStatCost(currentVal);

        if (data.getStatPoints() >= cost) {
            data.setStatPoints(data.getStatPoints() - cost);
            data.setStat(statName, currentVal + 1);
            return true;
        }
        return false;
    }

    public int getStatCost(int currentVal) {
        if (currentVal < 2) return 2;
        return ((currentVal - 1) / 10) + 2;
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
        int jobHit = (int) data.getJob().getHitBonus();
        return data.getBaseLevel() + dex + jobHit;
    }

    public int getFlee(Player player) {
        PlayerData data = getData(player.getUniqueId());
        int agi = getStat(player.getUniqueId(), "AGI");
        int jobFlee = (int) data.getJob().getFleeBonus();
        return data.getBaseLevel() + agi + jobFlee;
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
}
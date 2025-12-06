package org.rostats.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.rostats.ROStatsPlugin;
import org.rostats.data.PlayerData;
import org.rostats.data.StatManager;

public class PAPIHook extends PlaceholderExpansion {

    private final ROStatsPlugin plugin;

    public PAPIHook(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "rostats"; }
    @Override
    public @NotNull String getAuthor() { return "ServerDev"; }
    @Override
    public @NotNull String getVersion() { return "1.0"; }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline()) return "";
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) return "";

        StatManager stats = plugin.getStatManager();
        PlayerData data = stats.getData(player.getUniqueId());

        return switch (params.toLowerCase()) {
            case "baselevel" -> String.valueOf(data.getBaseLevel());
            case "baseexp" -> String.valueOf(data.getBaseExp()); // NEW
            case "baseexpreq" -> String.valueOf(data.getBaseExpReq()); // NEW
            case "joblevel" -> String.valueOf(data.getJobLevel()); // NEW
            case "jobexp" -> String.valueOf(data.getJobExp()); // NEW
            case "jobexpreq" -> String.valueOf(data.getJobExpReq()); // NEW
            case "skillpoints" -> String.valueOf(data.getSkillPoints()); // NEW
            case "statpoints" -> String.valueOf(data.getStatPoints());

            // HP/SP Values
            case "maxhp" -> String.format("%.0f", onlinePlayer.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            case "currenthp" -> String.format("%.0f", onlinePlayer.getHealth());
            case "maxsp" -> String.format("%.0f", data.getMaxSP());
            case "currentsp" -> String.format("%.0f", data.getCurrentSP());

            // Combat Stats
            case "patk" -> String.format("%.0f", stats.getPhysicalAttack(onlinePlayer));
            case "matk" -> String.format("%.0f", stats.getMagicAttack(onlinePlayer));
            case "hit" -> String.valueOf(stats.getHit(onlinePlayer));
            case "flee" -> String.valueOf(stats.getFlee(onlinePlayer));
            case "critchance" -> String.format("%.1f", stats.getCritChance(onlinePlayer));
            case "softdef" -> String.format("%.0f", stats.getSoftDef(onlinePlayer));
            case "softmdef" -> String.format("%.0f", stats.getSoftMDef(onlinePlayer));
            case "power" -> String.format("%.0f", stats.calculatePower(onlinePlayer));

            default -> {
                String statName = params.toUpperCase();
                if (data.getStat(statName) > 0) {
                    // Raw stat values (STR, AGI, etc.)
                    yield String.valueOf(data.getStat(statName));
                }
                yield null;
            }
        };
    }
}
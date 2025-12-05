package org.rostats.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.rostats.ROStatsPlugin;

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
        if (player == null) return "";
        String statName = params.toUpperCase();
        int value = plugin.getStatManager().getStat(player.getUniqueId(), statName);
        return String.valueOf(value);
    }
}
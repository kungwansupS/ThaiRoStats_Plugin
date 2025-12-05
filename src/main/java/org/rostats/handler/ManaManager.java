package org.rostats.handler;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.rostats.ROStatsPlugin;
import org.rostats.data.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManaManager implements Listener {

    private final ROStatsPlugin plugin;
    private final Map<UUID, BossBar> playerBars = new HashMap<>();

    public ManaManager(ROStatsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::regenTask, 40L, 40L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        createBar(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeBar(event.getPlayer());
    }

    public void createBar(Player player) {
        BossBar bar = Bukkit.createBossBar("SP", BarColor.BLUE, BarStyle.SOLID);
        bar.addPlayer(player);
        playerBars.put(player.getUniqueId(), bar);
        updateBar(player);
    }

    public void removeBar(Player player) {
        BossBar bar = playerBars.remove(player.getUniqueId());
        if (bar != null) bar.removePlayer(player);
    }

    public void updateBar(Player player) {
        BossBar bar = playerBars.get(player.getUniqueId());
        if (bar == null) return;

        PlayerData data = plugin.getStatManager().getData(player.getUniqueId());
        double current = data.getCurrentSP();
        double max = data.getMaxSP();
        double progress = current / max;
        if (progress < 0.0) progress = 0.0;
        if (progress > 1.0) progress = 1.0;

        bar.setProgress(progress);
        bar.setTitle("§bSP: " + String.format("%.0f", current) + " / " + String.format("%.0f", max));
    }

    private void regenTask() {
        for (UUID uuid : playerBars.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getStatManager().getData(uuid).regenSP();
                updateBar(player);
            }
        }
    }
}
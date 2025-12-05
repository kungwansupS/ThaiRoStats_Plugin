package org.rostats.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.rostats.ROStatsPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {

    private final ROStatsPlugin plugin;

    public DataManager(ROStatsPlugin plugin) {
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder(), "userdata");
        if (!folder.exists()) folder.mkdirs();
    }

    // UPDATE: loadPlayerData (now uses StatManager method to create PlayerData with plugin instance)
    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        // **IMPORTANT**: StatManager's getData must now create PlayerData using the plugin instance.
        // If StatManager does not have a reference to the plugin, PlayerData cannot access config.
        // Assuming StatManager is updated to StatManager(plugin) and getData handles PlayerData(plugin) creation.
        PlayerData data = plugin.getStatManager().getData(uuid);

        // ... (loading logic remains the same, removing Job/Job Level references) ...
        File file = new File(plugin.getDataFolder(), "userdata/" + uuid + ".yml");

        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            data.setBaseLevel(config.getInt("base-level", 1));
            data.setStatPoints(config.getInt("points", 0));
            data.setResetCount(config.getInt("reset-count", 0));

            // Stats Load...

            data.setStat("STR", config.getInt("stats.STR", 1));
            data.setStat("AGI", config.getInt("stats.AGI", 1));
            data.setStat("VIT", config.getInt("stats.VIT", 1));
            data.setStat("INT", config.getInt("stats.INT", 1));
            data.setStat("DEX", config.getInt("stats.DEX", 1));
            data.setStat("LUK", config.getInt("stats.LUK", 1));

            data.calculateMaxSP();
            if (config.contains("current-sp")) {
                data.setCurrentSP(config.getDouble("current-sp"));
            } else {
                data.setCurrentSP(data.getMaxSP());
            }
            plugin.getLogger().info("📄 Loaded data for " + player.getName());
        } else {
            savePlayerData(player);
        }
        plugin.getAttributeHandler().updatePlayerStats(player);
        plugin.getManaManager().updateBar(player);
    }

    public void savePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        File file = new File(plugin.getDataFolder(), "userdata/" + uuid + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        PlayerData data = plugin.getStatManager().getData(uuid);

        config.set("name", player.getName());
        config.set("base-level", data.getBaseLevel());
        config.set("points", data.getStatPoints());
        config.set("reset-count", data.getResetCount());
        config.set("current-sp", data.getCurrentSP());

        // Stats Save...
        config.set("stats.STR", data.getStat("STR"));
        config.set("stats.AGI", data.getStat("AGI"));
        config.set("stats.VIT", data.getStat("VIT"));
        config.set("stats.INT", data.getStat("INT"));
        config.set("stats.DEX", data.getStat("DEX"));
        config.set("stats.LUK", data.getStat("LUK"));

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
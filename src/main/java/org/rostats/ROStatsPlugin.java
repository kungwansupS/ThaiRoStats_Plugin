package org.rostats;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.rostats.command.AdminCommand;
import org.rostats.command.PlayerCommand;
import org.rostats.data.DataManager;
import org.rostats.data.StatManager;
import org.rostats.gui.GUIListener;
import org.rostats.handler.AttributeHandler;
import org.rostats.handler.CombatHandler; // Import ถูกต้อง
import org.rostats.handler.ManaManager;
import org.rostats.hook.PAPIHook;

import java.util.UUID;

public class ROStatsPlugin extends JavaPlugin implements Listener {

    private StatManager statManager;
    private AttributeHandler attributeHandler;
    private CombatHandler combatHandler;
    private ManaManager manaManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        // 0. Load Config
        saveDefaultConfig();

        // 1. Initialize Managers
        this.statManager = new StatManager(this);
        this.dataManager = new DataManager(this);
        this.manaManager = new ManaManager(this);
        this.attributeHandler = new AttributeHandler(this);
        this.combatHandler = new CombatHandler(this); // Instantiation ถูกต้อง

        // 2. Register Events
        getServer().getPluginManager().registerEvents(attributeHandler, this);
        getServer().getPluginManager().registerEvents(combatHandler, this); // Registration ถูกต้อง
        getServer().getPluginManager().registerEvents(manaManager, this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);

        // 3. Register Commands
        PluginCommand statusCmd = getCommand("status");
        if (statusCmd != null) statusCmd.setExecutor(new PlayerCommand(this));

        PluginCommand adminCmd = getCommand("roadmin");
        if (adminCmd != null) {
            AdminCommand adminExecutor = new AdminCommand(this);
            adminCmd.setExecutor(adminExecutor);
            adminCmd.setTabCompleter(adminExecutor);
        }

        // 4. PAPI Hook
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIHook(this).register();
        }

        // 5. Auto-Save Task (NEW: for completeness)
        // บันทึกข้อมูลทุก 5 นาที (6000 ticks)
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                dataManager.savePlayerData(player);
            }
            getLogger().info("💾 Auto-Saved all player data.");
        }, 6000L, 6000L);


        getLogger().info("✅ ROStats Enabled (Core Stats System)!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                dataManager.savePlayerData(player);
                if (manaManager != null) manaManager.removeBar(player);
            }
        }
        getLogger().info("❌ ROStats Disabled");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        dataManager.loadPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        dataManager.savePlayerData(event.getPlayer());
    }

    // NEW: Helper method for Floating Text (Hologram)
    public void showFloatingText(UUID playerUUID, String text) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) return;

        Location loc = player.getLocation().add(0, 2.5, 0); // Position above the player

        getServer().getScheduler().runTask(this, () -> {
            ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setCustomNameVisible(true);
            stand.customName(Component.text(text));

            // Make it float upwards and remove after 2 seconds
            getServer().getScheduler().runTaskLater(this, () -> {
                stand.remove();
            }, 40L); // 2 seconds * 20 ticks/sec
        });
    }

    public StatManager getStatManager() { return statManager; }
    public ManaManager getManaManager() { return manaManager; }
    public AttributeHandler getAttributeHandler() { return attributeHandler; }
    public DataManager getDataManager() { return dataManager; }
}
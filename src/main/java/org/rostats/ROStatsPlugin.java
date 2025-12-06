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
import org.bukkit.scheduler.BukkitTask;
import org.rostats.command.AdminCommand;
import org.rostats.command.PlayerCommand;
import org.rostats.data.DataManager;
import org.rostats.data.StatManager;
import org.rostats.gui.GUIListener;
import org.rostats.handler.AttributeHandler;
import org.rostats.handler.CombatHandler;
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
        this.combatHandler = new CombatHandler(this);

        // 2. Register Events
        getServer().getPluginManager().registerEvents(attributeHandler, this);
        getServer().getPluginManager().registerEvents(combatHandler, this);
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

    // MODIFIED: Helper method for Floating Text (Hologram) with animation and offset
    public void showFloatingText(UUID playerUUID, String text, double verticalOffset) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) return;

        // Start location: 2.0 blocks above head + offset
        Location startLoc = player.getLocation().add(0, 2.0 + verticalOffset, 0);

        getServer().getScheduler().runTask(this, () -> {
            ArmorStand stand = startLoc.getWorld().spawn(startLoc, ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setCustomNameVisible(true);
            stand.customName(Component.text(text));
            stand.setSmall(true);

            // Animation Task: Move upwards constantly for 1 second (20 ticks)
            BukkitTask[] task = new BukkitTask[1];
            task[0] = getServer().getScheduler().runTaskTimer(this, new Runnable() {
                private int ticks = 0;
                private final Location currentLocation = stand.getLocation();
                private final double distance = 0.5; // Total distance to move up
                private final double step = distance / 20.0; // Distance per tick (over 20 ticks)

                @Override
                public void run() {
                    if (stand.isDead() || ticks >= 20) {
                        stand.remove();
                        if (task[0] != null) task[0].cancel();
                        return;
                    }
                    currentLocation.add(0, step, 0); // Move up
                    stand.teleport(currentLocation);
                    ticks++;
                }
            }, 0L, 1L);
        });
    }

    // Keep the old signature for compatibility, delegating to the new one with a medium offset
    public void showFloatingText(UUID playerUUID, String text) {
        // Use a medium offset (0.25) for single calls that don't need stacking context
        showFloatingText(playerUUID, text, 0.25);
    }

    public StatManager getStatManager() { return statManager; }
    public ManaManager getManaManager() { return manaManager; }
    public AttributeHandler getAttributeHandler() { return attributeHandler; }
    public DataManager getDataManager() { return dataManager; }
}
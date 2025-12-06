package org.rostats; // <-- แก้ไขให้ถูกต้องแล้ว

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType; // MODIFIED: Import SlotType directly to resolve OFFHAND symbol
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.rostats.command.AdminCommand;
import org.rostats.command.PlayerCommand;
import org.rostats.data.DataManager;
import org.rostats.data.StatManager;
import org.rostats.data.ItemBonusService;
import org.rostats.data.PlayerData;
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
    private ItemBonusService itemBonusService;

    @Override
    public void onEnable() {
        // 0. Load Config
        saveDefaultConfig();

        // 1. Initialize Managers
        this.statManager = new StatManager(this);
        this.dataManager = new DataManager(this);
        this.manaManager = new ManaManager(this);
        this.itemBonusService = new ItemBonusService(this);
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

    /**
     * NEW: Centralized method to update player stats, including item bonuses.
     * This method ensures item bonuses are calculated and applied before Bukkit attributes are set.
     * @param player The player to update.
     */
    public void updateAllStats(Player player) {
        // FIX: Get PlayerData from StatManager, not DataManager.
        PlayerData data = getStatManager().getData(player.getUniqueId());

        // 1. Calculate and Apply Item Bonuses to PlayerData fields
        data.applyBonuses(itemBonusService.getCombinedItemBonuses(player));

        // 2. Update Vanilla Attributes (MaxHP/SPD/ASPD/ARMOR)
        attributeHandler.updatePlayerStats(player);

        // 3. Update Visuals (SP/EXP Bars - uses current stats)
        manaManager.updateBar(player);
    }

    /**
     * NEW: Trigger stat update when player equips/unequips gear.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if the click potentially involves an equipment slot (Armor, Offhand, Main Hand)
        // MODIFIED: Uses the shorter SlotType.OFFHAND due to the explicit import above.
        boolean isEquipmentSlot = event.getSlotType().equals(SlotType.ARMOR)
                || event.getSlotType().equals(SlotType.OFFHAND)
                || event.getSlot() == player.getInventory().getHeldItemSlot() // Main Hand
                || event.isShiftClick(); // Shift-clicks often move items to/from equipment slots

        if (isEquipmentSlot) {
            // Delay the update by 1 tick to ensure the item transfer/swap is complete
            // This is crucial for accurately reading the final equipped item's PDC.
            Bukkit.getScheduler().runTaskLater(this, () -> updateAllStats(player), 1L);
        }
    }


    // MODIFIED: Helper method for Floating Text (Hologram) with animation and offset (For EXP/Level Up Stacking)
    public void showFloatingText(UUID playerUUID, String text, double verticalOffset) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null || !player.isOnline()) return;

        // Start location: 2.0 blocks above head + offset
        Location startLoc = player.getLocation().add(0, 2.0 + verticalOffset, 0);

        showAnimatedText(startLoc, text);
    }

    public void showFloatingText(UUID playerUUID, String text) {
        showFloatingText(playerUUID, text, 0.25);
    }

    // NEW: Centralized method for Location-based FCT (Damage/Heal/Status)
    public void showCombatFloatingText(Location loc, String text) {
        showAnimatedText(loc.add(0, 1.5, 0), text); // Default combat/miss position is +1.5 to +2.0
    }

    // NEW: Core Animation Logic (Moved from CombatHandler.java and adapted)
    private void showAnimatedText(Location startLoc, String text) {
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

    // NEW: FCT Helper Methods exposed by the plugin (for external calls, e.g., skill handlers)

    // 7) Normal Damage
    public void showDamageFCT(Location loc, double damage) {
        showCombatFloatingText(loc, "§f" + String.format("%.0f", damage));
    }

    // 8) True Damage
    public void showTrueDamageFCT(Location loc, double damage) {
        showCombatFloatingText(loc, "§6" + String.format("%.0f", damage));
    }

    // 10) Heal HP
    public void showHealHPFCT(Location loc, double value) {
        showCombatFloatingText(loc, "§a+" + String.format("%.0f", value) + " HP");
    }

    // 11) Heal SP
    public void showHealSPFCT(Location loc, double value) {
        showCombatFloatingText(loc, "§b+" + String.format("%.0f", value) + " SP");
    }

    // 12) Status Damage (Poison/Burn/Bleed)
    public void showStatusDamageFCT(Location loc, String status, double value) {
        String color = switch (status.toLowerCase()) {
            case "poison" -> "§2";
            case "burn" -> "§c";
            case "bleed" -> "§4";
            default -> "§7"; // Default to grey if status is unknown
        };
        showCombatFloatingText(loc, color + "-" + String.format("%.0f", value));
    }

    public StatManager getStatManager() { return statManager; }
    public ManaManager getManaManager() { return manaManager; }
    public AttributeHandler getAttributeHandler() { return attributeHandler; }
    public DataManager getDataManager() { return dataManager; }
    public ItemBonusService getItemBonusService() { return itemBonusService; } // NEW Getter
}
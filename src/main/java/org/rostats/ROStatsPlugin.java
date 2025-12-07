package org.rostats; // <-- แก้ไขให้ถูกต้องแล้ว

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
// ... (omitted existing imports)
import org.bukkit.scheduler.BukkitTask;
import org.rostats.command.AdminCommand;
import org.rostats.command.PlayerCommand;
// ... (omitted existing imports)
import org.rostats.handler.ManaManager;
import org.rostats.hook.PAPIHook;
import org.bukkit.plugin.Plugin; // NEW IMPORT
import org.rostats.itemeditor.ItemAttributeManager; // NEW IMPORT
import org.rostats.itemeditor.ItemEditorPlugin; // NEW IMPORT

import java.util.UUID;

public class ROStatsPlugin extends JavaPlugin implements Listener {

    private StatManager statManager;
    private AttributeHandler attributeHandler;
    private CombatHandler combatHandler;
    private ManaManager manaManager;
    private DataManager dataManager;
    private ItemAttributeManager itemAttributeManager; // NEW FIELD: Store Item Manager instance

    @Override
    public void onEnable() {
        // 0. Load Config
        saveDefaultConfig();

        // 1. Initialize Managers
        // ... (omitted initialization code)

        // 2. Register Events
        // ... (omitted event registration code)

        // 3. Register Commands
        // ... (omitted command registration code)

        // 4. PAPI Hook
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIHook(this).register();
        }

        // 5. Item Editor Hook (NEW)
        Plugin itemEditor = getServer().getPluginManager().getPlugin("ThaiRoStats-ItemEditor");
        if (itemEditor instanceof ItemEditorPlugin editorPlugin) {
            this.itemAttributeManager = editorPlugin.getAttributeManager();
            getLogger().info("✅ Hooked to ThaiRoStats-ItemEditor! Item bonuses will be applied.");
        } else {
            getLogger().warning("❌ ThaiRoStats-ItemEditor not found. Item bonuses disabled.");
        }

        // 6. Auto-Save Task (NEW: for completeness)
        // ... (omitted existing code)
    }

    // ... (omitted onDisable and event handlers)

    // ... (omitted showFloatingText, showCombatFloatingText, showAnimatedText, etc.)

    // ... (omitted FCT Helper Methods)

    public StatManager getStatManager() { return statManager; }
    public ManaManager getManaManager() { return manaManager; }
    public AttributeHandler getAttributeHandler() { return attributeHandler; }
    public DataManager getDataManager() { return dataManager; }
    public ItemAttributeManager getItemAttributeManager() { return itemAttributeManager; } // NEW GETTER
}
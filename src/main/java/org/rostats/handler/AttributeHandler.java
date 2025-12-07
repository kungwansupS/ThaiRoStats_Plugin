package org.rostats.handler;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.rostats.ROStatsPlugin;
import org.rostats.data.PlayerData;
import org.rostats.data.StatManager;
import org.rostats.itemeditor.ItemAttributeManager; // NEW IMPORT

public class AttributeHandler implements Listener {

    private final ROStatsPlugin plugin;

    public AttributeHandler(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updatePlayerStats(event.getPlayer());
    }

    public void updatePlayerStats(Player player) {
        StatManager stats = plugin.getStatManager();
        PlayerData data = stats.getData(player.getUniqueId());

        // *** 1. NEW: Get and apply item bonuses (MUST HAPPEN FIRST) ***
        ItemAttributeManager itemManager = plugin.getItemAttributeManager();
        if (itemManager != null) {
            itemManager.updateAllEquippedAttributes(player); // This method is new and must be implemented in the editor plugin
        }
        // ************************************************************

        int vit = data.getStat("VIT");
        int agi = data.getStat("AGI");
        int dex = data.getStat("DEX");
        int baseLevel = data.getBaseLevel();

        // 2. VIT -> Max HP (Now uses getMaxHP which incorporates item bonuses)
        // ... (omitted rest of the code as it is now correct, relying on updated PlayerData getters)

        double finalMaxHealth = data.getMaxHP();

        if (finalMaxHealth > 2048.0) finalMaxHealth = 2048.0;
        setAttribute(player, Attribute.GENERIC_MAX_HEALTH, finalMaxHealth);

        // 3. AGI -> Movement Speed (Now uses BaseMSPD + MSpdPercent, both incorporate item bonuses)
        double speedBonus = data.getBaseMSPD() + (data.getMSpdPercent() / 100.0);
        double finalSpeed = speedBonus;
        if (finalSpeed > 1.0) finalSpeed = 1.0;
        setAttribute(player, Attribute.GENERIC_MOVEMENT_SPEED, finalSpeed);

        // 4. ASPD (Now uses getAspdBonus which incorporates item bonuses)
        double aspdMultiplier = stats.getAspdBonus(player);
        setAttribute(player, Attribute.GENERIC_ATTACK_SPEED, 4.0 * aspdMultiplier);

        // 5. Soft DEF (Values don't change from item, only derived/combat stats do)
        double softDef = stats.getSoftDef(player);
        setAttribute(player, Attribute.GENERIC_ARMOR, softDef);

        if (player.getHealth() > finalMaxHealth) {
            player.setHealth(finalMaxHealth);
        }
    }

    private void setAttribute(Player player, Attribute attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }
}
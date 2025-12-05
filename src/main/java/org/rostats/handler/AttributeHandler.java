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

        int vit = data.getStat("VIT");
        int agi = data.getStat("AGI");
        int dex = data.getStat("DEX");
        int baseLevel = data.getBaseLevel();

        // 1. VIT -> Max HP
        double baseHealth = 20.0 + (baseLevel * 2.0);
        double vitMultiplier = 1.0 + (vit / 100.0);

        // Job Multiplier
        double jobMultiplier = 1.0 + data.getJob().getHpMultiplier();

        double finalMaxHealth = baseHealth * vitMultiplier * jobMultiplier;

        if (finalMaxHealth > 2048.0) finalMaxHealth = 2048.0;
        setAttribute(player, Attribute.GENERIC_MAX_HEALTH, finalMaxHealth);

        // 2. AGI -> Movement Speed
        double speedBonus = agi * 0.002;
        double finalSpeed = 0.1 + speedBonus;
        if (finalSpeed > 1.0) finalSpeed = 1.0;
        setAttribute(player, Attribute.GENERIC_MOVEMENT_SPEED, finalSpeed);

        // 3. ASPD
        double aspdBonus = (agi * 0.01) + (dex * 0.002);
        setAttribute(player, Attribute.GENERIC_ATTACK_SPEED, 4.0 * (1.0 + aspdBonus));

        // 4. Soft DEF
        double softDef = vit * 0.5;
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
package org.rostats.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.rostats.ROStatsPlugin;
import org.rostats.data.StatManager;

import java.time.Duration;
import java.util.Random;

public class CombatHandler implements Listener {

    private final ROStatsPlugin plugin;
    private final Random random = new Random();

    public CombatHandler(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        double weaponDamage = event.getDamage();
        Player attackerPlayer = null;
        Player victimPlayer = null;
        boolean isRanged = false;
        boolean isMagic = (event.getCause() == EntityDamageEvent.DamageCause.MAGIC);
        Entity damagerEntity = event.getDamager();

        // ระบุตัวตน (Identify Attacker)
        if (damagerEntity instanceof Player p) {
            attackerPlayer = p;
            isRanged = false;
        } else if (damagerEntity instanceof Projectile proj) {
            isRanged = true;
            if (proj.getShooter() instanceof Player p) attackerPlayer = p;
        }

        if (victim instanceof Player p) victimPlayer = p;

        StatManager stats = plugin.getStatManager();

        // Mob attacking Player (Flee check for victim)
        if (attackerPlayer == null) {
            if (victimPlayer != null) {
                int victimFlee = stats.getFlee(victimPlayer);
                int mobHit = 50;
                if (random.nextDouble() * 100 > (80 + mobHit - victimFlee)) {
                    event.setCancelled(true);
                    showFloatingText(victim.getLocation().add(0, 1.5, 0), "§7MISS");
                    return;
                }
            }
            return;
        }

        // ==========================================
        // 1. Attack Calculation
        // ==========================================
        double statusAtk;
        double damageBonusMultiplier;

        if (isMagic) {
            statusAtk = stats.getMagicAttack(attackerPlayer);
            damageBonusMultiplier = stats.getMagicDamageBonus(attackerPlayer);
        } else {
            statusAtk = stats.getPhysicalAttack(attackerPlayer);
            damageBonusMultiplier = stats.getPhysicalDamageBonus(attackerPlayer);
        }

        double finalDamage = (weaponDamage + statusAtk) * (1.0 + damageBonusMultiplier);

        // ==========================================
        // 2. Hit / Miss (Dodge System)
        // ==========================================
        if (!isMagic) {
            int attackerHit = stats.getHit(attackerPlayer);
            int victimFlee = (victimPlayer != null) ? stats.getFlee(victimPlayer) : 10;
            if (isRanged) attackerHit += 20;

            double hitChance = 80.0 + attackerHit - victimFlee;
            if (hitChance < 5.0) hitChance = 5.0;

            if (random.nextDouble() * 100 > hitChance) {
                event.setCancelled(true);
                showFloatingText(victim.getLocation().add(0, 1.5, 0), "§7MISS");
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f);
                if (damagerEntity instanceof Projectile proj) proj.remove();
                return;
            }
        }

        // ==========================================
        // 3. Critical System
        // ==========================================
        boolean isCritical = false;
        if (!isMagic) {
            double critRate = stats.getCritChance(attackerPlayer);
            if (random.nextDouble() * 100 < critRate) {
                isCritical = true;
                double critDmgMultiplier = stats.getCriticalDamage(attackerPlayer);
                finalDamage *= critDmgMultiplier;
            }
        }

        // ==========================================
        // 4. Defense & Penetration
        // ==========================================
        if (victimPlayer != null) {
            double defense;
            double penetration = 0.0;

            if (isMagic) {
                defense = stats.getSoftMDef(victimPlayer);
            } else {
                defense = stats.getSoftDef(victimPlayer);
                penetration = stats.getPhysicalPenetration(attackerPlayer);
            }

            double effectiveDef = defense * (1.0 - penetration);
            finalDamage -= effectiveDef;
        }

        if (finalDamage < 1.0) finalDamage = 1.0;
        event.setDamage(finalDamage);

        if (isCritical) {
            showFloatingText(victim.getLocation().add(0, 2, 0), "§c§lCRITICAL " + String.format("%.0f", finalDamage));
            attackerPlayer.playSound(attackerPlayer.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
            attackerPlayer.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 20);

            Title.Times times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(500), Duration.ofMillis(200));
            attackerPlayer.showTitle(Title.title(Component.text(""), Component.text("§cCRITICAL!"), times));
        }
    }

    private void showFloatingText(Location loc, String text) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            org.bukkit.entity.ArmorStand stand = loc.getWorld().spawn(loc, org.bukkit.entity.ArmorStand.class);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setCustomName(text);
            stand.setCustomNameVisible(true);
            stand.setSmall(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, stand::remove, 20L);
        });
    }
}
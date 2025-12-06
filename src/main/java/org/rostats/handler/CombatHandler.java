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
import org.rostats.data.PlayerData;
import org.rostats.data.StatManager;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

public class CombatHandler implements Listener {

    private final ROStatsPlugin plugin;
    private final Random random = new Random();
    private final double SKILL_POWER = 1.0; // Default skill power for basic attacks
    private final double BASE_CRIT = 1.5;
    private final int K_DEFENSE = 400; // Constant K for defense formula (Section G)

    public CombatHandler(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        double weaponDamage = event.getDamage();
        Player attackerPlayer = null;
        LivingEntity defenderEntity = victim;
        boolean isRanged = false;
        boolean isMagic = (event.getCause() == EntityDamageEvent.DamageCause.MAGIC);
        Entity damagerEntity = event.getDamager();

        // 0. Identify Attacker/Defender
        if (damagerEntity instanceof Player p) {
            attackerPlayer = p;
        } else if (damagerEntity instanceof Projectile proj) {
            isRanged = true;
            if (proj.getShooter() instanceof Player p) attackerPlayer = p;
        }

        // If Attacker is Mob: Use simple Flee/Defense check and exit
        if (attackerPlayer == null) {
            if (defenderEntity instanceof Player victimPlayer) {
                int victimFlee = plugin.getStatManager().getFlee(victimPlayer);
                int mobHit = 50; // Placeholder Mob HIT
                if (random.nextDouble() * 100 > (80 + mobHit - victimFlee)) {
                    event.setCancelled(true);
                    showFloatingText(victim.getLocation().add(0, 1.5, 0), "§7MISS");
                    return;
                }
            }
            return;
        }

        // Attacker is Player (A)
        PlayerData A = plugin.getStatManager().getData(attackerPlayer.getUniqueId());
        StatManager stats = plugin.getStatManager();

        // Defender (D)
        PlayerData D = (defenderEntity instanceof Player) ? plugin.getStatManager().getData(defenderEntity.getUniqueId()) : null;


        // ==========================================
        // COMBAT PIPELINE (Section B)
        // ==========================================

        // 1. HIT check (Hit vs Flee)
        if (!isMagic) {
            int attackerHit = stats.getHit(attackerPlayer);
            int defenderFlee = (defenderEntity instanceof Player) ? stats.getFlee((Player) defenderEntity) : 10;

            // Formula: HitRate = 0.75 + (AttackerHIT - DefenderFLEE) / 1000
            double hitRate = 0.75 + (attackerHit - defenderFlee) / 1000.0;
            hitRate = Math.max(0.05, Math.min(0.95, hitRate));

            if (random.nextDouble() > hitRate) {
                event.setCancelled(true);
                showFloatingText(defenderEntity.getLocation().add(0, 1.5, 0), "§7MISS");
                return;
            }
        }

        // 2. Determine damage type (Skipped: Already determined by isMagic)
        double damage = weaponDamage;
        if (isMagic) {
            damage += stats.getMagicAttack(attackerPlayer);
        } else {
            damage += stats.getPhysicalAttack(attackerPlayer);
        }

        // 3. Compute BaseDamage (ATK * SkillPower)
        double baseDamage = damage * SKILL_POWER;

        // 4. Apply Damage Bonus% (Section C, D)
        if (isMagic) {
            baseDamage *= (1 + A.getMDmgBonusPercent() / 100.0);
        } else {
            baseDamage *= (1 + A.getPDmgBonusPercent() / 100.0);
        }

        // 5. Apply Melee/Range Bonus% (Section C, D)
        if (!isMagic) {
            if (isRanged) {
                baseDamage *= (1 + A.getRangePDmgPercent() / 100.0);
            } else { // Melee
                baseDamage *= (1 + A.getMeleePDmgPercent() / 100.0);
            }
        }

        // 6. Apply Flat Damage Bonus (Section C, D)
        if (isMagic) {
            baseDamage += A.getMDmgBonusFlat();
        } else {
            baseDamage += A.getPDmgBonusFlat();
        }

        // 7. Apply PVE/PVP Bonus (Simplified: Mob is PVE, Player is PVP)
        if (defenderEntity instanceof Player) {
            baseDamage *= (1 + A.getPvpDmgBonusPercent() / 100.0);
        } else { // PVE
            baseDamage *= (1 + A.getPveDmgBonusPercent() / 100.0);
        }

        // 8. Apply Defense Pipeline (Section G) & 9. Convert DEF -> Reduction
        double damageAfterDEF = baseDamage;
        if (D != null && defenderEntity instanceof Player defenderPlayer) {
            damageAfterDEF = isMagic ?
                    applyMagicDEF(A, D, baseDamage, defenderPlayer) :
                    applyPhysicalDEF(A, D, baseDamage, defenderPlayer);
        }

        double damageTaken = damageAfterDEF;

        // 10. Apply Damage Reduction% (P/M, Melee/Range)
        if (D != null && defenderEntity instanceof Player defenderPlayer) {
            if (isMagic) {
                damageTaken *= (1 - D.getMDmgReductionPercent() / 100.0);
            } else {
                damageTaken *= (1 - D.getPDmgReductionPercent() / 100.0);

                if (isRanged) {
                    damageTaken *= (1 - D.getRangePDReductionPercent() / 100.0);
                } else { // Melee
                    damageTaken *= (1 - D.getMeleePDReductionPercent() / 100.0);
                }
            }

            // 11. Apply PVE/PVP Reduction (Section C, D)
            if (defenderEntity instanceof Player) {
                damageTaken *= (1 - D.getPvpDmgReductionPercent() / 100.0);
            } else { // PVE
                damageTaken *= (1 - D.getPveDmgReductionPercent() / 100.0);
            }
        }

        // 12. Apply Final DMG% (Section I)
        damageTaken *= (1 + A.getFinalDmgPercent() / 100.0);

        // 13. Apply Final P.DMG% or Final M.DMG% (Section I)
        if (isMagic) {
            damageTaken *= (1 + A.getFinalMDmgPercent() / 100.0);
        } else {
            damageTaken *= (1 + A.getFinalPDmgPercent() / 100.0);
        }

        // 14. Apply Final DMG RES% (Section I)
        if (D != null) {
            damageTaken *= (1 - D.getFinalDmgResPercent() / 100.0);
        }

        // 15. Apply Critical (Section F)
        boolean isCritical = false;
        if (!isMagic) {
            double effectiveCritChance = calculateCritChance(A, D);
            if (random.nextDouble() < effectiveCritChance) {
                isCritical = true;
                double critMultiplier = calculateCritMultiplier(A, D);
                damageTaken *= critMultiplier;
            }
        }

        // 16. Apply Shield absorption (Section J)
        if (D != null && D.getShieldValueFlat() > 0) {
            double absorb = Math.min(D.getShieldValueFlat(), damageTaken);
            damageTaken -= absorb;
            // State change for D.ShieldValue must be handled with care
        }

        // 17. Apply Lifesteal heal (Section K) - We only calculate it here.

        // Final Output
        double finalDamage = Math.max(1.0, damageTaken);
        event.setDamage(finalDamage);

        if (isCritical && attackerPlayer != null) {
            showCritEffects(attackerPlayer, defenderEntity, finalDamage);
        }
    }

    // ==========================================
    // COMBAT HELPER METHODS
    // ==========================================

    // Section G: Defense Pipeline
    private double applyPhysicalDEF(PlayerData A, PlayerData D, double damage, Player defenderPlayer) {
        StatManager stats = plugin.getStatManager();
        double def0 = stats.getSoftDef(defenderPlayer); // CORRECT CALL: Proxy BasePDef

        // 1) Penetration flat: def1 = max(0, def0 - A.PPenFlat)
        double def1 = Math.max(0, def0 - A.getPPenFlat());

        // 2) Penetration %: def2 = def1 * (1 - A.PPenPercent / 100)
        double def2 = def1 * (1 - A.getPPenPercent() / 100.0);

        // 3) Ignore DEF flat: def3 = max(0, def2 - A.IgnorePDefFlat)
        double def3 = Math.max(0, def2 - A.getIgnorePDefFlat());

        // 4) Ignore DEF %: effectiveDef = def3 * (1 - A.IgnorePDefPercent / 100)
        double effectiveDef = def3 * (1 - A.getIgnorePDefPercent() / 100.0);

        // 5) Convert DEF -> Reduction
        double defReduction = effectiveDef / (effectiveDef + K_DEFENSE);

        // 6) Apply
        return damage * (1 - defReduction);
    }

    private double applyMagicDEF(PlayerData A, PlayerData D, double damage, Player defenderPlayer) {
        StatManager stats = plugin.getStatManager();
        double def0 = stats.getSoftMDef(defenderPlayer); // CORRECT CALL: Proxy BaseMDef

        // Simplified Magic Def Pipeline
        double effectiveMDef = Math.max(0, def0 - A.getMPenFlat());
        effectiveMDef *= (1 - A.getMPenPercent() / 100.0);
        effectiveMDef = Math.max(0, effectiveMDef - A.getIgnoreMDefFlat());
        effectiveMDef *= (1 - A.getIgnoreMDefPercent() / 100.0);

        double mDefReduction = effectiveMDef / (effectiveMDef + K_DEFENSE);

        return damage * (1 - mDefReduction);
    }

    // Section F: Critical Chance
    private double calculateCritChance(PlayerData A, PlayerData D) {
        // 1) Crit Chance: rawCrit = A.CRIT / 100
        double rawCrit = A.getStatKeys().stream().filter(s -> s.equals("LUK")).findFirst().map(s -> A.getStat(s)).orElse(0) * 0.3 / 100.0;

        // critRes = D.CRIT_RES / 100
        double critRes = (D != null) ? D.getCritRes() / 100.0 : 0.0;

        // effectiveCritChance = max(0, rawCrit - critRes)
        return Math.max(0, rawCrit - critRes);
    }

    // Section F: Critical Multiplier
    private double calculateCritMultiplier(PlayerData A, PlayerData D) {
        double bonusCrit = A.getCritDmgPercent() / 100.0;
        double resistCrit = (D != null) ? D.getCritDmgResPercent() / 100.0 : 0.0;

        // CritMultiplier = baseCrit * (1 + bonusCrit) * (1 - resistCrit)
        return BASE_CRIT * (1 + bonusCrit) * (1 - resistCrit);
    }

    // Helper for visual effects
    private void showCritEffects(Player attacker, LivingEntity victim, double finalDamage) {
        showFloatingText(victim.getLocation().add(0, 2, 0), "§c§lCRITICAL " + String.format("%.0f", finalDamage));
        attacker.playSound(attacker.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1f);
        attacker.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1, 0), 20);

        Title.Times times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(500), Duration.ofMillis(200));
        attacker.showTitle(Title.title(Component.text(""), Component.text("§cCRITICAL!"), times));
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
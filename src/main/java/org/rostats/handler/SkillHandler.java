package org.rostats.handler;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.rostats.ROStatsPlugin;
import org.rostats.data.PlayerData;
import org.rostats.data.Skill;
import org.rostats.data.StatManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SkillHandler implements Listener {

    private final ROStatsPlugin plugin;
    private final Set<UUID> nextHitSkill = new HashSet<>();

    public SkillHandler(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCast(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;

        PlayerData data = plugin.getStatManager().getData(player.getUniqueId());
        Skill skill = data.getActiveSkill();

        if (skill == Skill.NONE) return;

        // Check Job
        if (data.getJob().getTier() < skill.getRequiredJob().getTier() && data.getJob() != skill.getRequiredJob()) {
            player.sendActionBar(Component.text("§cInvalid Job for this skill!"));
            return;
        }

        // Check Cooldown
        if (!data.isSkillReady(skill)) {
            long left = data.getRemainingCooldown(skill);
            player.sendActionBar(Component.text("§cCooldown: " + String.format("%.1f", left / 1000.0) + "s"));
            return;
        }

        // Check SP
        if (data.getCurrentSP() < skill.getSpCost()) {
            player.sendActionBar(Component.text("§cNot enough SP!"));
            return;
        }

        // Execute Skill
        boolean success = false;

        switch (skill) {
            case BASH:
                nextHitSkill.add(player.getUniqueId());
                player.sendMessage("§e[Bash] Ready!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 0.5f);
                success = true;
                break;

            case FIRE_BOLT:
                SmallFireball fireball = player.launchProjectile(SmallFireball.class);
                fireball.setIsIncendiary(false);
                fireball.setYield(0);
                fireball.setShooter(player);
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
                success = true;
                break;

            case DOUBLE_STRAFE:
                if (item.getType() != Material.BOW && item.getType() != Material.CROSSBOW) {
                    player.sendMessage("§cRequires a Bow!");
                    return;
                }
                player.launchProjectile(Arrow.class);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> player.launchProjectile(Arrow.class), 3L);
                player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 1f);
                success = true;
                break;

            case HIDING:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1));
                player.sendMessage("§7[Hiding] Active.");
                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);
                success = true;
                break;

            case MAMMONITE:
                if (player.getInventory().contains(Material.GOLD_NUGGET)) {
                    nextHitSkill.add(player.getUniqueId());
                    player.sendMessage("§6[Mammonite] Ready! (1 Gold)");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    success = true;
                } else {
                    player.sendMessage("§cRequires 1 Gold Nugget!");
                }
                break;

            case HEAL:
                StatManager stats = plugin.getStatManager();
                double matk = stats.getMagicAttack(player);
                double healAmount = matk * 0.5;
                double maxHP = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                double newHP = Math.min(maxHP, player.getHealth() + healAmount);
                player.setHealth(newHP);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 2, 0), 5);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                player.sendMessage("§aHealed " + String.format("%.0f", healAmount) + " HP");
                success = true;
                break;
        }

        if (success) {
            data.setCurrentSP(data.getCurrentSP() - skill.getSpCost());
            data.setSkillCooldown(skill);
            plugin.getManaManager().updateBar(player);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        if (nextHitSkill.contains(player.getUniqueId())) {
            PlayerData data = plugin.getStatManager().getData(player.getUniqueId());
            Skill skill = data.getActiveSkill();
            double dmg = event.getDamage();

            if (skill == Skill.BASH) {
                event.setDamage(dmg * 2.0);
                player.getWorld().spawnParticle(Particle.EXPLOSION, event.getEntity().getLocation(), 1);
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
                player.sendMessage("§c[Bash] Hit!");
                if (event.getEntity() instanceof LivingEntity target) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 5));
                }
            }
            else if (skill == Skill.MAMMONITE) {
                if (player.getInventory().contains(Material.GOLD_NUGGET)) {
                    player.getInventory().removeItem(new ItemStack(Material.GOLD_NUGGET, 1));
                    event.setDamage(dmg * 3.0);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
                    player.sendMessage("§6[Mammonite] Smash!");
                }
            }
            nextHitSkill.remove(player.getUniqueId());
        }
    }
}
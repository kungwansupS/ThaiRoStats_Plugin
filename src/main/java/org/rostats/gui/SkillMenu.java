package org.rostats.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.rostats.ROStatsPlugin;
import org.rostats.data.Job;
import org.rostats.data.PlayerData;
import org.rostats.data.Skill;

import java.util.ArrayList;
import java.util.List;

public class SkillMenu {

    private final ROStatsPlugin plugin;

    public SkillMenu(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        PlayerData data = plugin.getStatManager().getData(player.getUniqueId());
        Job myJob = data.getJob();
        Skill active = data.getActiveSkill();

        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§0§lSelect Active Skill"));

        int slot = 10;
        for (Skill s : Skill.values()) {
            if (s == Skill.NONE) continue;

            if (s.getRequiredJob() == myJob || myJob.getParent() == s.getRequiredJob()) {
                boolean isSelected = (s == active);
                inv.setItem(slot++, createSkillIcon(s, isSelected));
            }
        }

        inv.setItem(26, createItem(Material.BARRIER, "§cUnequip Skill", "§7Click to clear active skill."));
        player.openInventory(inv);
    }

    private ItemStack createSkillIcon(Skill skill, boolean selected) {
        Material mat = selected ? Material.LIME_DYE : Material.BOOK;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        String prefix = selected ? "§a§l[EQUIPPED] " : "§e";
        meta.displayName(Component.text(prefix + skill.getDisplayName()));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Job: " + skill.getRequiredJob().getDisplayName()));
        lore.add(Component.text("§bSP Cost: " + (int)skill.getSpCost()));
        lore.add(Component.text("§cCooldown: " + (skill.getCooldownMs()/1000.0) + "s"));
        lore.add(Component.text(" "));
        lore.add(Component.text("§eClick to select!"));

        if (selected) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(desc));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
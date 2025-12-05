package org.rostats.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.rostats.ROStatsPlugin;
import org.rostats.data.Job;
import org.rostats.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class JobMenu {

    private final ROStatsPlugin plugin;

    public JobMenu(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        PlayerData data = plugin.getStatManager().getData(player.getUniqueId());
        Job currentJob = data.getJob();
        int jobLevel = data.getJobLevel();

        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§0§lJob Selection"));

        inv.setItem(4, createItem(currentJob.getIcon(), "§eCurrent: " + currentJob.getDisplayName(),
                "§7Job Level: §a" + jobLevel));

        int slot = 10;
        boolean canChange = false;

        for (Job targetJob : Job.values()) {
            if (targetJob.getParent() == currentJob) {
                int reqLevel = (currentJob == Job.NOVICE) ? 10 : 40;

                if (jobLevel >= reqLevel) {
                    inv.setItem(slot++, createJobIcon(targetJob, true));
                    canChange = true;
                } else {
                    inv.setItem(slot++, createLockedIcon(targetJob, reqLevel));
                }

                if (slot == 13) slot++;
            }
        }

        if (!canChange && slot == 10) {
            inv.setItem(13, createItem(Material.BARRIER, "§cNo jobs available", "§7You need Job Lv.10 to change class."));
        }

        // [BACK BUTTON]
        ItemStack backBtn = createItem(Material.ARROW, "§c§lBack", "§7Return to Main Menu");
        inv.setItem(26, backBtn);

        player.openInventory(inv);
    }

    private ItemStack createJobIcon(Job job, boolean unlocked) {
        ItemStack item = new ItemStack(job.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§6§l" + job.getDisplayName()));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7" + job.getDescription()));
        lore.add(Component.text(" "));
        if (job.getHpMultiplier() > 0) lore.add(Component.text("§7HP Bonus: §a+" + (int)(job.getHpMultiplier()*100) + "%"));
        if (job.getSpMultiplier() > 0) lore.add(Component.text("§7SP Bonus: §b+" + (int)(job.getSpMultiplier()*100) + "%"));
        if (job.getHitBonus() > 0) lore.add(Component.text("§7HIT Bonus: §6+" + (int)job.getHitBonus()));
        if (job.getFleeBonus() > 0) lore.add(Component.text("§7FLEE Bonus: §b+" + (int)job.getFleeBonus()));
        lore.add(Component.text(" "));
        lore.add(Component.text("§eClick to change class!"));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createLockedIcon(Job job, int req) {
        ItemStack item = new ItemStack(Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§8§l" + job.getDisplayName() + " (Locked)"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§cRequires Job Lv. " + req));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        List<Component> l = new ArrayList<>();
        for (String s : lore) l.add(Component.text(s));
        meta.lore(l);
        item.setItemMeta(meta);
        return item;
    }
}
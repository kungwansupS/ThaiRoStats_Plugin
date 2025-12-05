package org.rostats.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.rostats.ROStatsPlugin;
import org.rostats.data.PlayerData;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {

    private final ROStatsPlugin plugin;

    public MainMenu(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§0§lMain Menu"));
        PlayerData data = plugin.getStatManager().getData(player.getUniqueId());

        // --- Buttons ---
        ItemStack statsBtn = createItem(Material.PLAYER_HEAD, "§a§lStatus & Attributes",
                "§7Check and upgrade your stats.",
                "§7Points Left: §e" + data.getStatPoints(),
                " ",
                "§eClick to open!");
        inv.setItem(11, statsBtn);

        ItemStack jobBtn = createItem(Material.GOLDEN_HELMET, "§6§lClass Info",
                "§7Current: §f" + data.getJob().getDisplayName(),
                "§7Job Level: §a" + data.getJobLevel(),
                " ",
                "§eClick to change class!");
        inv.setItem(13, jobBtn);

        ItemStack skillBtn = createItem(Material.ENCHANTED_BOOK, "§d§lActive Skills",
                "§7Current: §f" + data.getActiveSkill().getDisplayName(),
                " ",
                "§eClick to manage skills!");
        inv.setItem(15, skillBtn);

        ItemStack resetBtn = createItem(Material.REDSTONE, "§c§lReset All",
                "§7Reset Stats & Points.",
                "§7Used: §f" + data.getResetCount() + " times",
                " ",
                "§cClick to reset!");
        inv.setItem(22, resetBtn);

        // [CLOSE BUTTON] มุมขวาล่าง
        ItemStack closeBtn = createItem(Material.BARRIER, "§c§lClose Menu", "§7Click to close.");
        inv.setItem(26, closeBtn);

        // Background
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, bg);
        }

        player.openInventory(inv);
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
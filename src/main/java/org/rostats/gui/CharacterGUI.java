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
import org.rostats.data.StatManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharacterGUI {

    public enum Tab {
        BASIC_INFO, MORE_INFO, RESET_CONFIRM
    }

    private final ROStatsPlugin plugin;

    public CharacterGUI(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, Tab tab) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("§0§lCharacter Status (ROO)"));
        PlayerData data = plugin.getStatManager().getData(player.getUniqueId());

        // 1. Header/Tabs (NEW: 3 TABS)
        inv.setItem(0, createTabItem(Tab.BASIC_INFO, tab, Material.PLAYER_HEAD, "Basic Info."));
        inv.setItem(1, createTabItem(Tab.MORE_INFO, tab, Material.PAPER, "More Info."));
        inv.setItem(2, createTabItem(Tab.RESET_CONFIRM, tab, Material.REDSTONE, "Reset Point"));

        // Background
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, bg);
        }
        for (int i = 9; i < 54; i++) {
            inv.setItem(i, bg);
        }

        // 2. Content Display
        if (tab == Tab.BASIC_INFO) {
            displayBasicInfo(inv, player, data);
            displayStatAllocation(inv, player, data);
        } else if (tab == Tab.MORE_INFO) {
            displayMoreInfo(inv, player);
        } else if (tab == Tab.RESET_CONFIRM) {
            displayResetConfirm(inv, player, data);
        }

        // [CLOSE BUTTON] มุมขวาล่าง
        inv.setItem(53, createItem(Material.BARRIER, "§c§lClose Menu", "§7Click to close."));

        player.openInventory(inv);
    }

    // --- Display Methods ---

    private void displayBasicInfo(Inventory inv, Player player, PlayerData data) {
        StatManager stats = plugin.getStatManager();

        // (Mimic ROO Image_97b31b.png)
        inv.setItem(10, createItem(Material.IRON_CHESTPLATE, "§e§lBasic Info.",
                "§cHP: §f" + String.format("%.0f/%.0f", player.getHealth(), player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()),
                createBar(player.getHealth(), player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(), "HP"),
                "§bSP: §f" + String.format("%.0f/%.0f", data.getCurrentSP(), data.getMaxSP()),
                createBar(data.getCurrentSP(), data.getMaxSP(), "SP"),
                "§7Base Lv: §a" + data.getBaseLevel(),
                createBar(data.getBaseLevel(), 150.0, "BASE_LV"), // Assume max Base Lv 150 for bar scaling
                "§7Job Lv: §a1 (Dummy)",
                "§8(ยังไม่เปิดให้ใช้งาน - ใช้กับ ROJob Plugin ในอนาคต)",
                "§7Stamina: §a100/100 (Dummy)",
                "§8(ยังไม่เปิดให้ใช้งาน - ใช้กับ Plugin ทำอาหารในอนาคต)",
                "§6Power: §f" + String.format("%.0f", stats.calculatePower(player))
        ));
    }

    private void displayMoreInfo(Inventory inv, Player player) {
        StatManager stats = plugin.getStatManager();

        // (Mimic ROO Image_97b2f9.png - Combat Stats)
        inv.setItem(10, createItem(Material.PAPER, "§f§lCombat Stats (More Info.)",
                "§cATK: §f" + String.format("%.0f", stats.getPhysicalAttack(player)),
                "§dMATK: §f" + String.format("%.0f", stats.getMagicAttack(player)),
                "§aDEF (Soft): §f" + String.format("%.0f", stats.getSoftDef(player)),
                "§bMDEF (Soft): §f" + String.format("%.0f", stats.getSoftMDef(player)),
                "§6HIT: §f" + stats.getHit(player),
                "§bFLEE: §f" + stats.getFlee(player),
                "§eCRIT: §f" + String.format("%.1f%%", stats.getCritChance(player)),
                "§fASPD: §f" + String.format("%.0f%%", stats.getAspdBonus(player) * 100)
        ));
        // Fill other slots with placeholder info if needed
    }

    private void displayResetConfirm(Inventory inv, Player player, PlayerData data) {
        int freeResets = plugin.getConfig().getInt("reset-system.free-resets", 3);
        int usedResets = data.getResetCount();
        String itemReq = plugin.getConfig().getString("reset-system.reset-item", "NETHER_STAR");

        inv.setItem(20, createItem(Material.LIME_CONCRETE, "§a§l[CONFIRM] Reset Stats",
                "§7Are you sure you want to reset all your allocated stats?",
                "§7This will remove all your stat points (STR-LUK).",
                "§7---",
                (usedResets < freeResets) ? "§eFree Resets Left: §a" + (freeResets - usedResets) : "§cRequires: 1x " + itemReq,
                "§eClick to proceed."));

        inv.setItem(24, createItem(Material.RED_CONCRETE, "§c§l[CANCEL]",
                "§7Return to Status Screen.",
                "§eClick to cancel."));

        // Reset point status info
        inv.setItem(13, createItem(Material.REDSTONE, "§c§lReset Point Status",
                "§7Free Resets: §f" + (freeResets - usedResets) + " / " + freeResets,
                "§7Required Item (After Free): §f" + itemReq,
                "§7---",
                "§cWARNING: Resets Stats and gives back Points based on Base Level."
        ));
    }

    private void displayStatAllocation(Inventory inv, Player player, PlayerData data) {
        // Stats start at slot 19
        inv.setItem(19, createStatRow(player, data, "STR", Material.IRON_SWORD, 25));
        inv.setItem(28, createStatRow(player, data, "AGI", Material.FEATHER, 34));
        inv.setItem(37, createStatRow(player, data, "VIT", Material.IRON_CHESTPLATE, 43));
        inv.setItem(20, createStatRow(player, data, "INT", Material.ENCHANTED_BOOK, 26));
        inv.setItem(29, createStatRow(player, data, "DEX", Material.BOW, 35));
        inv.setItem(38, createStatRow(player, data, "LUK", Material.RABBIT_FOOT, 44));

        // Allocation Box at Slots 46-52
        inv.setItem(46, createItem(Material.GOLD_NUGGET, "§6§lStat Points Left", "§7Points: §e" + data.getStatPoints()));
        inv.setItem(47, createItem(Material.LIME_DYE, "§a§lAllocate (Confirm)", "§7Click to apply all pending upgrades."));
        inv.setItem(51, createItem(Material.RED_DYE, "§c§lCancel (Reset Current)", "§7Click to clear all current pending upgrades."));
    }

    // --- Helper Methods ---

    private ItemStack createStatRow(Player player, PlayerData data, String statKey, Material mat, int plusSlot) {
        StatManager stats = plugin.getStatManager();
        int currentVal = data.getStat(statKey);
        int cost = stats.getStatCost(currentVal);
        boolean canAfford = data.getStatPoints() >= cost;
        String costColor = canAfford ? "§a" : "§c";

        // 1. Stat Icon (Slot 19, 28, 37, 20, 29, 38)
        ItemStack item = createItem(mat, "§f§l" + statKey, "§7Current Stat (Pts Only): §e" + currentVal);
        inv.setItem(item.getSlot(), item); // Dummy slot set for item

        // 2. Bonus Slot (StatValue + Bonus) - Placeholder implementation
        inv.setItem(item.getSlot() + 1, createItem(Material.DIAMOND, "§b§lBonus", "§7Bonus Stats (Equip/Other): §b" + 0));

        // 3. Pts Slot (Req)
        inv.setItem(item.getSlot() + 2, createItem(Material.IRON_NUGGET, "§6§lRequired Pts", "§7Cost to +1: " + costColor + cost));

        // 4. Plus Button (Slot 25, 34, 43, 26, 35, 44)
        inv.setItem(plusSlot, createItem(Material.GREEN_STAINED_GLASS_PANE, "§a§l+1", "§7Cost: " + costColor + cost));

        //         return item; // Return a dummy item for slotting (actual slotting is done by inv.setItem)
    }

    private ItemStack createTabItem(Tab currentTab, Tab activeTab, Material mat, String name) {
        ItemStack item = createItem(mat, (currentTab == activeTab ? "§a§l" : "§f") + name);
        if (currentTab == activeTab) item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    private String createBar(double current, double max, String type) {
        int length = plugin.getConfig().getInt("bar-length", 10);
        String[] colors = plugin.getConfig().getString("bar-colors." + type, "§f|§8").split("\\|");
        String fill = colors[0];
        String empty = colors[1];

        int fillAmount = (int) Math.round((current / max) * length);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i < fillAmount) bar.append(fill);
            else bar.append(empty);
            bar.append("█");
        }
        return bar.toString();
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        List<Component> loreList = new ArrayList<>();
        for (String line : lore) loreList.add(Component.text(line));
        meta.lore(loreList);
        item.setItemMeta(meta);
        return item;
    }
}
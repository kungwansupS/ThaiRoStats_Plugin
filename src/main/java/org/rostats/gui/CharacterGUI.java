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
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
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

        // 1. Header & Tabs (Common to all screens)
        displayHeader(inv, player, tab, data);

        // Background & Filler (Simplified)
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 17 && i < 27 || i > 35 && i < 42 || i > 43 && i < 45 || i > 50 && i < 52) continue; // Skip custom slots
            if (inv.getItem(i) == null) inv.setItem(i, bg);
        }
        inv.setItem(23, bg); // Custom slot 23 for Bonus
        inv.setItem(32, bg); // Custom slot 32 for Req
        inv.setItem(48, bg); // Custom slot 48 for + button

        // 2. Content Display
        if (tab == Tab.BASIC_INFO) {
            displayComprehensiveInfoBlock(inv, player, data);
            displayAllocationMatrix(inv, player, data);
        } else if (tab == Tab.MORE_INFO) {
            displayMoreInfo(inv, player, data);
        } else if (tab == Tab.RESET_CONFIRM) {
            displayResetConfirm(inv, player, data);
        }

        // 3. Controls
        inv.setItem(8, createItem(Material.BARRIER, "§c§lปิดเมนู", "§7คลิกเพื่อปิดหน้าจอสถานะ")); // R0 Close
        inv.setItem(53, createItem(Material.BARRIER, "§c§lปิดเมนู", "§7คลิกเพื่อปิดหน้าจอสถานะ")); // R5 Close

        player.openInventory(inv);
    }

    // --- Header & Layout Helpers ---

    private void displayHeader(Inventory inv, Player player, Tab activeTab, PlayerData data) {
        // R0: Header
        inv.setItem(0, createItem(Material.PLAYER_HEAD, "§e§lCharacter Details",
                "§7ชื่อ: §f" + player.getName(),
                "§7ยศ/ตำแหน่ง: §f(ยังไม่มีระบบยศ)",
                "§7Emblem: §f(ยังไม่มีระบบ Emblem)"
        ));
        inv.setItem(1, createItem(Material.GOLDEN_HELMET, "§e§lJob/Class Info",
                "§7อาชีพ: NOVICE (Placeholder)",
                "§8(ระบบอาชีพปิดใช้งาน)"
        ));
        inv.setItem(2, createItem(Material.PAPER, "§e§lPlayer Info",
                "§7Base Level: §a" + data.getBaseLevel(),
                "§7HP: §c" + String.format("%.0f/%.0f", player.getHealth(), player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()),
                "§7SP: §b" + String.format("%.0f/%.0f", data.getCurrentSP(), data.getMaxSP())
        ));

        // R1: Tabs & Presets
        inv.setItem(9, createTabItem(Tab.BASIC_INFO, activeTab, Material.DIAMOND, "§aข้อมูล Stat (Basic)", "§7หน้าจอหลักสำหรับอัพเกรดค่าสเตตัส"));
        inv.setItem(10, createTabItem(Tab.MORE_INFO, activeTab, Material.PAPER, "§fข้อมูล Combat (More)", "§7แสดงรายละเอียดสถานะการต่อสู้ทั้งหมด"));
        inv.setItem(11, createTabItem(Tab.RESET_CONFIRM, activeTab, Material.REDSTONE, "§cรีเซ็ตแต้ม (Reset Point)", "§7ทำการรีเซ็ตค่าสเตตัสที่อัพเกรดมาทั้งหมด"));

        inv.setItem(13, createItem(Material.LIME_DYE, "§aPreset 1", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อโหลด Stat Preset 1"));
        inv.setItem(14, createItem(Material.BLUE_DYE, "§bPreset 2", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อโหลด Stat Preset 2"));
        inv.setItem(15, createItem(Material.RED_DYE, "§cPreset 3", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อโหลด Stat Preset 3"));

        // R4/R5 Controls
        inv.setItem(42, createItem(Material.GOLD_NUGGET, "§6§lแต้มคงเหลือ", "§7แต้มที่สามารถใช้อัพเกรด Stat ได้"));
        inv.setItem(43, createItem(Material.REDSTONE_BLOCK, "§c§lรีเซ็ตชั่วคราว", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อยกเลิกการอัพเกรดที่รอดำเนินการ"));
        inv.setItem(52, createItem(Material.LIME_CONCRETE, "§a§lยืนยันการอัพเกรด", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อยืนยันการอัพเกรด Stat ทั้งหมด"));
    }

    // NEW: Comprehensive Info Block (Slot 12)
    private void displayComprehensiveInfoBlock(Inventory inv, Player player, PlayerData data) {
        StatManager stats = plugin.getStatManager();
        double maxHP = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHP = player.getHealth();
        int baseLevel = data.getBaseLevel();
        double power = stats.calculatePower(player);
        double maxPower = 5000; // Placeholder Max Power for bar scale

        inv.setItem(12, createItem(Material.IRON_CHESTPLATE, "§e§lสถานะพื้นฐาน (Basic Status)",
                "§cHP: §f" + String.format("%.0f/%.0f", currentHP, maxHP),
                createBar(currentHP, maxHP, "HP"),
                "§bSP: §f" + String.format("%.0f/%.0f", data.getCurrentSP(), data.getMaxSP()),
                createBar(data.getCurrentSP(), data.getMaxSP(), "SP"),
                "§7Base Lv§a" + baseLevel + " §8(" + data.getBaseExp() + "/" + data.getBaseExpReq() + ")",
                createBar(data.getBaseExp(), data.getBaseExpReq(), "BASE_LV"),
                "§7Job Lv§a" + data.getJobLevel() + " §8(" + data.getJobExp() + "/" + data.getJobExpReq() + ")",
                createBar(data.getJobExp(), data.getJobExpReq(), "JOB_LV"),
                "§7Stamina: §a100/100",
                createBar(100.0, 100.0, "STAMINA"), // Stamina bar placeholder
                "§6Power: §f" + String.format("%.0f", power),
                createBar(power, maxPower, "POWER")
        ));
    }


    private void displayAllocationMatrix(Inventory inv, Player player, PlayerData data) {
        // Stat Row Details (R4: Bonus, R5: Req/+1 Button) - Call createStatRow to set R4/R5 items
        createStatRow(inv, player, data, "STR", Material.IRON_SWORD, 27, 36, 45, "§cSTR (Strength)");
        createStatRow(inv, player, data, "AGI", Material.FEATHER, 28, 37, 46, "§bAGI (Agility)");
        createStatRow(inv, player, data, "VIT", Material.IRON_CHESTPLATE, 29, 38, 47, "§aVIT (Vitality)");
        createStatRow(inv, player, data, "INT", Material.ENCHANTED_BOOK, 30, 39, 48, "§dINT (Intelligence)");
        createStatRow(inv, player, data, "DEX", Material.BOW, 31, 40, 49, "§6DEX (Dexterity)");
        createStatRow(inv, player, data, "LUK", Material.RABBIT_FOOT, 32, 41, 50, "§eLUK (Luck)");
    }

    private void displayMoreInfo(Inventory inv, Player player, PlayerData data) {
        StatManager stats = plugin.getStatManager();
        double currentSPRegen = 1 + (data.getStat("INT") / plugin.getConfig().getDouble("sp-regen.regen-int-divisor", 6.0)) + (data.getMaxSP() / plugin.getConfig().getDouble("sp-regen.regen-maxsp-divisor", 100.0));

        // Combine Basic and Advanced Stats into Lore (Slot 19)
        List<String> combinedLore = new ArrayList<>();
        combinedLore.add("§f§l-- ข้อมูลพื้นฐาน --");
        combinedLore.add("§cMax HP: §f" + String.format("%.0f", player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) + " §7| §bMax SP: §f" + String.format("%.0f", data.getMaxSP()));
        combinedLore.add("§cATK (Physical): §f" + String.format("%.0f", stats.getPhysicalAttack(player)) + " §7| §dMATK (Magic): §f" + String.format("%.0f", stats.getMagicAttack(player)));
        combinedLore.add("§aDEF (Soft): §f" + String.format("%.0f", stats.getSoftDef(player)) + " §7| §bMDEF (Soft): §f" + String.format("%.0f", stats.getSoftMDef(player)));
        combinedLore.add("§6HIT: §f" + stats.getHit(player) + " §7| §bFLEE: §f" + stats.getFlee(player));
        combinedLore.add("§bSP Recovery: §f" + String.format("%.1f", currentSPRegen) + " / 2s");
        combinedLore.add("§7---");

        combinedLore.add("§f§l-- ข้อมูลเชิงลึก --");
        combinedLore.add("§fASPD: §f" + String.format("%.0f%%", (1.0 + stats.getAspdBonus(player)) * 100) + " §7| §eCRIT Rate: §f" + String.format("%.1f%%", stats.getCritChance(player)));
        combinedLore.add("§7P. Penetration: §f" + String.format("%.1f%%", stats.getPhysicalPenetration(player) * 100) + " §7| §cCrit DMG: §f" + String.format("%.1f", stats.getCriticalDamage(player)) + "x");
        combinedLore.add("§8(Refine ATK/DEF, Cast Time, etc. จะเพิ่มในภายหลัง)");

        inv.setItem(19, createItem(Material.PAPER, "§f§lDetailed Status", combinedLore.toArray(new String[0])));
    }

    private void displayResetConfirm(Inventory inv, Player player, PlayerData data) {
        int freeResets = plugin.getConfig().getInt("reset-system.free-resets", 3);
        int usedResets = data.getResetCount();
        String itemReq = plugin.getConfig().getString("reset-system.reset-item", "NETHER_STAR");
        Material resetItem = Material.getMaterial(itemReq);

        // Remove previous items and clear area
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 18; i <= 36; i++) {
            if (i == 29 || i == 31) continue;
            inv.setItem(i, bg);
        }
        inv.setItem(12, bg);
        inv.setItem(20, bg);
        inv.setItem(22, bg);
        inv.setItem(24, bg);
        inv.setItem(38, bg);


        // Confirmation Button (Slot 29 - Center of R4)
        inv.setItem(29, createItem(Material.LIME_CONCRETE, "§a§l[ยืนยัน] รีเซ็ตสเตตัส",
                "§cคำเตือน: การกระทำนี้ไม่สามารถย้อนกลับได้!",
                "§7---",
                "§7การรีเซ็ตจะคืนแต้ม Stat ทั้งหมดตาม Base Level",
                "§7แต้มที่ใช้รีเซ็ตฟรี: §f" + Math.max(0, freeResets - usedResets) + " / " + freeResets,
                (usedResets >= freeResets) ? "§cต้องใช้: 1x " + (resetItem != null ? resetItem.name() : itemReq) : "§eเป็นการรีเซ็ตฟรี!",
                "§7---",
                "§eคลิกเพื่อดำเนินการต่อ"
        ));

        // Cancel Button (Slot 31 - Near center)
        inv.setItem(31, createItem(Material.RED_CONCRETE, "§c§l[ยกเลิก]",
                "§7คลิกเพื่อกลับไปหน้าจอ Basic Info",
                "§eคลิกเพื่อยกเลิก"));

        // Slot 12 was the info block in the old layout. Ensure it's empty or filler.
        inv.setItem(12, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
    }

    // --- Stat Row Helper (R3, R4, R5) ---

    private void createStatRow(Inventory inv, Player player, PlayerData data, String statKey, Material mat, int statSlot, int bonusSlot, int reqSlot, String statFullName) {
        StatManager stats = plugin.getStatManager();
        int currentVal = data.getStat(statKey);
        int bonusVal = 0; // Placeholder for Bonus
        int totalVal = currentVal + bonusVal;
        int cost = stats.getStatCost(currentVal);
        boolean canAfford = data.getStatPoints() >= cost;
        String costColor = canAfford ? "§a" : "§c";

        // 1. Stat Icon & Value (Slot: R3) - Update lore for this item
        // The item is already initialized in displayAllocationMatrix, we only update lore here.
        ItemStack statItem = inv.getItem(statSlot);
        if (statItem != null) {
            ItemMeta meta = statItem.getItemMeta();
            List<Component> loreList = new ArrayList<>();
            loreList.add(Component.text("§7Stats (แต้มที่อัพ): §e" + currentVal));
            loreList.add(Component.text("§7Total: §e" + totalVal));
            loreList.add(Component.text("§7หน้าที่: [TBD]")); // Placeholder for description
            meta.lore(loreList);
            statItem.setItemMeta(meta);
            inv.setItem(statSlot, statItem);
        }

        // 2. Bonus Slot (Slot: R4) - Bonus
        inv.setItem(bonusSlot, createItem(Material.DIAMOND, "§b§lBonus",
                "§7Bonus Stats (จากอุปกรณ์/บัฟ): §b" + bonusVal,
                "§8(ยังไม่เปิดใช้งาน)"));

        // 3. Req (+1 Button) (Slot: R5) - Req/Upgrade
        inv.setItem(reqSlot, createItem(Material.GREEN_STAINED_GLASS_PANE, "§a§l+1 Point",
                "§7ค่าใช้จ่าย: " + costColor + cost + " แต้ม",
                "§7---",
                "§eคลิกซ้าย: §7เพิ่ม 1 แต้ม",
                "§eคลิกขวา: §7เพิ่ม 10 แต้ม"
        ));
    }

    // --- General Helpers ---

    private ItemStack createTabItem(Tab currentTab, Tab activeTab, Material mat, String name, String desc) {
        ItemStack item = createItem(mat, (currentTab == activeTab ? "§a§l" : "§f") + name, desc);
        if (currentTab == activeTab) item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
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

        if (max == 0) return empty + "██████████";

        double effectiveCurrent = Math.min(current, max); // Cap current value
        int fillAmount = (int) Math.round((effectiveCurrent / max) * length);

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
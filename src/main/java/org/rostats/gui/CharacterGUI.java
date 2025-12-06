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

        // Background & Filler
        ItemStack bg = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, bg);
        }

        // 2. Content Display
        if (tab == Tab.BASIC_INFO) {
            displayComprehensiveInfoBlock(inv, player, data);
            displayAllocationMatrix(inv, player, data);
        } else if (tab == Tab.MORE_INFO) {
            displayMoreInfo(inv, player, data);
        } else if (tab == Tab.RESET_CONFIRM) {
            displayResetConfirm(inv, player, data);
        }

        player.openInventory(inv);
    }

    // --- Header & Layout Helpers ---

    private void displayHeader(Inventory inv, Player player, Tab activeTab, PlayerData data) {
        // R0: Header (Info, Job, Tabs, Exit)
        inv.setItem(0, createItem(Material.PLAYER_HEAD, "§e§lCharacter Details",
                "§7ชื่อ: §f" + player.getName()
        ));
        inv.setItem(1, createItem(Material.GOLDEN_HELMET, "§e§lJob/Class Info",
                "§7อาชีพ: NOVICE (Placeholder)"
        ));
        // Tabs (R0 C2-C3)
        inv.setItem(2, createTabItem(Tab.BASIC_INFO, activeTab, Material.DIAMOND, "§aBasic Info.", "§7หน้าจอหลักสำหรับอัพเกรดค่าสเตตัส"));
        inv.setItem(3, createTabItem(Tab.MORE_INFO, activeTab, Material.PAPER, "§fMore Info.", "§7แสดงรายละเอียดสถานะการต่อสู้ทั้งหมด"));

        // R0 Presets (C4-C6)
        inv.setItem(4, createItem(Material.LIME_DYE, "§aPreset 1", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อโหลด Stat Preset 1"));
        inv.setItem(5, createItem(Material.BLUE_DYE, "§bPreset 2", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อโหลด Stat Preset 2"));
        inv.setItem(6, createItem(Material.RED_DYE, "§cPreset 3", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อโหลด Stat Preset 3"));

        // R0 C8: Exit
        inv.setItem(8, createItem(Material.BARRIER, "§c§lX", "§7คลิกเพื่อปิดหน้าจอสถานะ"));

        // R3 C6: Points Left (Slot 33)
        inv.setItem(33, createItem(Material.GOLD_NUGGET, "§6§lแต้มคงเหลือ", "§7แต้มที่สามารถใช้อัพเกรด Stat ได้: §e" + data.getStatPoints()));

        // R3 C7: Reset Point (Slot 34)
        inv.setItem(34, createItem(Material.REDSTONE_BLOCK, "§c§lReset Point", "§7คลิกเพื่อเข้าสู่หน้ายืนยันการรีเซ็ตแต้มทั้งหมด"));

        // R4 C6: Reset Select (Temp) (Slot 42)
        inv.setItem(42, createItem(Material.ANVIL, "§c§lReset Select", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อยกเลิกการอัพเกรดที่รอดำเนินการ"));

        // R5 C7: Allocate (Slot 52)
        inv.setItem(52, createItem(Material.LIME_CONCRETE, "§a§lAllocate", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อยืนยันการอัพเกรด Stat ทั้งหมด"));
    }

    // R1 C6-C8: Basic Status Block (Slot 15) - Using R1 C6-C8 area for visual space
    private void displayComprehensiveInfoBlock(Inventory inv, Player player, PlayerData data) {
        StatManager stats = plugin.getStatManager();
        double maxHP = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHP = player.getHealth();
        int baseLevel = data.getBaseLevel();
        double power = stats.calculatePower(player);
        double maxPower = 5000;

        inv.setItem(15, createItem(Material.IRON_CHESTPLATE, "§e§lสถานะพื้นฐาน (Basic Status)",
                "§cHP: §f" + String.format("%.0f/%.0f", currentHP, maxHP),
                createBar(currentHP, maxHP, "HP"),
                "§bSP: §f" + String.format("%.0f/%.0f", data.getCurrentSP(), data.getMaxSP()),
                createBar(data.getCurrentSP(), data.getMaxSP(), "SP"),
                "§7Base Lv§a" + baseLevel + " §8(" + data.getBaseExp() + "/" + data.getBaseExpReq() + ")",
                createBar(data.getBaseExp(), data.getBaseExpReq(), "BASE_LV"),
                "§7Job Lv§a" + data.getJobLevel() + " §8(" + data.getJobExp() + "/" + data.getJobExpReq() + ")",
                createBar(data.getJobExp(), data.getJobExpReq(), "JOB_LV"),
                "§7Stamina: §a100/100",
                createBar(100.0, 100.0, "STAMINA"),
                "§6Power: §f" + String.format("%.0f", power),
                createBar(power, maxPower, "POWER")
        ));
    }


    private void displayAllocationMatrix(Inventory inv, Player player, PlayerData data) {
        // R1: Stats (Slots 9-14)
        // R2: Bonus (Slots 18-23)
        // R3: Req (Slots 27-32)
        // R4: + Button (Slots 36-41)
        // R5: - Button (Slots 45-50)

        createStatRow(inv, player, data, "STR", Material.IRON_SWORD, 9, 18, 27, 36, 45, "§cSTR");
        createStatRow(inv, player, data, "AGI", Material.FEATHER, 10, 19, 28, 37, 46, "§bAGI");
        createStatRow(inv, player, data, "VIT", Material.IRON_CHESTPLATE, 11, 20, 29, 38, 47, "§aVIT");
        createStatRow(inv, player, data, "INT", Material.ENCHANTED_BOOK, 12, 21, 30, 39, 48, "§dINT");
        createStatRow(inv, player, data, "DEX", Material.BOW, 13, 22, 31, 40, 49, "§6DEX");
        createStatRow(inv, player, data, "LUK", Material.RABBIT_FOOT, 14, 23, 32, 41, 50, "§eLUK");
    }

    private void displayMoreInfo(Inventory inv, Player player, PlayerData data) {
        StatManager stats = plugin.getStatManager();
        double currentSPRegen = 1 + (data.getStat("INT") / plugin.getConfig().getDouble("sp-regen.regen-int-divisor", 6.0)) + (data.getMaxSP() / plugin.getConfig().getDouble("sp-regen.regen-maxsp-divisor", 100.0));

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

        // Set More Info block to R2 C1 (Slot 19)
        inv.setItem(19, createItem(Material.PAPER, "§f§lDetailed Status", combinedLore.toArray(new String[0])));
    }

    private void displayResetConfirm(Inventory inv, Player player, PlayerData data) {
        int freeResets = plugin.getConfig().getInt("reset-system.free-resets", 3);
        int usedResets = data.getResetCount();
        String itemReq = plugin.getConfig().getString("reset-system.reset-item", "NETHER_STAR");
        Material resetItem = Material.getMaterial(itemReq);

        // Confirmation Button (Slot 29)
        inv.setItem(29, createItem(Material.LIME_CONCRETE, "§a§l[ยืนยัน] รีเซ็ตสเตตัส",
                "§cคำเตือน: การกระทำนี้ไม่สามารถย้อนกลับได้!",
                "§7---",
                "§7การรีเซ็ตจะคืนแต้ม Stat ทั้งหมดตาม Base Level",
                "§7แต้มที่ใช้รีเซ็ตฟรี: §f" + Math.max(0, freeResets - usedResets) + " / " + freeResets,
                (usedResets >= freeResets) ? "§cต้องใช้: 1x " + (resetItem != null ? resetItem.name() : itemReq) : "§eเป็นการรีเซ็ตฟรี!",
                "§7---",
                "§eคลิกเพื่อดำเนินการต่อ"
        ));

        // Cancel Button (Slot 31)
        inv.setItem(31, createItem(Material.RED_CONCRETE, "§c§l[ยกเลิก]",
                "§7คลิกเพื่อกลับไปหน้าจอ Basic Info",
                "§eคลิกเพื่อยกเลิก"));

        // Clear R1 C3-C5 area
        inv.setItem(12, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(13, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
        inv.setItem(14, createItem(Material.GRAY_STAINED_GLASS_PANE, " "));
    }

    // --- Stat Row Helper (R1, R2, R3, R4, R5) ---

    private void createStatRow(Inventory inv, Player player, PlayerData data, String statKey, Material mat, int statSlot, int bonusSlot, int reqSlot, int addSlot, int minusSlot, String statFullName) {
        StatManager stats = plugin.getStatManager();
        int currentVal = data.getStat(statKey);
        int pendingCount = data.getPendingStat(statKey);

        int bonusVal = 0;
        int totalVal = currentVal + bonusVal + pendingCount;

        int costNextPoint = stats.getStatCost(currentVal + pendingCount);

        // 1. Stat Icon & Value (Slot: R1)
        inv.setItem(statSlot, createItem(mat, statFullName,
                "§7Stats (แต้มที่อัพ): §e" + currentVal + (pendingCount > 0 ? " §a(+" + pendingCount + ")" : ""),
                "§7Total: §e" + totalVal,
                "§7หน้าที่: [TBD]"
        ));

        // 2. Bonus Slot (Slot: R2)
        inv.setItem(bonusSlot, createItem(Material.DIAMOND, "§b§lBonus",
                "§7Bonus Stats (จากอุปกรณ์/บัฟ): §b" + bonusVal,
                "§8(ยังไม่เปิดใช้งาน)"));

        // 3. Req Slot (Slot: R3) - Displays cumulative cost
        inv.setItem(reqSlot, createItem(Material.IRON_NUGGET, "§6§lRequired Points",
                "§7แต้มรอดำเนินการ: §e" + pendingCount,
                "§7ค่าใช้จ่ายรวม: §c" + stats.getPendingCost(data, statKey)
        ));

        // 4. + Button (Slot: R4)
        inv.setItem(addSlot, createItem(Material.GREEN_STAINED_GLASS_PANE, "§a§l+" + statKey,
                "§7แต้มที่ต้องการ: §c" + costNextPoint + " แต้ม",
                "§7---",
                "§eคลิกซ้าย: §7เพิ่ม 1 แต้ม",
                "§eคลิกขวา: §7เพิ่ม 10 แต้ม"
        ));

        // 5. - Button (Slot: R5)
        inv.setItem(minusSlot, createItem(Material.RED_STAINED_GLASS_PANE, "§c§l-" + statKey,
                "§7แต้มที่คืน: §a" + (pendingCount > 0 ? stats.getStatCost(currentVal + pendingCount - 1) : 0),
                "§7---",
                "§eคลิกซ้าย: §7ลด 1 แต้ม",
                "§eคลิกขวา: §7ลด 10 แต้ม"
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

        double effectiveCurrent = Math.min(current, max);
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
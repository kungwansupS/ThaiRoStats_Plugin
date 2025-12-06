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
import org.bukkit.inventory.ItemFlag; // NEW IMPORT

import java.util.ArrayList;
import java.util.List;

public class CharacterGUI {

    public enum Tab {
        BASIC_INFO, GENERAL, ADVANCED, SPECIAL, RESET_CONFIRM
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
            displayAllocationMatrix(inv, player, data);
        } else if (tab == Tab.GENERAL) {
            // Placeholder content display
            inv.setItem(18, createItem(Material.BOOK, "§9§lGeneral Attribute Data", getGeneralLore(player, data)));
        } else if (tab == Tab.ADVANCED) {
            inv.setItem(18, createItem(Material.ENCHANTED_BOOK, "§6§lAdvanced Attribute Data", getAdvancedLore(player, data)));
        } else if (tab == Tab.SPECIAL) {
            inv.setItem(18, createItem(Material.NETHER_STAR, "§d§lSpecial Attribute Data", getSpecialLore(player, data)));
        } else if (tab == Tab.RESET_CONFIRM) {
            displayResetConfirm(inv, player, data);
        }

        player.openInventory(inv);
    }

    // --- Header & Layout Helpers ---

    private void displayHeader(Inventory inv, Player player, Tab activeTab, PlayerData data) {
        // R0: Header (Info, Job, Tabs, Presets, Exit)
        inv.setItem(0, createItem(Material.PLAYER_HEAD, "§e§lCharacter Details", "§7ชื่อ: §f" + player.getName()));
        inv.setItem(1, createItem(Material.GOLDEN_HELMET, "§e§lJob/Class Info", "§7อาชีพ: NOVICE (Placeholder)"));

        // Tabs (R0 C2, C3, C4, C5) - Consolidated Info into Lore
        inv.setItem(2, createTabItem(Tab.BASIC_INFO, activeTab, Material.DIAMOND, "§aBasic Info.", getBasicStatusLore(player, data)));
        inv.setItem(3, createTabItem(Tab.GENERAL, activeTab, Material.BOOK, "§9General Attribute", getGeneralLore(player, data)));
        inv.setItem(4, createTabItem(Tab.ADVANCED, activeTab, Material.ENCHANTED_BOOK, "§6Advanced Attribute", getAdvancedLore(player, data)));
        inv.setItem(5, createTabItem(Tab.SPECIAL, activeTab, Material.NETHER_STAR, "§dSpecial Attribute", getSpecialLore(player, data)));

        // R0 Presets (C6, C7)
        inv.setItem(6, createItem(Material.LIME_DYE, "§aPreset 1", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อโหลด Stat Preset 1"));
        inv.setItem(7, createItem(Material.BLUE_DYE, "§bPreset 2", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อโหลด Stat Preset 2"));

        // R0 C8: Exit
        inv.setItem(8, createItem(Material.BARRIER, "§c§lX", "§7คลิกเพื่อปิดหน้าจอสถานะ"));

        // R3 C6: Points Left (Slot 33)
        inv.setItem(33, createItem(Material.GOLD_NUGGET, "§6§lแต้มคงเหลือ", "§7แต้มที่สามารถใช้อัพเกรด Stat ได้: §e" + data.getStatPoints()));

        // R3 C7: Reset Point (Slot 34)
        inv.setItem(34, createItem(Material.REDSTONE_BLOCK, "§c§lReset Point", "§7คลิกเพื่อเข้าสู่หน้ายืนยันการรีเซ็ตแต้มทั้งหมด"));

        // R4 C6: Reset Select (Temp) (Slot 42)
        inv.setItem(42, createItem(Material.ANVIL, "§c§lReset Select", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อยกเลิกการอัพเกรดที่รอดำเนินการ"));

        // R4 C8: Reset All (Slot 44)
        inv.setItem(44, createItem(Material.REDSTONE, "§c§lReset All", "§7คลิกเพื่อเข้าสู่หน้ายืนยันการรีเซ็ตแต้มทั้งหมด"));

        // R5 C7: Allocate (Slot 52)
        inv.setItem(52, createItem(Material.LIME_CONCRETE, "§a§lAllocate", "§7(ยังไม่เปิดใช้งาน) คลิกเพื่อยืนยันการอัพเกรด Stat ทั้งหมด"));
    }

    // 1. Basic Status Lore (HP/SP/Level/Bars)
    private String[] getBasicStatusLore(Player player, PlayerData data) {
        StatManager stats = plugin.getStatManager();
        double maxHP = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHP = player.getHealth();
        int baseLevel = data.getBaseLevel();
        double power = stats.calculatePower(player);
        double maxPower = 5000;

        List<String> lore = new ArrayList<>();
        lore.add("§7--- สถานะปัจจุบัน ---");

        lore.add("§cHP: §f" + String.format("%.0f/%.0f", currentHP, maxHP));
        lore.add(createBar(currentHP, maxHP, "HP"));
        lore.add("§bSP: §f" + String.format("%.0f/%.0f", data.getCurrentSP(), data.getMaxSP()));
        lore.add(createBar(data.getCurrentSP(), data.getMaxSP(), "SP"));
        lore.add("§7Base Lv§a" + baseLevel + " §8(" + data.getBaseExp() + "/" + data.getBaseExpReq() + ")");
        lore.add(createBar(data.getBaseExp(), data.getBaseExpReq(), "BASE_LV"));
        lore.add("§7Job Lv§a" + data.getJobLevel() + " §8(" + data.getJobExp() + "/" + data.getJobExpReq() + ")");
        lore.add(createBar(data.getJobExp(), data.getJobExpReq(), "JOB_LV"));
        lore.add("§7Stamina: §a100/100");
        lore.add(createBar(100.0, 100.0, "STAMINA"));
        lore.add("§6Power: §f" + String.format("%.0f", power));
        lore.add(createBar(power, maxPower, "POWER"));
        lore.add("§7--------------------");
        lore.add("§7คลิกเพื่อเปิดหน้าจออัพเกรดสเตตัส");

        return lore.toArray(new String[0]);
    }

    // 2. General Attribute Lore (Section 1)
    private String[] getGeneralLore(Player player, PlayerData data) {
        StatManager stats = plugin.getStatManager();
        double maxHP = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentSPRegen = 1 + (data.getStat("INT") / plugin.getConfig().getDouble("sp-regen.regen-int-divisor", 6.0)) + (data.getMaxSP() / plugin.getConfig().getDouble("sp-regen.regen-maxsp-divisor", 100.0));

        List<String> lore = new ArrayList<>();
        lore.add("§7");
        lore.add("§8§l[§f§lGENERAL ATTRIBUTE (พื้นฐาน)§8§l]");

        // Max HP | Max SP
        lore.add(formatTwoColumns("§fMax HP: §e" + String.format("%.0f", maxHP), "§fMax SP: §e" + String.format("%.0f", data.getMaxSP())));
        // P.ATK | M.ATK
        lore.add(formatTwoColumns("§fP.ATK: §c" + String.format("%.0f", stats.getPhysicalAttack(player)), "§fM.ATK: §b" + String.format("%.0f", stats.getMagicAttack(player))));
        // P.DEF | M.DEF
        lore.add(formatTwoColumns("§fP.DEF: §6" + String.format("%.0f", stats.getSoftDef(player)), "§fM.DEF: §5" + String.format("%.0f", stats.getSoftMDef(player))));
        // HP Recovery | SP Recovery
        lore.add(formatTwoColumns("§fHP Recovery: §20", "§fSP Recovery: §1" + String.format("%.1f", currentSPRegen)));
        // HIT | FLEE
        lore.add(formatTwoColumns("§fHIT: §e" + stats.getHit(player), "§fFLEE: §b" + stats.getFlee(player)));

        lore.add("§7--------------------");
        lore.add("§7คลิกเพื่อเปิดหน้าจอข้อมูลทั่วไป");

        return lore.toArray(new String[0]);
    }

    // 3. Advanced Attribute Lore (Section 2)
    private String[] getAdvancedLore(Player player, PlayerData data) {
        StatManager stats = plugin.getStatManager();

        List<String> lore = new ArrayList<>();
        lore.add("§7");
        lore.add("§8§l[§6§lADVANCED ATTRIBUTE (ขั้นสูง)§8§l]");

        // Critical
        lore.add("§e-- CRITICAL --");
        lore.add(formatTwoColumns("§fCRIT: §e" + String.format("%.1f", stats.getCritChance(player)), "§fCRIT RES: §7" + String.format("%.1f", data.getCritRes())));
        lore.add(formatTwoColumns("§fCRIT DMG: §6" + String.format("%.1f", data.getCritDmgPercent()) + "%", "§fCRIT DMG RES: §8" + String.format("%.1f", data.getCritDmgResPercent()) + "%"));

        // DMG Bonus/Reduction
        lore.add("§e-- DAMAGE BONUS / REDUCTION --");
        lore.add(formatTwoColumns("§fP.DMG Bonus%: §c" + String.format("%.1f", data.getPDmgBonusPercent()) + "%", "§fM.DMG Bonus%: §b" + String.format("%.1f", data.getMDmgBonusPercent()) + "%"));
        lore.add(formatTwoColumns("§fP.DMG +: §c" + String.format("%.0f", data.getPDmgBonusFlat()), "§fM.DMG +: §9" + String.format("%.0f", data.getMDmgBonusFlat())));
        lore.add(formatTwoColumns("§fP.DMG Red: §3" + String.format("%.1f", data.getPDmgReductionPercent()) + "%", "§fM.Red: §5" + String.format("%.1f", data.getMDmgReductionPercent()) + "%"));

        // Melee / Range
        lore.add("§e-- MELEE / RANGE --");
        lore.add(formatTwoColumns("§fMelee DMG: §c" + String.format("%.1f", data.getMeleePDmgPercent()) + "%", "§fRange DMG: §9" + String.format("%.1f", data.getRangePDmgPercent()) + "%"));
        lore.add(formatTwoColumns("§fMelee Red: §3" + String.format("%.1f", data.getMeleePDReductionPercent()) + "%", "§fRange Red: §1" + String.format("%.1f", data.getRangePDReductionPercent()) + "%"));

        // Penetration / Ignore DEF
        lore.add("§e-- PENETRATION / IGNORE DEF --");
        lore.add(formatTwoColumns("§fP.PEN: §d" + String.format("%.0f", data.getPPenFlat()), "§fM.PEN: §d" + String.format("%.0f", data.getMPenFlat())));
        lore.add(formatTwoColumns("§fP.PEN%: §d" + String.format("%.1f%%", stats.getPhysicalPenetration(player) * 100), "§fM.PEN%: §d" + String.format("%.1f", data.getMPenPercent()) + "%"));
        lore.add(formatTwoColumns("§fIgnore P.DEF: §c" + String.format("%.0f", data.getIgnorePDefFlat()), "§fIgnore M.DEF: §b" + String.format("%.0f", data.getIgnoreMDefFlat())));
        lore.add(formatTwoColumns("§fIgnore P.DEF%: §6" + String.format("%.1f", data.getIgnorePDefPercent()) + "%", "§fIgnore M.DEF%: §5" + String.format("%.1f", data.getIgnoreMDefPercent()) + "%"));

        // Speed / Cast Time
        lore.add("§e-- SPEED / CAST TIME --");
        lore.add(formatTwoColumns("§fASPD: §a" + String.format("%.0f%%", (stats.getAspdBonus(player) * 100)), "§fMSPD: §a" + String.format("%.1f", data.getMSpdPercent()) + "%"));
        lore.add(formatTwoColumns("§fVar CT: §d" + String.format("%.1f", data.getVarCTPercent()) + "%", "§fVar Flat: §d" + String.format("%.1f", data.getVarCTFlat()) + "s"));
        lore.add(formatTwoColumns("§fFix CT: §5" + String.format("%.1f", data.getFixedCTPercent()) + "%", "§fFix Flat: §5" + String.format("%.1f", data.getFixedCTFlat()) + "s"));

        // Healing / Final Modifiers
        lore.add("§e-- HEALING / FINAL MODIFIERS --");
        lore.add(formatTwoColumns("§fHealing+: §a" + String.format("%.1f", data.getHealingEffectPercent()) + "%", "§fHeal Recv: §2" + String.format("%.1f", data.getHealingReceivedPercent()) + "%"));
        lore.add(formatTwoColumns("§fFinal DMG: §6" + String.format("%.1f", data.getFinalDmgPercent()) + "%", "§fFinal RES: §8" + String.format("%.1f", data.getFinalDmgResPercent()) + "%"));
        lore.add(formatTwoColumns("§fFinal P.DMG: §c" + String.format("%.1f", data.getFinalPDmgPercent()) + "%", "§fFinal M.DMG: §b" + String.format("%.1f", data.getFinalMDmgPercent()) + "%"));

        // PVE / PVP
        lore.add("§e-- PVE / PVP --");
        lore.add(formatTwoColumns("§fPVE DMG: §a" + String.format("%.1f", data.getPveDmgBonusPercent()) + "%", "§fPVP DMG: §d" + String.format("%.1f", data.getPvpDmgBonusPercent()) + "%"));
        lore.add(formatTwoColumns("§fPVE Red: §2" + String.format("%.1f", data.getPveDmgReductionPercent()) + "%", "§fPVP Red: §5" + String.format("%.1f", data.getPvpDmgReductionPercent()) + "%"));

        lore.add("§7--------------------");
        lore.add("§7คลิกเพื่อเปิดหน้าจอข้อมูลการต่อสู้ขั้นสูง");

        return lore.toArray(new String[0]);
    }

    // 4. Special Attribute Lore (Section 3)
    private String[] getSpecialLore(Player player, PlayerData data) {
        List<String> lore = new ArrayList<>();
        lore.add("§7");
        lore.add("§8§l[§a§lSPECIAL ATTRIBUTE (พิเศษ)§8§l]");

        lore.add(formatTwoColumns("§fMax HP%: §a" + String.format("%.1f", data.getMaxHPPercent()) + "%", "§fMax SP%: §b" + String.format("%.1f", data.getMaxSPPercent()) + "%"));
        lore.add(formatTwoColumns("§fLifesteal P: §c" + String.format("%.1f", data.getLifestealPPercent()) + "%", "§fLifesteal M: §9" + String.format("%.1f", data.getLifestealMPercent()) + "%"));
        lore.add(formatTwoColumns("§fTrue DMG: §4" + String.format("%.0f", data.getTrueDamageFlat()), "§fShield: §3" + String.format("%.0f", data.getShieldValueFlat())));
        lore.add(formatTwoColumns("§fShield Rate: §6" + String.format("%.1f", data.getShieldRatePercent()) + "%", "§7(Reserved)"));

        lore.add("§7--------------------");
        lore.add("§7คลิกเพื่อเปิดหน้าจอข้อมูลพิเศษ");

        return lore.toArray(new String[0]);
    }

    private void displayAllocationMatrix(Inventory inv, Player player, PlayerData data) {
        // R1: Stats (Slots 9-14)
        // R2: Bonus (Slots 18-23)
        // R3: Req (Slots 27-32)
        // R4: + Button (Slots 36-41)
        // R5: - Button (Slots 45-50)

        // Column 0-5
        createStatRow(inv, player, data, "STR", Material.IRON_SWORD, 9, 18, 27, 36, 45, "§cSTR");
        createStatRow(inv, player, data, "AGI", Material.FEATHER, 10, 19, 28, 37, 46, "§bAGI");
        createStatRow(inv, player, data, "VIT", Material.IRON_CHESTPLATE, 11, 20, 29, 38, 47, "§aVIT");
        createStatRow(inv, player, data, "INT", Material.ENCHANTED_BOOK, 12, 21, 30, 39, 48, "§dINT");
        createStatRow(inv, player, data, "DEX", Material.BOW, 13, 22, 31, 40, 49, "§6DEX");
        createStatRow(inv, player, data, "LUK", Material.RABBIT_FOOT, 14, 23, 32, 41, 50, "§eLUK");
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
        int costPreviousPoint = stats.getStatCost(currentVal + pendingCount - 1); // Cost to gain the point being removed

        // 1. Stat Icon & Value (Slot: R1)
        inv.setItem(statSlot, createItem(mat, statFullName,
                "§7Stats (แต้มที่อัพ): §e" + currentVal + (pendingCount > 0 ? " §a(+" + pendingCount + ")" : ""),
                "§7Total: §e" + totalVal,
                "§7หน้าที่: [TBD]"
        ));

        // 2. Bonus Slot (Slot: R2)
        inv.setItem(bonusSlot, createItem(Material.DIAMOND, "§b§lBonus (" + statKey + ")", // Specify Stat in Bonus Name
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
                "§7แต้มที่คืน: §a" + (pendingCount > 0 ? costPreviousPoint : 0),
                "§7---",
                "§eคลิกซ้าย: §7ลด 1 แต้ม",
                "§eคลิกขวา: §7ลด 10 แต้ม"
        ));
    }

    // --- General Helpers ---

    private ItemStack createTabItem(Tab currentTab, Tab activeTab, Material mat, String name, String[] desc) {
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

    // Helper for 2-Column Formatting
    private String formatTwoColumns(String left, String right) {
        final int MAX_LENGTH = 38;
        String strippedLeft = left.replaceAll("§[0-9a-fk-or]", "");
        int padding = MAX_LENGTH - strippedLeft.length() - right.replaceAll("§[0-9a-fk-or]", "").length();
        if (padding < 1) padding = 1;

        return left + " ".repeat(padding) + right;
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES); // HIDE VANILLA LORE
        List<Component> loreList = new ArrayList<>();
        for (String line : lore) loreList.add(Component.text(line));
        meta.lore(loreList);
        item.setItemMeta(meta);
        return item;
    }
}
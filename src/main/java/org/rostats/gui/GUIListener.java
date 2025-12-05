package org.rostats.gui;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.rostats.ROStatsPlugin;
import org.rostats.data.Job;
import org.rostats.data.PlayerData;
import org.rostats.data.Skill;

public class GUIListener implements Listener {

    private final ROStatsPlugin plugin;
    private final Material RESET_ITEM = Material.NETHER_STAR;
    private final int FREE_RESETS = 3;

    public GUIListener(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().title().toString();

        if (title.contains("Main Menu")) {
            handleMainMenu(event);
        } else if (title.contains("Character Status") || title.contains("Job Selection") || title.contains("Select Active Skill")) {
            // เช็คปุ่ม Back ก่อนเลย (ใช้ร่วมกันได้ทุกเมนูย่อย)
            if (handleBackButton(event)) return;

            if (title.contains("Character Status")) handleStatusMenu(event);
            else if (title.contains("Job Selection")) handleJobMenu(event);
            else if (title.contains("Select Active Skill")) handleSkillMenu(event);
        }
    }

    // --- BUTTON HANDLERS ---

    private boolean handleBackButton(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return false;
        if (event.getCurrentItem() == null) return false;

        String name = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());

        if (name.contains("Back")) {
            event.setCancelled(true);
            new MainMenu(plugin).open(player); // กลับไปเมนูหลัก
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return true;
        }
        return false;
    }

    private void handleMainMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String name = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());

        if (name.contains("Status & Attributes")) {
            new StatusMenu(plugin).open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
        else if (name.contains("Class Info")) {
            new JobMenu(plugin).open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
        else if (name.contains("Active Skills")) {
            new SkillMenu(plugin).open(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
        else if (name.contains("Reset All")) {
            performResetCheck(player);
            player.closeInventory();
        }
        else if (name.contains("Close Menu")) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }

    private void performResetCheck(Player player) {
        PlayerData data = plugin.getStatManager().getData(player.getUniqueId());
        int usedResets = data.getResetCount();

        if (usedResets < FREE_RESETS) {
            doReset(player, data);
            player.sendMessage("§eFree Reset used! (" + (usedResets + 1) + "/" + FREE_RESETS + ")");
        } else {
            if (player.getInventory().contains(RESET_ITEM)) {
                player.getInventory().removeItem(new ItemStack(RESET_ITEM, 1));
                doReset(player, data);
                player.sendMessage("§bUsed 1x Nether Star to reset!");
            } else {
                player.sendMessage("§cNo free resets! You need 1x Nether Star.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }

    private void doReset(Player player, PlayerData data) {
        data.resetStats();
        data.incrementResetCount();
        plugin.getAttributeHandler().updatePlayerStats(player);
        plugin.getManaManager().updateBar(player);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
    }

    // --- SUB MENUS (Status, Job, Skill) ---
    // (Logic เดิม แต่ตัด Back button ออกเพราะดักไว้ข้างบนแล้ว)

    private void handleStatusMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String itemName = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());
        String statToUpgrade = null;

        if (itemName.contains("STR")) statToUpgrade = "STR";
        else if (itemName.contains("AGI")) statToUpgrade = "AGI";
        else if (itemName.contains("VIT")) statToUpgrade = "VIT";
        else if (itemName.contains("INT")) statToUpgrade = "INT";
        else if (itemName.contains("DEX")) statToUpgrade = "DEX";
        else if (itemName.contains("LUK")) statToUpgrade = "LUK";

        if (statToUpgrade != null) {
            boolean success = false;
            if (event.isLeftClick()) {
                success = plugin.getStatManager().upgradeStat(player, statToUpgrade);
            } else if (event.isRightClick()) {
                int count = 0;
                while (count < 10 && plugin.getStatManager().upgradeStat(player, statToUpgrade)) {
                    count++;
                    success = true;
                }
            }

            if (success) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                plugin.getAttributeHandler().updatePlayerStats(player);
                plugin.getManaManager().updateBar(player);
                new StatusMenu(plugin).open(player);
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                player.sendMessage("§cNot enough points!");
            }
        }
    }

    private void handleJobMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String jobName = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName()).replace("§6§l", "").trim();

        try {
            Job selectedJob = null;
            for (Job j : Job.values()) {
                if (jobName.equalsIgnoreCase(j.getDisplayName())) {
                    selectedJob = j;
                    break;
                }
            }

            if (selectedJob != null) {
                PlayerData data = plugin.getStatManager().getData(player.getUniqueId());
                data.setJob(selectedJob);
                player.sendMessage("§aYou changed your job to " + selectedJob.getDisplayName() + "!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                player.closeInventory();
                plugin.getAttributeHandler().updatePlayerStats(player);
                plugin.getManaManager().updateBar(player);
            }
        } catch (Exception e) {}
    }

    private void handleSkillMenu(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String displayName = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());

        if (displayName.contains("Unequip Skill")) {
            plugin.getStatManager().getData(player.getUniqueId()).setActiveSkill(Skill.NONE);
            player.sendMessage("§eSkill unequipped.");
            player.closeInventory();
            return;
        }

        for (Skill s : Skill.values()) {
            if (displayName.contains(s.getDisplayName())) {
                plugin.getStatManager().getData(player.getUniqueId()).setActiveSkill(s);
                player.sendMessage("§aSelected skill: " + s.getDisplayName());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                player.closeInventory();
                player.sendActionBar(net.kyori.adventure.text.Component.text("§eRight-click with weapon to use " + s.getDisplayName()));
                return;
            }
        }
    }
}
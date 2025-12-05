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
import org.rostats.data.PlayerData;
import org.rostats.gui.CharacterGUI.Tab;

public class GUIListener implements Listener {

    private final ROStatsPlugin plugin;

    public GUIListener(ROStatsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().title().toString();

        if (!title.contains("Character Status (ROO)")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        String name = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());

        // 1. Handle Tab Clicks (Slots 0, 1, 2)
        if (event.getSlot() == 0) new CharacterGUI(plugin).open(player, Tab.BASIC_INFO);
        else if (event.getSlot() == 1) new CharacterGUI(plugin).open(player, Tab.MORE_INFO);
        else if (event.getSlot() == 2) new CharacterGUI(plugin).open(player, Tab.RESET_CONFIRM);

            // 2. Handle Close Button (Slot 53)
        else if (event.getSlot() == 53) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }

        // 3. Handle Stat Allocation (Slots 19-44) - (Left/Right click logic retained from StatusMenu)
        else if (event.getSlot() >= 19 && event.getSlot() <= 44 && name.contains("STR") || name.contains("AGI") || name.contains("VIT") || name.contains("INT") || name.contains("DEX") || name.contains("LUK")) {
            handleStatUpgrade(player, name, event.isLeftClick(), event.isRightClick());
        }

        // 4. Handle Reset/Confirm Buttons
        else if (name.contains("[CONFIRM] Reset Stats")) {
            performReset(player);
            player.closeInventory();
        }
        else if (name.contains("[CANCEL]") && title.contains("RESET_CONFIRM")) {
            new CharacterGUI(plugin).open(player, Tab.BASIC_INFO);
        }
        // Note: Allocate (Confirm/Cancel) buttons (Slots 47, 51) are placeholders and need to be implemented for bulk changes.
    }

    private void handleStatUpgrade(Player player, String itemName, boolean isLeftClick, boolean isRightClick) {
        String statToUpgrade = null;
        if (itemName.contains("STR")) statToUpgrade = "STR";
        else if (itemName.contains("AGI")) statToUpgrade = "AGI";
        else if (itemName.contains("VIT")) statToUpgrade = "VIT";
        else if (itemName.contains("INT")) statToUpgrade = "INT";
        else if (itemName.contains("DEX")) statToUpgrade = "DEX";
        else if (itemName.contains("LUK")) statToUpgrade = "LUK";

        if (statToUpgrade != null) {
            boolean success = false;
            if (isLeftClick) {
                success = plugin.getStatManager().upgradeStat(player, statToUpgrade);
            } else if (isRightClick) {
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
                new CharacterGUI(plugin).open(player, Tab.BASIC_INFO); // Refresh GUI
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                player.sendMessage("§cNot enough points!");
            }
        }
    }

    private void performReset(Player player) {
        PlayerData data = plugin.getStatManager().getData(player.getUniqueId());
        int freeResets = plugin.getConfig().getInt("reset-system.free-resets", 3);
        int usedResets = data.getResetCount();
        Material resetItem = Material.getMaterial(plugin.getConfig().getString("reset-system.reset-item", "NETHER_STAR"));

        if (usedResets < freeResets) {
            data.resetStats();
            data.incrementResetCount();
            player.sendMessage("§eFree Reset used! (" + (usedResets + 1) + "/" + freeResets + ")");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
        } else if (resetItem != null && player.getInventory().contains(resetItem)) {
            player.getInventory().removeItem(new ItemStack(resetItem, 1));
            data.resetStats();
            data.incrementResetCount();
            player.sendMessage("§bUsed 1x " + resetItem.name() + " to reset!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
        } else {
            player.sendMessage("§cNo free resets! You need 1x " + resetItem.name() + ".");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }

        plugin.getAttributeHandler().updatePlayerStats(player);
        plugin.getManaManager().updateBar(player);
    }
}
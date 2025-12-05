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
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        if (!title.contains("Character Status (ROO)")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        String name = PlainTextComponentSerializer.plainText().serialize(event.getCurrentItem().displayName());
        int slot = event.getSlot();

        // 1. Handle Tab Clicks (Slots 9, 10, 11)
        if (slot == 9) new CharacterGUI(plugin).open(player, Tab.BASIC_INFO);
        else if (slot == 10) new CharacterGUI(plugin).open(player, Tab.MORE_INFO);
        else if (slot == 11) new CharacterGUI(plugin).open(player, Tab.RESET_CONFIRM);

            // 2. Handle Close Button (Slot 53)
        else if (slot == 8 || slot == 53) { // Close button in R0:8 and R5:53
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }

        // 3. Handle Reset/Confirm Buttons (ใน Tab.RESET_CONFIRM)
        else if (name.contains("[CONFIRM] Reset Stats")) {
            performReset(player);
            player.closeInventory();
        }
        else if (name.contains("[CANCEL]") && slot == 31) {
            new CharacterGUI(plugin).open(player, Tab.BASIC_INFO);
        }

        // 4. Handle Stat Allocation (+1 Buttons)
        String statKey = getStatKeyFromPlusSlot(slot);
        if (statKey != null && event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
            handleStatUpgrade(player, statKey, event.isLeftClick(), event.isRightClick());
        }
    }

    // Slot positions for +1 button based on the new layout (R5: 45-50)
    private String getStatKeyFromPlusSlot(int slot) {
        return switch (slot) {
            case 45 -> "STR";
            case 46 -> "AGI";
            case 47 -> "VIT";
            case 48 -> "INT";
            case 49 -> "DEX";
            case 50 -> "LUK";
            default -> null;
        };
    }

    private void handleStatUpgrade(Player player, String statKey, boolean isLeftClick, boolean isRightClick) {
        if (statKey == null) return;

        boolean success = false;
        if (isLeftClick) {
            success = plugin.getStatManager().upgradeStat(player, statKey);
        } else if (isRightClick) {
            int count = 0;
            while (count < 10 && plugin.getStatManager().upgradeStat(player, statKey)) {
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
            player.sendMessage("§cNo free resets! You need 1x " + (resetItem != null ? resetItem.name() : "NETHER_STAR") + ".");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        plugin.getAttributeHandler().updatePlayerStats(player);
        plugin.getManaManager().updateBar(player);
    }
}
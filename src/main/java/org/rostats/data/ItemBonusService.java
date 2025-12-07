package org.rostats.data;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.rostats.ROStatsPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to read and aggregate bonus stats from equipped items.
 * It reads custom data stored in the item's Persistent Data Container (PDC).
 */
public class ItemBonusService {

    private final ROStatsPlugin plugin;
    // NamespacedKey for the custom tag that holds all other bonus data
    private final NamespacedKey bonusKey;

    // Define all attribute keys that items can provide bonuses for.
    // This list must match the field names in PlayerData.java
    public static final String[] ATTRIBUTE_KEYS = {
            "pAtkBonusFlat", "mAtkBonusFlat", "critRes", "critDmgPercent", "critDmgResPercent",
            "pDmgBonusPercent", "mDmgBonusPercent", "pDmgBonusFlat", "mDmgBonusFlat",
            "pDmgReductionPercent", "mDmgReductionPercent", "meleePDmgPercent", "rangePDmgPercent",
            "meleePDReductionPercent", "rangePDReductionPercent", "pPenFlat", "mPenFlat",
            "pPenPercent", "mPenPercent", "ignorePDefFlat", "ignoreMDefFlat",
            "ignorePDefPercent", "ignoreMDefPercent", "aSpdPercent", "mSpdPercent",
            "varCTPercent", "varCTFlat", "fixedCTPercent", "fixedCTFlat",
            "healingEffectPercent", "healingReceivedPercent", "finalDmgPercent", "finalDmgResPercent",
            "finalPDmgPercent", "finalMDmgPercent", "pveDmgBonusPercent", "pvpDmgBonusPercent",
            "pveDmgReductionPercent", "pvpDmgReductionPercent", "maxHPPercent", "maxSPPercent",
            "lifestealPPercent", "lifestealMPercent", "trueDamageFlat", "shieldValueFlat",
            "shieldRatePercent", "weaponPAtk", "weaponMAtk", "hitBonusFlat", "fleeBonusFlat",
            "baseMSPD"
    };

    public ItemBonusService(ROStatsPlugin plugin) {
        this.plugin = plugin;
        // The main key under which the bonus container is stored
        this.bonusKey = new NamespacedKey(plugin, "rostats_item_bonus_container");
    }

    /**
     * Calculates the total bonus map from all equipped items (Armor, Main Hand, Off Hand).
     * @param player The player whose inventory to check.
     * @return A map of combined bonus attributes (Key -> Total Double Value).
     */
    public Map<String, Double> getCombinedItemBonuses(Player player) {
        Map<String, Double> combinedBonuses = new HashMap<>();

        // Aggregate all equipment into a single list
        ItemStack[] equipment = player.getInventory().getArmorContents();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        // Create a list of all equipped items to check
        ItemStack[] allEquipment = new ItemStack[equipment.length + 2];
        System.arraycopy(equipment, 0, allEquipment, 0, equipment.length);
        allEquipment[equipment.length] = mainHand;
        allEquipment[equipment.length + 1] = offHand;

        for (ItemStack item : allEquipment) {
            if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer itemRootPDC = meta.getPersistentDataContainer();

                // Check for the main bonus container tag
                if (itemRootPDC.has(bonusKey, PersistentDataType.TAG_CONTAINER)) {
                    PersistentDataContainer itemPDC = itemRootPDC.get(bonusKey, PersistentDataType.TAG_CONTAINER);

                    if (itemPDC != null) {
                        for (String key : ATTRIBUTE_KEYS) {
                            NamespacedKey attributeNSKey = new NamespacedKey(plugin, key);
                            // Read the double value for the specific attribute key
                            if (itemPDC.has(attributeNSKey, PersistentDataType.DOUBLE)) {
                                double bonus = itemPDC.get(attributeNSKey, PersistentDataType.DOUBLE);

                                // Merge logic: sum up bonuses for the same attribute from different items
                                combinedBonuses.merge(key, bonus, Double::sum);
                            }
                        }
                    }
                }
            }
        }

        return combinedBonuses;
    }
}
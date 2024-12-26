package net.colorixer.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class RegistryUtilForChopableClass {

    /**
     * Safely retrieves a Block from the registry using a full identifier string (e.g., "minecraft:oak_log").
     *
     * @param fullId The full identifier string.
     * @return The Block instance or null if not found.
     */
    public static Block getBlock(String fullId) {
        try {
            Identifier id = Identifier.tryParse(fullId);
            if (id != null) {
                return Registries.BLOCK.get(id);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving block with ID: " + fullId + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * Safely retrieves an Item from the registry using a full identifier string (e.g., "minecraft:iron_axe").
     *
     * @param fullId The full identifier string.
     * @return The Item instance or null if not found.
     */
    public static Item getItem(String fullId) {
        try {
            Identifier id = Identifier.tryParse(fullId);
            if (id != null) {
                return Registries.ITEM.get(id);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving item with ID: " + fullId + " - " + e.getMessage());
        }
        return null;
    }
}

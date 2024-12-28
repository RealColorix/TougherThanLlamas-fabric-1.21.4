package net.colorixer.util;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;

/**
 * Utility class for handling Identifier-related operations for the Chopable class.
 */
public class IdentifierUtilForChopableClass {

    /**
     * Creates an Identifier from namespace and path using tryParse.
     *
     * @param namespace The namespace (e.g., "ttll").
     * @param path The path (e.g., "chopable_reload").
     * @return The Identifier instance.
     * @throws IllegalArgumentException If the Identifier cannot be parsed.
     */
    public static Identifier createIdentifier(String namespace, String path) {
        Identifier id = Identifier.tryParse(namespace + ":" + path);
        if (id == null) {
            throw new IllegalArgumentException("Invalid Identifier: " + namespace + ":" + path);
        }
        return id;
    }

    /**
     * Determines if the given itemField represents a tag.
     *
     * @param itemField The item field from the JSON.
     * @return True if it represents a tag, false otherwise.
     */
    public static boolean isTag(String itemField) {
        // A valid tag must contain a colon and a hash, e.g., "minecraft:#axes".
        return itemField.contains(":") && itemField.contains("#");
    }

    /**
     * Checks whether the given toolStack matches the itemId (as a tag or specific item).
     *
     * @param toolStack The ItemStack to check.
     * @param itemId The item ID, either representing a tag (e.g., "modid:#tagname") or a specific item.
     * @return True if the toolStack matches the tag or item, false otherwise.
     */
    public static boolean matchesItemOrTag(ItemStack toolStack, String itemId) {
        if (itemId == null || toolStack == null) {
            throw new IllegalArgumentException("itemId and toolStack cannot be null");
        }

        // Split the itemId into modid and the remainder (which could be a tag or a specific item)
        int colonIndex = itemId.indexOf(':');
        if (colonIndex == -1) {
            // No colon found; assume it's a specific item without a namespace (default to "minecraft")
            String specificItemId = "minecraft:" + itemId;
            return matchesSpecificItem(specificItemId, toolStack);
        }

        String modid = itemId.substring(0, colonIndex);
        String remainder = itemId.substring(colonIndex + 1);

        if (remainder.startsWith("#")) {
            // Item ID represents a tag (format: "modid:#tagname")
            String tagName = remainder.substring(1); // Remove the '#' prefix
            Identifier tagIdentifier = Identifier.tryParse(modid + ":" + tagName);
            if (tagIdentifier == null) {
                throw new IllegalArgumentException("Invalid tag format: " + itemId);
            }

            TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, tagIdentifier);
            return toolStack.isIn(tag);
        } else {
            // Item ID represents a specific item (format: "modid:itemname")
            return matchesSpecificItem(itemId, toolStack);
        }
    }

    /**
     * Checks if the toolStack matches the specific item identified by itemId.
     *
     * @param itemId    The item ID (e.g., "minecraft:diamond_axe").
     * @param toolStack The ItemStack to check.
     * @return True if toolStack's item matches the itemId, false otherwise.
     * @throws IllegalArgumentException If the itemId format is invalid.
     */
    private static boolean matchesSpecificItem(String itemId, ItemStack toolStack) {
        Identifier id = Identifier.tryParse(itemId);
        if (id == null) {
            throw new IllegalArgumentException("Invalid item format: " + itemId);
        }

        Item item = Registries.ITEM.get(id);
        if (item == null) {
            throw new IllegalArgumentException("Item not found in registry: " + itemId);
        }

        return toolStack.getItem() == item;
    }
}

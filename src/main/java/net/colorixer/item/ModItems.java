package net.colorixer.item;

import net.colorixer.TougherThanLlamas;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item POINTY_STICK = registerItem("pointy_stick", new Item(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of("ttll", name), item);
    }

    public static void registerModItems() {
        TougherThanLlamas.LOGGER.info("Registering Items for " + TougherThanLlamas.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(POINTY_STICK);
        });
    }
}

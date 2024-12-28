package net.colorixer.item;

import net.colorixer.TougherThanLlamas;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {


    //Treesorts
    public static final Item OAK_FIREWOOD = registerItem("oak_firewood", Item::new, new Item.Settings());
    public static final Item BIRCH_FIREWOOD = registerItem("birch_firewood", Item::new, new Item.Settings());
    public static final Item JUNGLE_FIREWOOD = registerItem("jungle_firewood", Item::new, new Item.Settings());
    public static final Item SPRUCE_FIREWOOD = registerItem("spruce_firewood", Item::new, new Item.Settings());

    public static final Item OAK_BARK = registerItem("oak_bark", Item::new, new Item.Settings());
    public static final Item BIRCH_BARK = registerItem("birch_bark", Item::new, new Item.Settings());
    public static final Item JUNGLE_BARK = registerItem("jungle_bark", Item::new, new Item.Settings());
    public static final Item SPRUCE_BARK = registerItem("spruce_bark", Item::new, new Item.Settings());





    //AXES
    public static final Item FLINT_HATCHET_STRING = registerItem("flint_hatchet_string", Item::new, new Item.Settings());
    public static final Item FLINT_HATCHET_LEATHER_STRING = registerItem("flint_hatchet_leather_string", Item::new, new Item.Settings());
    public static final Item FLINT_HATCHET_TWINE = registerItem("flint_hatchet_twine",settings -> new AxeItem(TTLLToolMaterial.FLINT, 2.0F, -3.0F, settings), new Item.Settings().maxDamage(10));
    public static final Item FLINT_HATCHET_LEATHER_TWINE = registerItem("flint_hatchet_leather_twine", Item::new, new Item.Settings());
    public static final Item LEATHER_HANDLE = registerItem("leather_handle", Item::new, new Item.Settings());

    public static final Item POINTY_STICK = registerItem("pointy_stick", Item::new, new Item.Settings());
    public static final Item BRANCH = registerItem("branch", Item::new, new Item.Settings());
    public static final Item FLINT_FRAGMENT = registerItem("flint_fragment", Item::new, new Item.Settings());
    public static final Item SHARPEND_BONE = registerItem("sharpend_bone", Item::new, new Item.Settings());
    public static final Item TWINE = registerItem("twine", Item::new, new Item.Settings());
    public static final Item SMALL_POINTY_STICKS = registerItem("small_pointy_stick", Item::new, new Item.Settings());
    public static final Item KNITTING_STICKS = registerItem("knitting_sticks", Item::new, new Item.Settings());
    public static final Item FLINT_KNIFE = registerItem("flint_knife", settings -> new KnifeItem(TTLLToolMaterial.FLINT, 1.5F, -1.0F, settings), new Item.Settings());
    public static final Item GRASS_FIBER = registerItem("grass_fiber", Item::new, new Item.Settings());

    /** COLORED ITEMS ----------------------------------------------------------------------------------------------------------------------------------------------------***/

    // WOOL ITEMS ----------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item WHITE_WOOL = registerItem("white_wool", Item::new, new Item.Settings());

    // CLOTH ITEMS ----------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item CLOTH_WOOL_WHITE = registerItem("cloth_wool_white", Item::new, new Item.Settings());

    // KNITTING ITEMS ----------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item KNITTING_GRASS_FIBER = registerItem(
            "knitting_grass_fiber",
            settings -> new KnittingSticksItem(settings, 90, ModItems.TWINE, 1, SoundEvents.BLOCK_GRASS_STEP),
            new Item.Settings().maxCount(1));

    public static final Item KNITTING_WOOL_WHITE = registerItem(
            "knitting_wool_white",
            settings -> new KnittingSticksItem(settings, 540, ModItems.CLOTH_WOOL_WHITE, 1,SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1));


    // REGISTRATION ----------------------------------------------------------------------------------------------------------------------------------------------------


    public static Item registerItem(String path, Function<Item.Settings, Item> factory, Item.Settings settings) {
        final RegistryKey<Item> registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(TougherThanLlamas.MOD_ID, path));
        return Items.register(registryKey, factory, settings);
    }


    public static void registerModItems() {
        TougherThanLlamas.LOGGER.info("Registering Items for " + TougherThanLlamas.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(POINTY_STICK);
            entries.add(BRANCH);
            entries.add(FLINT_FRAGMENT);
            entries.add(SHARPEND_BONE);
            entries.add(FLINT_HATCHET_TWINE);
            entries.add(FLINT_HATCHET_STRING);
            entries.add(FLINT_HATCHET_LEATHER_TWINE);
            entries.add(FLINT_HATCHET_LEATHER_STRING);
            entries.add(LEATHER_HANDLE);
            entries.add(TWINE);
            entries.add(SMALL_POINTY_STICKS);
            entries.add(KNITTING_STICKS);
            entries.add(FLINT_KNIFE);
            entries.add(GRASS_FIBER);
            entries.add(KNITTING_GRASS_FIBER);
            entries.add(OAK_FIREWOOD);
            entries.add(OAK_BARK);
            entries.add(BIRCH_FIREWOOD);
            entries.add(BIRCH_BARK);
            entries.add(SPRUCE_FIREWOOD);
            entries.add(SPRUCE_BARK);
            entries.add(JUNGLE_FIREWOOD);
            entries.add(JUNGLE_BARK);
            entries.add(KNITTING_WOOL_WHITE);
            entries.add(CLOTH_WOOL_WHITE);
            entries.add(WHITE_WOOL);
        });
    }
}

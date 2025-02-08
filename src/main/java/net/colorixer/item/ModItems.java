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


    // Tree Sorts --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item OAK_FIREWOOD = registerItem("oak_firewood", Item::new, new Item.Settings());
    public static final Item BIRCH_FIREWOOD = registerItem("birch_firewood", Item::new, new Item.Settings());
    public static final Item JUNGLE_FIREWOOD = registerItem("jungle_firewood", Item::new, new Item.Settings());
    public static final Item SPRUCE_FIREWOOD = registerItem("spruce_firewood", Item::new, new Item.Settings());

    public static final Item OAK_BARK = registerItem("oak_bark", Item::new, new Item.Settings());
    public static final Item BIRCH_BARK = registerItem("birch_bark", Item::new, new Item.Settings());
    public static final Item JUNGLE_BARK = registerItem("jungle_bark", Item::new, new Item.Settings());
    public static final Item SPRUCE_BARK = registerItem("spruce_bark", Item::new, new Item.Settings());





    public static final Item CRUDE_BRUSH = registerItem("crude_brush", BrushItem::new, new Item.Settings().maxCount(1).maxDamage(16));
    public static final Item LEATHER_HANDLE = registerItem("leather_handle", Item::new, new Item.Settings());
    public static final Item ROCK = registerItem("rock", Item::new, new Item.Settings());
    public static final Item SHARP_ROCK = registerItem("sharp_rock", Item::new, new Item.Settings());


    public static final Item POINTY_STICK = registerItem("pointy_stick", Item::new, new Item.Settings());
    public static final Item BRANCH = registerItem("branch", Item::new, new Item.Settings());
    public static final Item FLINT_FRAGMENT = registerItem("flint_fragment", Item::new, new Item.Settings());
    public static final Item SHARPEND_BONE = registerItem("sharpend_bone", Item::new, new Item.Settings());
    public static final Item TWINE = registerItem("twine", Item::new, new Item.Settings());
    public static final Item SMALL_POINTY_STICKS = registerItem("small_pointy_stick", Item::new, new Item.Settings());
    public static final Item KNITTING_STICKS = registerItem("knitting_sticks", Item::new, new Item.Settings());
    public static final Item GRASS_FIBER = registerItem("grass_fiber", Item::new, new Item.Settings());
    public static final Item SPUNNED_STRING = registerItem("spunned_string", Item::new, new Item.Settings());

    /// TOOLS --------- FLINT HATCHES   ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------**/

    public static final Item FLINT_HATCHET = registerItem("flint_hatchet",settings -> new AxeItem(TTLLToolMaterial.FLINT_FOR_AXE, 1.8F, -3.1F, settings), new Item.Settings());
    public static final Item FLINT_HATCHET_LEATHER_GRIP = registerItem("flint_hatchet_leather_grip",settings -> new AxeItem(TTLLToolMaterial.FLINT_LEATHER_GRIP_FOR_AXE, 1.8F, -3.0F, settings), new Item.Settings());
    public static final Item FLINT_HATCHET_TWINE = registerItem("flint_hatchet_twine",settings -> new AxeItem(TTLLToolMaterial.FLINT_TWINE_FOR_AXE, 2.0F, -3.0F, settings), new Item.Settings());
    public static final Item FLINT_HATCHET_TWINE_LEATHER_GRIP = registerItem("flint_hatchet_twine_leather_grip",settings -> new AxeItem(TTLLToolMaterial.FLINT_TWINE_LEATHER_GRIP_FOR_AXE, 2.0F, -2.9F, settings), new Item.Settings());
    public static final Item FLINT_HATCHET_STRING = registerItem("flint_hatchet_string",settings -> new AxeItem(TTLLToolMaterial.FLINT_STRING_FOR_AXE, 2.2F, -2.9F, settings), new Item.Settings());
    public static final Item FLINT_HATCHET_STRING_LEATHER_GRIP = registerItem("flint_hatchet_string_leather_grip",settings -> new AxeItem(TTLLToolMaterial.FLINT_STRING_LEATHER_GRIP_FOR_AXE, 2.2F, -2.8F, settings), new Item.Settings());

    // CRUDE DRAXES ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item CRUDE_DRAX = registerItem("crude_drax",settings -> new DraxItem(TTLLToolMaterial.CRUDE_FOR_DRAX, 1.5F, -3.1F, settings), new Item.Settings());
    public static final Item CRUDE_DRAX_LEATHER_GRIP = registerItem("crude_drax_leather_grip",settings -> new DraxItem(TTLLToolMaterial.CRUDE_LEATHER_GRIP_FOR_DRAX, 1.5F, -3.0F, settings), new Item.Settings());
    public static final Item CRUDE_DRAX_TWINE = registerItem("crude_drax_twine",settings -> new DraxItem(TTLLToolMaterial.CRUDE_TWINE_FOR_DRAX, 1.5F, -3.0F, settings), new Item.Settings());
    public static final Item CRUDE_DRAX_TWINE_LEATHER_GRIP = registerItem("crude_drax_twine_leather_grip",settings -> new DraxItem(TTLLToolMaterial.CRUDE_TWINE_LEATHER_GRIP_FOR_DRAX, 1.5F, -2.9F, settings), new Item.Settings());
    public static final Item CRUDE_DRAX_STRING = registerItem("crude_drax_string",settings -> new DraxItem(TTLLToolMaterial.CRUDE_STRING_FOR_DRAX, 1.5F, -2.9F, settings), new Item.Settings());
    public static final Item CRUDE_DRAX_STRING_LEATHER_GRIP = registerItem("crude_drax_string_leather_grip",settings -> new DraxItem(TTLLToolMaterial.CRUDE_STRING_LEATHER_GRIP_FOR_DRAX, 1.5F, -2.8F, settings), new Item.Settings());

    // KNIVES ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item FLINT_KNIFE = registerItem("flint_knife", settings -> new KnifeItem(TTLLToolMaterial.FLINT_STRING_FOR_AXE, 1.5F, -1.0F, settings), new Item.Settings());

    /// COLORED ITEMS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------***/
    // WOOL ITEMS

    public static final Item WHITE_WOOL = registerItem("white_wool", Item::new, new Item.Settings());

    // CLOTH ITEMS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item CLOTH_WOOL_WHITE = registerItem("cloth_wool_white", Item::new, new Item.Settings());

    // KNITTING ITEMS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item KNITTING_GRASS_FIBER = registerItem(
            "knitting_grass_fiber",
            settings -> new KnittingSticksItem(settings, 90, ModItems.TWINE, 1, SoundEvents.BLOCK_GRASS_STEP),
            new Item.Settings().maxCount(1));

    public static final Item STRING_KNITTING = registerItem(
            "string_knitting",
            settings -> new KnittingSticksItem(settings, 270, ModItems.SPUNNED_STRING, 1, SoundEvents.BLOCK_COBWEB_HIT),
            new Item.Settings().maxCount(1));

    public static final Item KNITTING_WOOL_WHITE = registerItem(
            "knitting_wool_white",
            settings -> new KnittingSticksItem(settings, 540, ModItems.CLOTH_WOOL_WHITE, 1,SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1));


    // REGISTRATION ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


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
            entries.add(FLINT_HATCHET);
            entries.add(FLINT_HATCHET_LEATHER_GRIP);
            entries.add(FLINT_HATCHET_TWINE);
            entries.add(FLINT_HATCHET_TWINE_LEATHER_GRIP);
            entries.add(FLINT_HATCHET_STRING);
            entries.add(FLINT_HATCHET_STRING_LEATHER_GRIP);
            entries.add(SPUNNED_STRING);
            entries.add(STRING_KNITTING);
            entries.add(CRUDE_BRUSH);
            entries.add(CRUDE_DRAX);
            entries.add(CRUDE_DRAX_TWINE);
            entries.add(CRUDE_DRAX_STRING);
            entries.add(CRUDE_DRAX_LEATHER_GRIP);
            entries.add(CRUDE_DRAX_TWINE_LEATHER_GRIP);
            entries.add(CRUDE_DRAX_STRING_LEATHER_GRIP);
            entries.add(ROCK);
            entries.add(SHARP_ROCK);


        });
    }
}

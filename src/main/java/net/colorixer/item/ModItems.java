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



    //MOLDS

    public static final Item PICKAXE_MOLD = registerItem("pickaxe_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item AXE_MOLD = registerItem("axe_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item SWORD_MOLD = registerItem("sword_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item SHOVEL_MOLD = registerItem("shovel_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item HOE_MOLD = registerItem("hoe_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item NUGGET_MOLD = registerItem("nugget_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item INGOT_MOLD = registerItem("ingot_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item PLATE_MOLD = registerItem("plate_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item BUCKET_MOLD = registerItem("bucket_mold", MoldItem::new, new Item.Settings().maxCount(4));
    public static final Item CHISEL_MOLD = registerItem("chisel_mold", MoldItem::new, new Item.Settings().maxCount(4));


    //CASTS

    public static final Item PICKAXE_CAST = registerItem("pickaxe_cast", Item::new, new Item.Settings());
    public static final Item AXE_CAST = registerItem("axe_cast", Item::new, new Item.Settings());
    public static final Item SWORD_CAST = registerItem("sword_cast", Item::new, new Item.Settings());
    public static final Item SHOVEL_CAST = registerItem("shovel_cast", Item::new, new Item.Settings());
    public static final Item HOE_CAST = registerItem("hoe_cast", Item::new, new Item.Settings());
    public static final Item NUGGET_CAST = registerItem("nugget_cast", Item::new, new Item.Settings());
    public static final Item INGOT_CAST = registerItem("ingot_cast", Item::new, new Item.Settings());
    public static final Item PLATE_CAST = registerItem("plate_cast", Item::new, new Item.Settings());
    public static final Item BUCKET_CAST = registerItem("bucket_cast", Item::new, new Item.Settings());
    public static final Item CHISEL_CAST = registerItem("chisel_cast", Item::new, new Item.Settings());

    public static final Item IRON_PICKAXE_CAST = registerItem("iron_pickaxe_cast", Item::new, new Item.Settings());
    public static final Item IRON_AXE_CAST = registerItem("iron_axe_cast", Item::new, new Item.Settings());
    public static final Item IRON_SWORD_CAST = registerItem("iron_sword_cast", Item::new, new Item.Settings());
    public static final Item IRON_SHOVEL_CAST = registerItem("iron_shovel_cast", Item::new, new Item.Settings());
    public static final Item IRON_HOE_CAST = registerItem("iron_hoe_cast", Item::new, new Item.Settings());
    public static final Item IRON_NUGGET_CAST = registerItem("iron_nugget_cast", Item::new, new Item.Settings());
    public static final Item IRON_INGOT_CAST = registerItem("iron_ingot_cast", Item::new, new Item.Settings());
    public static final Item IRON_PLATE_CAST = registerItem("iron_plate_cast", Item::new, new Item.Settings());
    public static final Item IRON_BUCKET_CAST = registerItem("iron_bucket_cast", Item::new, new Item.Settings());
    public static final Item IRON_CHISEL_CAST = registerItem("iron_chisel_cast", Item::new, new Item.Settings());

    public static final Item GOLDEN_PICKAXE_CAST = registerItem("golden_pickaxe_cast", Item::new, new Item.Settings());
    public static final Item GOLDEN_AXE_CAST = registerItem("golden_axe_cast", Item::new, new Item.Settings());
    public static final Item GOLDEN_SWORD_CAST = registerItem("golden_sword_cast", Item::new, new Item.Settings());
    public static final Item GOLDEN_SHOVEL_CAST = registerItem("golden_shovel_cast", Item::new, new Item.Settings());
    public static final Item GOLDEN_HOE_CAST = registerItem("golden_hoe_cast", Item::new, new Item.Settings());
    public static final Item GOLD_NUGGET_CAST = registerItem("gold_nugget_cast", Item::new, new Item.Settings());
    public static final Item GOLD_INGOT_CAST = registerItem("gold_ingot_cast", Item::new, new Item.Settings());
    public static final Item GOLD_PLATE_CAST = registerItem("gold_plate_cast", Item::new, new Item.Settings());

    public static final Item COPPER_INGOT_CAST = registerItem("copper_ingot_cast", Item::new, new Item.Settings());
    public static final Item COPPER_PLATE_CAST = registerItem("copper_plate_cast", Item::new, new Item.Settings());

    // Tree Sorts --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item OAK_FIREWOOD = registerItem("oak_firewood", Item::new, new Item.Settings());
    public static final Item BIRCH_FIREWOOD = registerItem("birch_firewood", Item::new, new Item.Settings());
    public static final Item JUNGLE_FIREWOOD = registerItem("jungle_firewood", Item::new, new Item.Settings());
    public static final Item SPRUCE_FIREWOOD = registerItem("spruce_firewood", Item::new, new Item.Settings());

    public static final Item OAK_BARK = registerItem("oak_bark", Item::new, new Item.Settings());
    public static final Item BIRCH_BARK = registerItem("birch_bark", Item::new, new Item.Settings());
    public static final Item JUNGLE_BARK = registerItem("jungle_bark", Item::new, new Item.Settings());
    public static final Item SPRUCE_BARK = registerItem("spruce_bark", Item::new, new Item.Settings());

    public static final Item TREE_SAP = registerItem("tree_sap", Item::new, new Item.Settings());


    public static final Item DIRT_PILE = registerItem("dirt_pile", Item::new, new Item.Settings());
    public static final Item GRAVEL_PILE = registerItem("gravel_pile", Item::new, new Item.Settings());
    public static final Item SAND_PILE = registerItem("sand_pile", Item::new, new Item.Settings());
    public static final Item CRUDE_CLAY = registerItem("crude_clay", Item::new, new Item.Settings());

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

    /// ORES -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item COAL_DUST = registerItem("coal_dust", Item::new, new Item.Settings());

    public static final Item IRON_DUST = registerItem("iron_dust", Item::new, new Item.Settings());
    public static final Item GOLD_DUST = registerItem("gold_dust", Item::new, new Item.Settings());
    public static final Item COPPER_DUST = registerItem("copper_dust", Item::new, new Item.Settings());

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

    // CRUDE SHOVELS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item STONE_SHOVEL = registerItem("stone_shovel",settings -> new ShovelItem(TTLLToolMaterial.STONE_FOR_SHOVEL, 1f, -3f, settings), new Item.Settings());
    public static final Item STONE_SHOVEL_LEATHER_GRIP = registerItem("stone_shovel_leather_grip",settings -> new ShovelItem(TTLLToolMaterial.STONE_LEATHER_GRIP_FOR_SHOVEL, 1f, -3f, settings), new Item.Settings());
    public static final Item STONE_SHOVEL_TWINE = registerItem("stone_shovel_twine",settings -> new ShovelItem(TTLLToolMaterial.STONE_TWINE_FOR_SHOVEL, 1f, -3f, settings), new Item.Settings());
    public static final Item STONE_SHOVEL_TWINE_LEATHER_GRIP = registerItem("stone_shovel_twine_leather_grip",settings -> new ShovelItem(TTLLToolMaterial.STONE_TWINE_LEATHER_GRIP_FOR_SHOVEL, 1f, -3f, settings), new Item.Settings());
    public static final Item STONE_SHOVEL_STRING = registerItem("stone_shovel_string",settings -> new ShovelItem(TTLLToolMaterial.STONE_STRING_FOR_SHOVEL, 1f, -3f, settings), new Item.Settings());
    public static final Item STONE_SHOVEL_STRING_LEATHER_GRIP = registerItem("stone_shovel_string_leather_grip",settings -> new ShovelItem(TTLLToolMaterial.STONE_STRING_LEATHER_GRIP_FOR_SHOVEL, 1f, -3f, settings), new Item.Settings());

    // INDIVIDUAL ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item FLINT_KNIFE = registerItem("flint_knife", settings -> new KnifeItem(TTLLToolMaterial.FLINT_FOR_KNIFE, 1.5F, -1.0F, settings), new Item.Settings());

    public static final Item FLINT_CHISEL = registerItem("flint_chisel",settings -> new PickaxeItem(TTLLToolMaterial.FLINT_FOR_CHISEL, 1, -1.7F, settings), new Item.Settings());

    /// COLORED ITEMS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------***/
    // WOOL ITEMS

    public static final Item WHITE_WOOL = registerItem("white_wool", Item::new, new Item.Settings());

    // CLOTH ITEMS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item CLOTH_WOOL_WHITE = registerItem("cloth_wool_white", Item::new, new Item.Settings());


    // FIRESTARTER ITEMS ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    public static final Item FIRE_PLOUGH = registerItem("fire_plough", settings -> new FireStarterItem(settings, 0.00666), new Item.Settings().maxCount(1).maxDamage(200));
    public static final Item BOW_DRILL = registerItem("bow_drill", settings -> new FireStarterItem(settings, 0.01), new Item.Settings().maxCount(1).maxDamage(500));


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
            entries.add(DIRT_PILE);
            entries.add(GRAVEL_PILE);
            entries.add(SAND_PILE);
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
            entries.add(FLINT_CHISEL);
            entries.add(STONE_SHOVEL);
            entries.add(STONE_SHOVEL_LEATHER_GRIP);
            entries.add(STONE_SHOVEL_TWINE);
            entries.add(STONE_SHOVEL_TWINE_LEATHER_GRIP);
            entries.add(STONE_SHOVEL_STRING);
            entries.add(STONE_SHOVEL_STRING_LEATHER_GRIP);
            entries.add(CRUDE_CLAY);
            entries.add(SWORD_CAST);
            entries.add(PICKAXE_CAST);
            entries.add(AXE_CAST);
            entries.add(SHOVEL_CAST);
            entries.add(HOE_CAST);
            entries.add(CHISEL_CAST);
            entries.add(BUCKET_CAST);
            entries.add(NUGGET_CAST);
            entries.add(INGOT_CAST);
            entries.add(PLATE_CAST);
            entries.add(IRON_SWORD_CAST);
            entries.add(IRON_PICKAXE_CAST);
            entries.add(IRON_AXE_CAST);
            entries.add(IRON_SHOVEL_CAST);
            entries.add(IRON_HOE_CAST);
            entries.add(IRON_CHISEL_CAST);
            entries.add(IRON_BUCKET_CAST);
            entries.add(IRON_NUGGET_CAST);
            entries.add(IRON_INGOT_CAST);
            entries.add(IRON_PLATE_CAST);
            entries.add(GOLDEN_SWORD_CAST);
            entries.add(GOLDEN_PICKAXE_CAST);
            entries.add(GOLDEN_AXE_CAST);
            entries.add(GOLDEN_SHOVEL_CAST);
            entries.add(GOLDEN_HOE_CAST);
            entries.add(GOLD_NUGGET_CAST);
            entries.add(GOLD_INGOT_CAST);
            entries.add(GOLD_PLATE_CAST);
            entries.add(COPPER_INGOT_CAST);
            entries.add(COPPER_PLATE_CAST);
            entries.add(IRON_DUST);
            entries.add(GOLD_DUST);
            entries.add(COPPER_DUST);
            entries.add(SWORD_MOLD);
            entries.add(PICKAXE_MOLD);
            entries.add(AXE_MOLD);
            entries.add(SHOVEL_MOLD);
            entries.add(HOE_MOLD);
            entries.add(CHISEL_MOLD);
            entries.add(BUCKET_MOLD);
            entries.add(NUGGET_MOLD);
            entries.add(INGOT_MOLD);
            entries.add(PLATE_MOLD);
            entries.add(TREE_SAP);
            entries.add(COAL_DUST);
            entries.add(FIRE_PLOUGH);
            entries.add(BOW_DRILL);

        });
    }
}

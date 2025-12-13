package net.colorixer.item;

import net.colorixer.TougherThanLlamas;
import net.colorixer.item.items.*;
import net.colorixer.item.items.HoeItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {


    //MOLDS AND CASTS
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
    public static final Item FLAT_MOLD = registerItem("flat_mold", MoldItem::new, new Item.Settings().maxCount(4));
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
    public static final Item FLAT_CAST = registerItem("flat_cast", Item::new, new Item.Settings());
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

    // ---------- STONE AGE INGREDIENTS ----------- IN GROUPS OF WHAT THEY CONTAIN ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // ---------- BLOCK DROPS ----------
    public static final Item DIRT_PILE = registerItem("dirt_pile", Item::new, new Item.Settings());
    public static final Item GRAVEL_PILE = registerItem("gravel_pile", Item::new, new Item.Settings());
    public static final Item SAND_PILE = registerItem("sand_pile", Item::new, new Item.Settings());
    public static final Item CRUDE_CLAY = registerItem("crude_clay", Item::new, new Item.Settings());

    // ---------- SMALL ITEM DROPS ----------
    public static final Item ROCK = registerItem("rock", Item::new, new Item.Settings());
    public static final Item FLINT_FRAGMENT = registerItem("flint_fragment", Item::new, new Item.Settings());
    public static final Item GRASS_FIBER = registerItem("grass_fiber", Item::new, new Item.Settings());

    // ---------- ORE DROPS ----------
    public static final Item COAL_DUST = registerItem("coal_dust", Item::new, new Item.Settings());
    public static final Item IRON_DUST = registerItem("iron_dust", Item::new, new Item.Settings());
    public static final Item GOLD_DUST = registerItem("gold_dust", Item::new, new Item.Settings());
    public static final Item COPPER_DUST = registerItem("copper_dust", Item::new, new Item.Settings());

    // ---------- WOOD ----------
    public static final Item OAK_FIREWOOD = registerItem("oak_firewood", Item::new, new Item.Settings());
    public static final Item BIRCH_FIREWOOD = registerItem("birch_firewood", Item::new, new Item.Settings());
    public static final Item JUNGLE_FIREWOOD = registerItem("jungle_firewood", Item::new, new Item.Settings());
    public static final Item SPRUCE_FIREWOOD = registerItem("spruce_firewood", Item::new, new Item.Settings());
    public static final Item OAK_BARK = registerItem("oak_bark", Item::new, new Item.Settings());
    public static final Item BIRCH_BARK = registerItem("birch_bark", Item::new, new Item.Settings());
    public static final Item JUNGLE_BARK = registerItem("jungle_bark", Item::new, new Item.Settings());
    public static final Item SPRUCE_BARK = registerItem("spruce_bark", Item::new, new Item.Settings());
    public static final Item TREE_SAP = registerItem("tree_sap", Item::new, new Item.Settings());
    public static final Item SAW_DUST = registerItem("saw_dust", Item::new, new Item.Settings());
    public static final Item KNITTING_STICKS = registerItem("knitting_sticks", Item::new, new Item.Settings());
    public static final Item DRYING_RACK_LEG = registerItem("drying_rack_leg", Item::new, new Item.Settings());
    public static final Item FIRE_PLOUGH = registerItem("fire_plough", settings -> new FireStarterItem(settings, 0.01333), new Item.Settings().maxCount(1).maxDamage(200));
    public static final Item BOW_DRILL = registerItem("bow_drill", settings -> new FireStarterItem(settings, 0.025), new Item.Settings().maxCount(1).maxDamage(500));
    public static final Item POINTY_STICK = registerItem("pointy_stick", Item::new, new Item.Settings());
    public static final Item BRANCH = registerItem("branch", Item::new, new Item.Settings());
    public static final Item HANDLE = registerItem("handle", Item::new, new Item.Settings());


    // ---------- TOOLS & COMBAT ----------- FROM FIRST --> LAST THROUGH LINEAR PROGRESSION ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final Item FLINT_KNIFE = registerItem("flint_knife",settings -> new KnifeItem(TTLLToolMaterial.STONE, 1, -2.5F, settings), new Item.Settings());
    public static final Item FLINT = registerItem("flint",settings -> new DraxItem(TTLLToolMaterial.FLINT, 0.5f, -3.3F, settings), new Item.Settings());
    public static final Item SHARP_ROCK = registerItem("sharp_rock",settings -> new DraxItem(TTLLToolMaterial.SHARP_ROCK, 0.5f, -3.3F, settings), new Item.Settings());
    public static final Item FLINT_AXE = registerItem("flint_axe",settings -> new FlintAxeItem(TTLLToolMaterial.STONE, 1f, -3f, settings), new Item.Settings());
    public static final Item STONE_SHOVEL = registerItem("stone_shovel",settings -> new ShovelItem(TTLLToolMaterial.STONE, 1f, -3f, settings), new Item.Settings());
    public static final Item STONE_HOE = registerItem("stone_hoe",settings -> new HoeItem(TTLLToolMaterial.STONE, 1f, -3f, settings), new Item.Settings());
    public static final Item WOODEN_CLUB = registerItem("wooden_club",settings -> new SwordItem(TTLLToolMaterial.WOODEN_CLUB, 2f, -2.4F, settings), new Item.Settings());
    public static final Item ZOMBIE_ARM = registerItem("zombie_arm",settings -> new SwordItem(TTLLToolMaterial.ZOMBIE, 4f, -3.2F, settings), new Item.Settings());
    public static final Item BONE_CLUB = registerItem("bone_club",settings -> new SwordItem(TTLLToolMaterial.BONE, 5f, -3.4F, settings), new Item.Settings());

    // ---------- MOB DROPS ---------- NATURAL --> ANGRY ----------
    public static final Item TALLOW = registerItem("tallow", Item::new, new Item.Settings());
    public static final Item RAW_LEATHER = registerItem("raw_leather", Item::new, new Item.Settings());
    public static final Item LEATHER_CLOTH = registerItem("leather_cloth", Item::new, new Item.Settings());
    public static final Item MYSTERIOUS_GLAND = registerItem("mysterious_gland", Item::new, new Item.Settings());

    // ---------- MISCELLANEOUS ----------
    public static final Item LEATHER_BOOT = registerItem("leather_boot", Item::new, new Item.Settings());
    public static final Item CRUDE_BRUSH = registerItem("crude_brush", BrushItem::new, new Item.Settings().maxCount(1).maxDamage(16));
    public static final Item TWINE = registerItem("twine", Item::new, new Item.Settings());
    public static final Item SINEW = registerItem("sinew", Item::new, new Item.Settings());
    public static final Item SINEW_CHOPPING = registerItem("sinew_chopping", settings -> new KnittingSticksItem(settings, 720, ModItems.SINEW, 1,  ModItems.SHARP_ROCK,SoundEvents.BLOCK_COBWEB_HIT), new Item.Settings().maxCount(1));
    public static final Item FLINT_SINEW_CHOPPING = registerItem("flint_sinew_chopping", settings -> new KnittingSticksItem(settings, 720, ModItems.SINEW, 1,  ModItems.FLINT,SoundEvents.BLOCK_COBWEB_HIT), new Item.Settings().maxCount(1));
    public static final Item SPUNNED_STRING = registerItem("spunned_string", Item::new, new Item.Settings());

    // ---------- ANY WOOL RELATED ITEMS ---------- SORTED IN COLOR PER COLOR ----------

    public static final Item WHITE_WOOL = registerItem("white_wool", Item::new, new Item.Settings());
    public static final Item WHITE_GARN = registerItem("white_garn", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_WHITE = registerItem("cloth_wool_white", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_WHITE = registerItem("knitting_wool_white", settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_WHITE, 1, ModItems.KNITTING_STICKS,SoundEvents.BLOCK_WOOL_STEP), new Item.Settings().maxCount(1));
    public static final Item KNITTING_GARN_WHITE = registerItem("knitting_garn_white", settings -> new KnittingSticksItem(settings, 360, ModItems.WHITE_GARN, 1, ModItems.KNITTING_STICKS,SoundEvents.BLOCK_WOOL_STEP), new Item.Settings().maxCount(1));

    // ---------- ANY OTHER KNITTING ITEM
    public static final Item KNITTING_GRASS_FIBER = registerItem("knitting_grass_fiber", settings -> new KnittingSticksItem(settings, 150, ModItems.TWINE, 1,  ModItems.KNITTING_STICKS,SoundEvents.BLOCK_GRASS_STEP), new Item.Settings().maxCount(1));
    public static final Item STRING_KNITTING = registerItem("string_knitting", settings -> new KnittingSticksItem(settings, 270, ModItems.SPUNNED_STRING, 1,  ModItems.KNITTING_STICKS,SoundEvents.BLOCK_COBWEB_HIT), new Item.Settings().maxCount(1));
    public static final Item HANDLE_CARVING = registerItem("handle_carving", settings -> new KnittingSticksItem(settings, 45, ModItems.HANDLE, 1,  ModItems.SHARP_ROCK,SoundEvents.BLOCK_WOOD_HIT), new Item.Settings().maxCount(1));




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
            entries.add(SINEW);
            entries.add(DIRT_PILE);
            entries.add(GRAVEL_PILE);
            entries.add(SAND_PILE);
            entries.add(FLINT_FRAGMENT);
            entries.add(TWINE);
            entries.add(KNITTING_STICKS);
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
            entries.add(KNITTING_GARN_WHITE);
            entries.add(CLOTH_WOOL_WHITE);
            entries.add(WHITE_WOOL);
            entries.add(WHITE_GARN);
            entries.add(SPUNNED_STRING);
            entries.add(STRING_KNITTING);
            entries.add(CRUDE_BRUSH);
            entries.add(ROCK);
            entries.add(LEATHER_CLOTH);
            entries.add(LEATHER_BOOT);
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
            entries.add(FLAT_CAST);
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
            entries.add(FLAT_MOLD);
            entries.add(TREE_SAP);
            entries.add(TALLOW);
            entries.add(COAL_DUST);
            entries.add(FIRE_PLOUGH);
            entries.add(BOW_DRILL);
            entries.add(SAW_DUST);
            entries.add(MYSTERIOUS_GLAND);
            entries.add(RAW_LEATHER);
            entries.add(DRYING_RACK_LEG);
            entries.add(SINEW_CHOPPING);
            entries.add(FLINT_SINEW_CHOPPING);
            entries.add(HANDLE);
            entries.add(HANDLE_CARVING);

            entries.add(FLINT);
        });


        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(WOODEN_CLUB);
            entries.add(ZOMBIE_ARM);
            entries.add(BONE_CLUB);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(FLINT_KNIFE);
            entries.add(FLINT_AXE);
            entries.add(STONE_SHOVEL);
            entries.add(STONE_HOE);
            entries.add(SHARP_ROCK);
        });
    }
}

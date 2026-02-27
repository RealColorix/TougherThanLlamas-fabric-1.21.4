package net.colorixer.item;

import net.colorixer.TougherThanLlamas;
import net.colorixer.block.ModBlocks;
import net.colorixer.block.torch.BurningCrudeTorchItem;
import net.colorixer.block.torch.CrudeTorchItem;
import net.colorixer.entity.ModEntities;
import net.colorixer.item.items.*;
import net.colorixer.item.items.firestarteritem.FireStarterItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class ModItems {


    //TORCH
    public static final Item BURNING_CRUDE_TORCH_ITEM = registerItem("burning_crude_torch",
            settings -> new BurningCrudeTorchItem(ModBlocks.BURNING_CRUDE_TORCH, settings), new Item.Settings());
    public static final Item CRUDE_TORCH_ITEM = registerItem("crude_torch",
            settings -> new CrudeTorchItem(ModBlocks.CRUDE_TORCH, settings), new Item.Settings());


    //SPAWN EGGS
    // Change this:
    public static final Item FIRE_CREEPER_SPAWN_EGG = registerItem("fire_creeper_spawn_egg",
            settings -> new SpawnEggItem(ModEntities.FIRE_CREEPER, settings), new Item.Settings());
    public static final Item JUNGLE_SPIDER_SPAWN_EGG = registerItem("jungle_spider_spawn_egg",
            settings -> new SpawnEggItem(ModEntities.JUNGLE_SPIDER, settings), new Item.Settings());



    // ---------- STONE AGE INGREDIENTS ----------- IN GROUPS OF WHAT THEY CONTAIN ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    // ---------- BLOCK DROPS ----------
    public static final Item DIRT_PILE = registerItem("dirt_pile", Item::new, new Item.Settings());
    public static final Item GRAVEL_PILE = registerItem("gravel_pile", Item::new, new Item.Settings());
    public static final Item SAND_PILE = registerItem("sand_pile", Item::new, new Item.Settings());
    public static final Item CRUDE_CLAY = registerItem("crude_clay", Item::new, new Item.Settings());
    public static final Item LAPIS_LAZULI_DUST = registerItem("lapis_lazuli_dust", Item::new, new Item.Settings());

    // ---------- SMALL ITEM DROPS ----------
    public static final Item ROCK = registerItem("rock", Item::new, new Item.Settings());
    public static final Item CHIPPED_ROCK = registerItem("chipped_rock", Item::new, new Item.Settings());

    public static final Item FLINT_FRAGMENT = registerItem("flint_fragment", Item::new, new Item.Settings());

    // ---------- ORE DROPS ----------
    public static final Item COAL_DUST = registerItem("coal_dust", Item::new, new Item.Settings());
    public static final Item IRON_DUST = registerItem("iron_dust", Item::new, new Item.Settings());
    public static final Item GOLD_DUST = registerItem("gold_dust", Item::new, new Item.Settings());
    public static final Item COPPER_DUST = registerItem("copper_dust", Item::new, new Item.Settings());

    //----------- SCROLLS ----------

    public static final Item SCROLL_OF_ECHOES = registerItem("scroll_of_echoes", Item::new, new Item.Settings().rarity(Rarity.RARE).maxCount(1));
    public static final Item SCROLL_OF_OCULUS = registerItem("scroll_of_oculus", Item::new, new Item.Settings().rarity(Rarity.RARE).maxCount(1));
    public static final Item SCROLL_OF_ARCANE = registerItem("scroll_of_arcane", Item::new, new Item.Settings().rarity(Rarity.RARE).maxCount(1));


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
    public static final Item FIRE_PLOUGH = registerItem("fire_plough", settings -> new FireStarterItem(settings, 0.00666), new Item.Settings().maxCount(1).maxDamage(200));
    public static final Item BOW_DRILL = registerItem("bow_drill", settings -> new FireStarterItem(settings, 0.025), new Item.Settings().maxCount(1).maxDamage(500));
    public static final Item POINTY_STICK = registerItem("pointy_stick", Item::new, new Item.Settings());
    public static final Item BRANCH = registerItem("branch", Item::new, new Item.Settings());
    public static final Item HEMP = registerItem("hemp", Item::new, new Item.Settings());
    public static final Item BURNED_MEAT = registerItem("burned_meat",
            (settings) -> new Item(settings.food(ModFoodComponents.BURNED_MEAT)), new Item.Settings());

    public static final Item DIAMOND_INGOT = registerItem("diamond_ingot", Item::new, new Item.Settings());


    // ---------- TOOLS & COMBAT ----------- FROM FIRST --> LAST THROUGH LINEAR PROGRESSION ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static final Item FLINT = registerItem("flint",settings -> new DraxItem(TTLLToolMaterial.FLINT, 0.5f, -3.3F, settings), new Item.Settings());
    public static final Item SHARP_ROCK = registerItem("sharp_rock",settings -> new DraxItem(TTLLToolMaterial.SHARP_ROCK, 0.5f, -3.3F, settings), new Item.Settings());
    public static final Item FLINT_AXE = registerItem("flint_axe",settings -> new FlintAxeItem(TTLLToolMaterial.STONE_AXE, 1f, -3f, settings), new Item.Settings());
    public static final Item STONE_SHOVEL = registerItem("stone_shovel",settings -> new ShovelItem(TTLLToolMaterial.STONE, 1f, -3f, settings), new Item.Settings());
    public static final Item IRON_CHISEL = registerItem("iron_chisel",settings -> new DraxItem(TTLLToolMaterial.STONE, 1f, -3f, settings), new Item.Settings());
    public static final Item WOODEN_CLUB = registerItem("wooden_club",settings -> new SwordItem(TTLLToolMaterial.WOODEN_CLUB, 1f, -2.4F, settings), new Item.Settings());
    public static final Item BONE_CLUB = registerItem("bone_club",settings -> new SwordItem(TTLLToolMaterial.BONE, 3f, -3.0F, settings), new Item.Settings());

    // ---------- MOB DROPS ---------- NATURAL --> ANGRY ----------
    public static final Item TALLOW = registerItem("tallow", Item::new, new Item.Settings());
    public static final Item BANDAGES = registerItem("bandages", BandageItem::new, new Item.Settings());
    public static final Item RAW_LEATHER = registerItem("raw_leather", Item::new, new Item.Settings());
    public static final Item LEATHER_CLOTH = registerItem("leather_cloth", Item::new, new Item.Settings());
    public static final Item MYSTERIOUS_GLAND = registerItem("mysterious_gland", Item::new, new Item.Settings());
    public static final Item TANGLED_WEB = registerItem("tangled_web", Item::new, new Item.Settings());
    public static final Item CREEPER_SACK = registerItem("creeper_sack", Item::new, new Item.Settings());
    public static final Item FIRE_CREEPER_SACK = registerItem("fire_creeper_sack", Item::new, new Item.Settings().fireproof());
    public static final Item SUGAR_CANE_MASH = registerItem(
            "sugar_cane_mash",
            settings -> new SugarcaneMashItem(
                    settings,
                    Items.SUGAR, // The item it drops (Swap this with your custom Fibers item later!)
                    1,           // The amount it drops
                    SoundEvents.ENTITY_GENERIC_SPLASH // The sound it plays while washing
            ),
            new Item.Settings()
    );

    // ---------- MISCELLANEOUS ----------
    public static final Item LEATHER_BOOT = registerItem("leather_boot", Item::new, new Item.Settings());
    public static final Item CRUDE_BRUSH = registerItem("crude_brush", BrushItem::new, new Item.Settings().maxCount(1).maxDamage(16));
   public static final Item SINEW = registerItem("sinew", Item::new, new Item.Settings());
    public static final Item SINEW_CHOPPING = registerItem("sinew_chopping", settings -> new KnittingSticksItem(settings, 720, ModItems.SINEW, 1,  ModItems.SHARP_ROCK,SoundEvents.BLOCK_COBWEB_HIT), new Item.Settings().maxCount(1));
    public static final Item FLINT_SINEW_CHOPPING = registerItem("flint_sinew_chopping", settings -> new KnittingSticksItem(settings, 720, ModItems.SINEW, 1,  ModItems.FLINT,SoundEvents.BLOCK_COBWEB_HIT), new Item.Settings().maxCount(1));
    public static final Item SPUNNED_STRING = registerItem("spunned_string", Item::new, new Item.Settings());

    // ---------- ANY WOOL RELATED ITEMS ---------- SORTED IN COLOR PER COLOR ----------

    // WHITE
    public static final Item WHITE_WOOL = registerItem("white_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_WHITE = registerItem("cloth_wool_white", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_WHITE = registerItem(
            "knitting_wool_white",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_WHITE, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // ORANGE
    public static final Item ORANGE_WOOL = registerItem("orange_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_ORANGE = registerItem("cloth_wool_orange", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_ORANGE = registerItem(
            "knitting_wool_orange",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_ORANGE, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // MAGENTA
    public static final Item MAGENTA_WOOL = registerItem("magenta_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_MAGENTA = registerItem("cloth_wool_magenta", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_MAGENTA = registerItem(
            "knitting_wool_magenta",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_MAGENTA, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // LIGHT_BLUE
    public static final Item LIGHT_BLUE_WOOL = registerItem("light_blue_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_LIGHT_BLUE = registerItem("cloth_wool_light_blue", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_LIGHT_BLUE = registerItem(
            "knitting_wool_light_blue",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_LIGHT_BLUE, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // YELLOW
    public static final Item YELLOW_WOOL = registerItem("yellow_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_YELLOW = registerItem("cloth_wool_yellow", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_YELLOW = registerItem(
            "knitting_wool_yellow",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_YELLOW, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // LIME
    public static final Item LIME_WOOL = registerItem("lime_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_LIME = registerItem("cloth_wool_lime", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_LIME = registerItem(
            "knitting_wool_lime",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_LIME, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // PINK
    public static final Item PINK_WOOL = registerItem("pink_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_PINK = registerItem("cloth_wool_pink", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_PINK = registerItem(
            "knitting_wool_pink",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_PINK, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // GRAY
    public static final Item GRAY_WOOL = registerItem("gray_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_GRAY = registerItem("cloth_wool_gray", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_GRAY = registerItem(
            "knitting_wool_gray",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_GRAY, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // LIGHT_GRAY
    public static final Item LIGHT_GRAY_WOOL = registerItem("light_gray_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_LIGHT_GRAY = registerItem("cloth_wool_light_gray", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_LIGHT_GRAY = registerItem(
            "knitting_wool_light_gray",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_LIGHT_GRAY, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // CYAN
    public static final Item CYAN_WOOL = registerItem("cyan_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_CYAN = registerItem("cloth_wool_cyan", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_CYAN = registerItem(
            "knitting_wool_cyan",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_CYAN, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // PURPLE
    public static final Item PURPLE_WOOL = registerItem("purple_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_PURPLE = registerItem("cloth_wool_purple", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_PURPLE = registerItem(
            "knitting_wool_purple",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_PURPLE, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // BLUE
    public static final Item BLUE_WOOL = registerItem("blue_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_BLUE = registerItem("cloth_wool_blue", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_BLUE = registerItem(
            "knitting_wool_blue",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_BLUE, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // BROWN
    public static final Item BROWN_WOOL = registerItem("brown_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_BROWN = registerItem("cloth_wool_brown", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_BROWN = registerItem(
            "knitting_wool_brown",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_BROWN, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // GREEN
    public static final Item GREEN_WOOL = registerItem("green_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_GREEN = registerItem("cloth_wool_green", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_GREEN = registerItem(
            "knitting_wool_green",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_GREEN, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // RED
    public static final Item RED_WOOL = registerItem("red_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_RED = registerItem("cloth_wool_red", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_RED = registerItem(
            "knitting_wool_red",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_RED, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );

    // BLACK
    public static final Item BLACK_WOOL = registerItem("black_wool", Item::new, new Item.Settings());
    public static final Item CLOTH_WOOL_BLACK = registerItem("cloth_wool_black", Item::new, new Item.Settings());
    public static final Item KNITTING_WOOL_BLACK = registerItem(
            "knitting_wool_black",
            settings -> new KnittingSticksItem(settings, 720, ModItems.CLOTH_WOOL_BLACK, 1, ModItems.KNITTING_STICKS, SoundEvents.BLOCK_WOOL_STEP),
            new Item.Settings().maxCount(1)
    );
    // ---------- ANY OTHER KNITTING ITEM
    public static final Item STRING_KNITTING = registerItem("string_knitting", settings -> new KnittingSticksItem(settings, 270, ModItems.SPUNNED_STRING, 1,  ModItems.KNITTING_STICKS,SoundEvents.BLOCK_COBWEB_HIT), new Item.Settings().maxCount(1));
    public static final Item WEB_UNTANGLING = registerItem("web_untangling", settings -> new KnittingSticksItem(settings, 180, Items.STRING, 1,  ModItems.FLINT,SoundEvents.BLOCK_COBWEB_HIT), new Item.Settings().maxCount(1));




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
            entries.add(KNITTING_STICKS);
            entries.add(OAK_FIREWOOD);
            entries.add(OAK_BARK);
            entries.add(BIRCH_FIREWOOD);
            entries.add(BIRCH_BARK);
            entries.add(SPRUCE_FIREWOOD);
            entries.add(SPRUCE_BARK);
            entries.add(JUNGLE_FIREWOOD);
            entries.add(JUNGLE_BARK);
            //
            entries.add(WHITE_WOOL);
            entries.add(ORANGE_WOOL);
            entries.add(MAGENTA_WOOL);
            entries.add(LIGHT_BLUE_WOOL);
            entries.add(YELLOW_WOOL);
            entries.add(LIME_WOOL);
            entries.add(PINK_WOOL);
            entries.add(GRAY_WOOL);
            entries.add(LIGHT_GRAY_WOOL);
            entries.add(CYAN_WOOL);
            entries.add(PURPLE_WOOL);
            entries.add(BLUE_WOOL);
            entries.add(BROWN_WOOL);
            entries.add(GREEN_WOOL);
            entries.add(RED_WOOL);
            entries.add(BLACK_WOOL);
            //
            entries.add(CLOTH_WOOL_WHITE);
            entries.add(CLOTH_WOOL_ORANGE);
            entries.add(CLOTH_WOOL_MAGENTA);
            entries.add(CLOTH_WOOL_LIGHT_BLUE);
            entries.add(CLOTH_WOOL_YELLOW);
            entries.add(CLOTH_WOOL_LIME);
            entries.add(CLOTH_WOOL_PINK);
            entries.add(CLOTH_WOOL_GRAY);
            entries.add(CLOTH_WOOL_LIGHT_GRAY);
            entries.add(CLOTH_WOOL_CYAN);
            entries.add(CLOTH_WOOL_PURPLE);
            entries.add(CLOTH_WOOL_BLUE);
            entries.add(CLOTH_WOOL_BROWN);
            entries.add(CLOTH_WOOL_GREEN);
            entries.add(CLOTH_WOOL_RED);
            entries.add(CLOTH_WOOL_BLACK);
            //
            entries.add(KNITTING_WOOL_WHITE);
            entries.add(KNITTING_WOOL_ORANGE);
            entries.add(KNITTING_WOOL_MAGENTA);
            entries.add(KNITTING_WOOL_LIGHT_BLUE);
            entries.add(KNITTING_WOOL_YELLOW);
            entries.add(KNITTING_WOOL_LIME);
            entries.add(KNITTING_WOOL_PINK);
            entries.add(KNITTING_WOOL_GRAY);
            entries.add(KNITTING_WOOL_LIGHT_GRAY);
            entries.add(KNITTING_WOOL_CYAN);
            entries.add(KNITTING_WOOL_PURPLE);
            entries.add(KNITTING_WOOL_BLUE);
            entries.add(KNITTING_WOOL_BROWN);
            entries.add(KNITTING_WOOL_GREEN);
            entries.add(KNITTING_WOOL_RED);
            entries.add(KNITTING_WOOL_BLACK);
            //
            entries.add(SUGAR_CANE_MASH);
            entries.add(BANDAGES);
            entries.add(SPUNNED_STRING);
            entries.add(STRING_KNITTING);
            entries.add(CRUDE_BRUSH);
            entries.add(ROCK);
            entries.add(CHIPPED_ROCK);
            entries.add(LEATHER_CLOTH);
            entries.add(LEATHER_BOOT);
            entries.add(CRUDE_CLAY);
            entries.add(IRON_CHISEL);
            entries.add(IRON_DUST);
            entries.add(GOLD_DUST);
            entries.add(COPPER_DUST);
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
            entries.add(TANGLED_WEB);
            entries.add(WEB_UNTANGLING);
            entries.add(FLINT);
            entries.add(CREEPER_SACK);
            entries.add(FIRE_CREEPER_SACK);
            entries.add(SCROLL_OF_ARCANE);
            entries.add(SCROLL_OF_ECHOES);
            entries.add(SCROLL_OF_OCULUS);
            entries.add(LAPIS_LAZULI_DUST);
            entries.add(DIAMOND_INGOT);
            entries.add(BURNED_MEAT);
            entries.add(HEMP);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(JUNGLE_SPIDER_SPAWN_EGG);
            entries.add(FIRE_CREEPER_SPAWN_EGG);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(WOODEN_CLUB);
            entries.add(BONE_CLUB);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(FLINT_AXE);
            entries.add(STONE_SHOVEL);
            entries.add(SHARP_ROCK);
        });
    }
}

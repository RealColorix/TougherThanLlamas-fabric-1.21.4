package net.colorixer.block;

import net.colorixer.TougherThanLlamas;
import net.colorixer.block.brick_block.DriedBrickBlock;
import net.colorixer.block.brick_block.WetBrickBlock;
import net.colorixer.block.brick_furnace.BrickFurnaceBlock;
import net.colorixer.block.drying_rack.DryingRackBlock;
import net.colorixer.block.logs.StemBlock;
import net.colorixer.block.logs.TrunkBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import java.util.function.Function;

import static net.minecraft.block.Blocks.createLightLevelFromLitBlockState;


public class ModBlocks {



    public static final Block COBWEB_FUll = registerBlock(
            "cobweb_full",
            CobwebBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.WHITE_GRAY)
                    .sounds(BlockSoundGroup.COBWEB)
                    .solid()
                    .noCollision()
                    .requiresTool()
                    .strength(4.0F)
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .nonOpaque()
    );

    public static final Block DRYING_RACK = registerBlock("drying_rack", DryingRackBlock::new, Block.Settings.create()
            .mapColor(MapColor.TERRACOTTA_BROWN).strength(0.05F, 0F).sounds(BlockSoundGroup.WOOD));



    public static final Block DRIED_BRICK = registerBlock("dried_brick", DriedBrickBlock::new, Block.Settings.create()
            .mapColor(MapColor.DULL_PINK).strength(0.2F, 0.5F).sounds(BlockSoundGroup.STONE));
    public static final Block BRICK_SIDING = registerBlock("brick_siding", BrickBlockSiding::new, Block.Settings.create()
            .mapColor(MapColor.DULL_PINK).strength(1.0F, 4.0F).sounds(BlockSoundGroup.STONE).requiresTool());
    public static final Block WET_BRICK = registerBlock("wet_brick", WetBrickBlock::new, Block.Settings.copy(Blocks.CLAY));


    //TILE ENTITES

    public static final Block BRICK_FURNACE = registerBlock("brick_furnace", BrickFurnaceBlock::new, Block.Settings.create()
            .mapColor(MapColor.DARK_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.5F, 5.8F)
            .luminance(state -> state.get(BrickFurnaceBlock.LIT) ? 13 : 0)
            .nonOpaque()
    );


    //  SLABS



    public static final Block GRAVEL_SLAB = registerBlock("gravel_slab", FallingSlabBlock::new, Block.Settings.copy(Blocks.GRAVEL));
    public static final Block SAND_SLAB = registerBlock("sand_slab", FallingSlabBlock::new, Block.Settings.copy(Blocks.SAND));
    public static final Block LOOSE_DIRT_SLAB = registerBlock("loose_dirt_slab", FallingSlabBlock::new, Block.Settings.copy(Blocks.DIRT));



    /** STONE **/

    public static final Block BEDSTONE = registerBlock("bedstone", Block::new, Block.Settings.create()
            .mapColor(MapColor.BLACK).strength(6.0F, 6F).sounds(BlockSoundGroup.STONE).requiresTool());


    public static final Block WEATHERED_STONE = registerBlock("weathered_stone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.48F, 5.95F).sounds(BlockSoundGroup.STONE));
    public static final Block COBBLESTONE = registerBlock("cobblestone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.46F, 5.9F).sounds(BlockSoundGroup.STONE));
    public static final Block CRACKED_STONE = registerBlock("cracked_stone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.44F, 5.85F).sounds(BlockSoundGroup.STONE));
    public static final Block SHATTERED_STONE = registerBlock("shattered_stone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.5F, 5.8F).sounds(BlockSoundGroup.STONE));
    public static final Block EXCAVATED_STONE = registerBlock("excavated_stone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.5F, 5.8F).sounds(BlockSoundGroup.STONE));




    /** WOOD **/
    public static final Block OAK_TRUNK = registerBlock("oak_trunk", TrunkBlock::new, Block.Settings.create()
                    .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.OAK_TAN).sounds(BlockSoundGroup.WOOD));
    public static final Block OAK_STEM = registerBlock("oak_stem", net.colorixer.block.logs.StemBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.OAK_TAN).sounds(BlockSoundGroup.WOOD));

    public static final Block BIRCH_TRUNK = registerBlock("birch_trunk", TrunkBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.WHITE_GRAY).sounds(BlockSoundGroup.WOOD));
    public static final Block BIRCH_STEM = registerBlock("birch_stem", net.colorixer.block.logs.StemBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.WHITE_GRAY).sounds(BlockSoundGroup.WOOD));

    public static final Block JUNGLE_TRUNK = registerBlock("jungle_trunk", TrunkBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.BROWN).sounds(BlockSoundGroup.WOOD));
    public static final Block JUNGLE_STEM = registerBlock("jungle_stem", net.colorixer.block.logs.StemBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.BROWN).sounds(BlockSoundGroup.WOOD));

    public static final Block SPRUCE_TRUNK = registerBlock("spruce_trunk", TrunkBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.SPRUCE_BROWN).sounds(BlockSoundGroup.WOOD));
    public static final Block SPRUCE_STEM = registerBlock("spruce_stem", StemBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.SPRUCE_BROWN).sounds(BlockSoundGroup.WOOD));

    public static final Block WICKER = registerBlock(
            "wicker", ShortPlantBlock::new,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.DARK_GREEN)
                    .replaceable()
                    .noCollision()
                    .breakInstantly()
                    .sounds(BlockSoundGroup.GRASS)
                    .offset(AbstractBlock.OffsetType.XYZ)
                    .burnable()
                    .pistonBehavior(PistonBehavior.DESTROY)
                    .nonOpaque()
    );
    public static final Block RAW_MYTHRIL = registerBlock("raw_mythril", Block::new, Block.Settings.create()
            .mapColor(MapColor.LICHEN_GREEN).instrument(NoteBlockInstrument.BASEDRUM).sounds(BlockSoundGroup.STONE).requiresTool().strength(15F, 200F));

    public static final Block BEDSTONE_DIAMOND_ORE = registerBlock("bedstone_diamond_ore",
                    settings -> new ExperienceDroppingBlock(
                            UniformIntProvider.create(3, 7), settings
                    ), Block.Settings.create().hardness(6.0f).resistance(4.0f).instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.STONE));
    public static final Block BEDSTONE_COAL_ORE = registerBlock("bedstone_coal_ore",
            settings -> new ExperienceDroppingBlock(
                    UniformIntProvider.create(0, 2), settings
            ), Block.Settings.create().hardness(6.0f).resistance(4.0f).instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.STONE));
    public static final Block BEDSTONE_LAPIS_ORE = registerBlock("bedstone_lapis_ore",
            settings -> new ExperienceDroppingBlock(
                    UniformIntProvider.create(2, 5), settings
            ), Block.Settings.create().hardness(6.0f).resistance(4.0f).instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.STONE));
    public static final Block BEDSTONE_EMERALD_ORE = registerBlock("bedstone_emerald_ore",
            settings -> new ExperienceDroppingBlock(
                    UniformIntProvider.create(3, 7), settings
            ), Block.Settings.create().hardness(6.0f).resistance(4.0f).instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.STONE));

    public static final Block BEDSTONE_REDSTONE_ORE = registerBlock(
            "bedstone_redstone_ore",
            RedstoneOreBlock::new,
            Block.Settings.create()
                    .hardness(6.0F)
                    .resistance(4.0F)
                    .instrument(NoteBlockInstrument.BASS)
                    .requiresTool()
                    .mapColor(MapColor.DEEPSLATE_GRAY)
                    .sounds(BlockSoundGroup.STONE)
                    .ticksRandomly()
                    .luminance(createLightLevelFromLitBlockState(9))
    );
    public static final Block BEDSTONE_IRON_ORE = registerBlock("bedstone_iron_ore",
            settings -> new ExperienceDroppingBlock(
                    ConstantIntProvider.create(0), settings
            ), Block.Settings.create().hardness(6.0f).resistance(4.0f).instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.STONE));
    public static final Block BEDSTONE_GOLD_ORE = registerBlock("bedstone_gold_ore",
            settings -> new ExperienceDroppingBlock(
                    ConstantIntProvider.create(0), settings
            ), Block.Settings.create().hardness(6.0f).resistance(4.0f).instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.STONE));
    public static final Block BEDSTONE_COPPER_ORE = registerBlock("bedstone_copper_ore",
            settings -> new ExperienceDroppingBlock(
                    ConstantIntProvider.create(0), settings
            ), Block.Settings.create().hardness(6.0f).resistance(4.0f).instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.DEEPSLATE_GRAY).sounds(BlockSoundGroup.STONE));




    private static Block registerBlock(String path, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        final Identifier identifier = Identifier.of("ttll", path);
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);

        final Block block = Blocks.register(registryKey, factory, settings);
        Items.register(block);
        return block;
    }

    public static void registerModBlocks() {
        TougherThanLlamas.LOGGER.info("Registering Mod Blocks for " + TougherThanLlamas.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> {
            entries.add(ModBlocks.OAK_TRUNK);
            entries.add(ModBlocks.OAK_STEM);
            entries.add(ModBlocks.WEATHERED_STONE);
            entries.add(ModBlocks.COBBLESTONE);
            entries.add(ModBlocks.CRACKED_STONE);
            entries.add(ModBlocks.SHATTERED_STONE);
            entries.add(ModBlocks.EXCAVATED_STONE);
            entries.add(ModBlocks.BEDSTONE);
            entries.add(ModBlocks.GRAVEL_SLAB);
            entries.add(ModBlocks.SAND_SLAB);
            entries.add(ModBlocks.LOOSE_DIRT_SLAB);
            entries.add(ModBlocks.BRICK_FURNACE);
            entries.add(ModBlocks.DRIED_BRICK);
            entries.add(ModBlocks.WET_BRICK);
            entries.add(ModBlocks.BRICK_SIDING);
            entries.add(ModBlocks.DRYING_RACK);

            entries.add(ModBlocks.COBWEB_FUll);

            entries.add(ModBlocks.RAW_MYTHRIL);
            entries.add(ModBlocks.BEDSTONE_DIAMOND_ORE);
            entries.add(ModBlocks.BEDSTONE_EMERALD_ORE);
            entries.add(ModBlocks.BEDSTONE_REDSTONE_ORE);
            entries.add(ModBlocks.BEDSTONE_LAPIS_ORE);
            entries.add(ModBlocks.BEDSTONE_GOLD_ORE);
            entries.add(ModBlocks.BEDSTONE_IRON_ORE);
            entries.add(ModBlocks.BEDSTONE_COPPER_ORE);
            entries.add(ModBlocks.BEDSTONE_COAL_ORE);

        });
    }
}

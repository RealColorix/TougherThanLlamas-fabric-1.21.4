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

import java.util.function.Function;


public class ModBlocks {



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
    public static final Block WEATHERED_STONE = registerBlock("weathered_stone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.48F, 5.95F));
    public static final Block COBBLESTONE = registerBlock("cobblestone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.46F, 5.9F));
    public static final Block CRACKED_STONE = registerBlock("cracked_stone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.44F, 5.85F));
    public static final Block SHATTERED_STONE = registerBlock("shattered_stone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.5F, 5.8F));
    public static final Block EXCAVATED_STONE = registerBlock("excavated_stone", Block::new, Block.Settings.create()
            .mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(1.5F, 5.8F));




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
            entries.add(ModBlocks.GRAVEL_SLAB);
            entries.add(ModBlocks.SAND_SLAB);
            entries.add(ModBlocks.LOOSE_DIRT_SLAB);
            entries.add(ModBlocks.BRICK_FURNACE);
            entries.add(ModBlocks.DRIED_BRICK);
            entries.add(ModBlocks.WET_BRICK);
            entries.add(ModBlocks.BRICK_SIDING);
            entries.add(ModBlocks.DRYING_RACK);
        });
    }
}

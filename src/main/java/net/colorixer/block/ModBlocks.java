package net.colorixer.block;

import net.colorixer.TougherThanLlamas;
import net.colorixer.block.campfire.CampfireBlock;
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

import static net.colorixer.block.campfire.CampfireBlock.STATE;

public class ModBlocks {

    public static final Block OAK_TRUNK = registerBlock("oak_trunk", TrunkBlock::new, Block.Settings.create()
                    .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.OAK_TAN).sounds(BlockSoundGroup.WOOD));
    public static final Block OAK_STEM = registerBlock("oak_stem", StemBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.OAK_TAN).sounds(BlockSoundGroup.WOOD));

    public static final Block BIRCH_TRUNK = registerBlock("birch_trunk", TrunkBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.WHITE_GRAY).sounds(BlockSoundGroup.WOOD));
    public static final Block BIRCH_STEM = registerBlock("birch_stem", StemBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.WHITE_GRAY).sounds(BlockSoundGroup.WOOD));

    public static final Block JUNGLE_TRUNK = registerBlock("jungle_trunk", TrunkBlock::new, Block.Settings.create()
            .strength(2f).burnable().instrument(NoteBlockInstrument.BASS).requiresTool().mapColor(MapColor.BROWN).sounds(BlockSoundGroup.WOOD));
    public static final Block JUNGLE_STEM = registerBlock("jungle_stem", StemBlock::new, Block.Settings.create()
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


    public static final Block CAMPFIRE = registerBlock("campfire", CampfireBlock::new, Block.Settings.create()
            .mapColor(MapColor.SPRUCE_BROWN)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque()
            .luminance(state ->
                    state.get(STATE) == 1 ? 7 :
                            state.get(STATE) == 2 ? 12 :
                                    state.get(STATE) == 3 ? 15 :
                                            0) // Default light level for other states
            .burnable());


    private static Block registerBlock(String path, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        final Identifier identifier = Identifier.of("ttll", path);
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);

        final Block block = Blocks.register(registryKey, factory, settings);
        Items.register(block);
        return block;
    }

    public static void registerModBlocks() {
        TougherThanLlamas.LOGGER.info("Registering Mod Blocks for " + TougherThanLlamas.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(ModBlocks.OAK_TRUNK);
            entries.add(ModBlocks.OAK_STEM);
            entries.add(ModBlocks.CAMPFIRE);
            entries.add(ModBlocks.WICKER);
        });
    }
}

package net.colorixer.block;

import net.colorixer.TougherThanLlamas;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

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
        });
    }
}

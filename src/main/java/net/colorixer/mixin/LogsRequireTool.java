package net.colorixer.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.Direction;
import net.minecraft.block.MapColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Mixin to overwrite the createLogSettings method in the Blocks class
 * to add requiresTool() to the block settings.
 */
@Mixin(Blocks.class)
public abstract class LogsRequireTool {

    /**
     * Overwrites the createLogSettings method to include requiresTool().
     *
     * <p>
     * This modification ensures that all log blocks require the appropriate tool
     * to be harvested, enhancing gameplay balance and realism.
     * </p>
     *
     * @author
     *     [Your Name]
     * @reason
     *     Enforce that log blocks require a tool to harvest, preventing
     *     unintended block breaking without the correct tool.
     *
     * @param topMapColor   The map color for the top of the log.
     * @param sideMapColor  The map color for the sides of the log.
     * @param sounds        The sound group for the log.
     * @return Modified AbstractBlock.Settings with requiresTool().
     */
    @Overwrite
    public static AbstractBlock.Settings createLogSettings(MapColor topMapColor, MapColor sideMapColor, BlockSoundGroup sounds) {
        return AbstractBlock.Settings.create()
                .mapColor(state -> state.get(PillarBlock.AXIS) == Direction.Axis.Y ? topMapColor : sideMapColor)
                .instrument(NoteBlockInstrument.BASS)
                .strength(2.0F)
                .requiresTool() // Added requiresTool()
                .sounds(sounds)
                .burnable();
    }
}

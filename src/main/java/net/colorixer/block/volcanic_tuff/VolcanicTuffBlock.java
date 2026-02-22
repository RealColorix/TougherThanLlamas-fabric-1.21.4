package net.colorixer.block.volcanic_tuff;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VolcanicTuffBlock extends Block {
    public VolcanicTuffBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        // If the new block is not the same as the old one (meaning it was broken/replaced)
        if (!state.isOf(newState.getBlock())) {
            // Check if the world is server-side to avoid ghost blocks
            if (!world.isClient) {
                // Set the position to lava
                world.setBlockState(pos, Blocks.LAVA.getDefaultState());
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
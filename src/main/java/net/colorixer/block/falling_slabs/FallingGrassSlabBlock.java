package net.colorixer.block.falling_slabs;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class FallingGrassSlabBlock extends FallingSlabBlock {

    public FallingGrassSlabBlock(Settings settings) {
        super(settings.ticksRandomly());
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        // Grass death logic
        if (state.get(TYPE) == SlabType.DOUBLE) {
            BlockState aboveState = world.getBlockState(pos.up());
            if (aboveState.isFullCube(world, pos.up())
                    || (aboveState.contains(TYPE) && aboveState.get(TYPE) == SlabType.BOTTOM)) {
                convertToDirt(state, world, pos);
                return;
            }
        }

        attemptSpread(world, pos, random);
    }

    private void attemptSpread(ServerWorld world, BlockPos origin, Random random) {

        if (random.nextInt(4) != 0) return;

        BlockPos.Mutable targetPos = new BlockPos.Mutable();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {

                    targetPos.set(
                            origin.getX() + dx,
                            origin.getY() + dy,
                            origin.getZ() + dz
                    );

                    BlockState targetState = world.getBlockState(targetPos);
                    BlockPos abovePos = targetPos.up();
                    BlockState aboveState = world.getBlockState(abovePos);
                    FluidState aboveFluid = aboveState.getFluidState();

                    // No spread if liquid above
                    if (!aboveFluid.isEmpty()) continue;

                    // Blocked by full block above
                    if (aboveState.isFullCube(world, abovePos)) continue;

                    // Blocked by bottom slab above
                    if (aboveState.contains(TYPE) && aboveState.get(TYPE) == SlabType.BOTTOM) continue;

                    // DIRT → vanilla grass block
                    if (targetState.isOf(Blocks.DIRT)) {
                        world.setBlockState(
                                targetPos,
                                Blocks.GRASS_BLOCK.getDefaultState(),
                                Block.NOTIFY_ALL
                        );
                        continue;
                    }

                    // LOOSE_DIRT_SLAB → GRASS_SLAB
                    if (targetState.isOf(ModBlocks.LOOSE_DIRT_SLAB)) {

                        // Reject waterlogged bottom slabs
                        if (targetState.get(TYPE) == SlabType.BOTTOM
                                && targetState.get(WATERLOGGED)) {
                            continue;
                        }

                        world.setBlockState(
                                targetPos,
                                ModBlocks.GRASS_SLAB.getDefaultState()
                                        .with(TYPE, targetState.get(TYPE))
                                        .with(WATERLOGGED, targetState.get(WATERLOGGED)),
                                Block.NOTIFY_ALL
                        );
                    }
                }
            }
        }
    }

    private void convertToDirt(BlockState state, ServerWorld world, BlockPos pos) {
        world.setBlockState(
                pos,
                ModBlocks.LOOSE_DIRT_SLAB.getDefaultState()
                        .with(TYPE, state.get(TYPE))
                        .with(WATERLOGGED, state.get(WATERLOGGED)),
                Block.NOTIFY_ALL
        );
    }
}

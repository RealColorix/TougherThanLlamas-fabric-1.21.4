package net.colorixer.block.falling_slabs;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class FallingGrassSlabBlock extends FallingSlabBlock {

    public FallingGrassSlabBlock(Settings settings) {
        super(settings.ticksRandomly());
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        // 1. DEATH CHECKS
        if (state.get(WATERLOGGED)) {
            convertToDirt(state, world, pos);
            return;
        }

        if (!world.getFluidState(pos.up()).isEmpty()) {
            convertToDirt(state, world, pos);
            return;
        }

        BlockPos abovePos = pos.up();
        BlockState aboveState = world.getBlockState(abovePos);

        if (!aboveState.isOf(Blocks.SNOW)) {

            boolean hasFullDownFace =
                    aboveState.isSideSolidFullSquare(world, abovePos, Direction.DOWN);

            boolean isBottomSlab =
                    aboveState.contains(Properties.SLAB_TYPE) &&
                            aboveState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM;

            if (hasFullDownFace || isBottomSlab) {
                // Trigger death logic instead of instant dirt
                convertToDirt(state, world, pos);
                return;
            }
        }

        // 2. SPREAD LOGIC (With 1-in-7 chance and Light Check)
        if (world.getLightLevel(pos.up()) >= 9) {
            if (random.nextInt(7) == 0) { // Throttle spread speed
                attemptSpread(world, pos, random);
            }
        }
    }

    private void attemptSpread(ServerWorld world, BlockPos origin, Random random) {
        // Pick ONE random offset
        int dx = random.nextInt(3) - 1;
        int dy = random.nextInt(5) - 3;
        int dz = random.nextInt(3) - 1;

        BlockPos targetPos = origin.add(dx, dy, dz);
        BlockState targetState = world.getBlockState(targetPos);
        BlockPos abovePos = targetPos.up();
        BlockState aboveState = world.getBlockState(abovePos);

        // --- DEATH CHECK ---
        if (!aboveState.isOf(Blocks.SNOW)) {
            boolean hasFullDownFace = aboveState.isSideSolidFullSquare(world, abovePos, Direction.DOWN);
            boolean isBottomSlab = aboveState.contains(Properties.SLAB_TYPE) && aboveState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM;

            if (hasFullDownFace || isBottomSlab) {
                if (targetState.isOf(Blocks.GRASS_BLOCK) || targetState.isOf(ModBlocks.GRASS_SLAB)) {
                    // Note: You might need a more generic dirt conversion here
                    // if targetPos isn't a slab, but since this is a Slab class:
                    world.setBlockState(targetPos, Blocks.DIRT.getDefaultState());
                }
                return;
            }
        }

        // --- VALIDATION ---
        if (world.getLightLevel(abovePos) < 4 || aboveState.getOpacity() > 2) return;
        if (!world.getFluidState(abovePos).isEmpty()) return;

        // --- SPREAD ---
        if (targetState.isOf(Blocks.DIRT)) {
            world.setBlockState(targetPos, Blocks.GRASS_BLOCK.getDefaultState(), Block.NOTIFY_ALL);
        } else if (targetState.isOf(ModBlocks.LOOSE_DIRT_SLAB)) {
            if (targetState.contains(Properties.WATERLOGGED) && targetState.get(Properties.WATERLOGGED)) return;

            world.setBlockState(
                    targetPos,
                    ModBlocks.GRASS_SLAB.getDefaultState()
                            .with(Properties.SLAB_TYPE, targetState.get(Properties.SLAB_TYPE))
                            .with(Properties.WATERLOGGED, false),
                    Block.NOTIFY_ALL
            );
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
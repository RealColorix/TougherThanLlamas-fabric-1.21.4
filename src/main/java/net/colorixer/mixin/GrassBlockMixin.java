package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.state.property.Properties.WATERLOGGED;

// TARGET SPREADABLEBLOCK INSTEAD OF GRASSBLOCK
@Mixin(SpreadableBlock.class)
public abstract class GrassBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        // Only apply this logic to Grass Blocks (and not Mycelium, unless you want it to)
        if (!state.isOf(Blocks.GRASS_BLOCK)) return;

        // 1. DEATH CHECK: Submerged or Suffocated
        if (!world.getFluidState(pos.up()).isEmpty()) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            ci.cancel();
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
                ci.cancel();
                return;
            }
        }

        // 2. LIGHT CHECK & RANDOM THROTTLE (1-in-7)
        if (world.getLightLevel(pos.up()) >= 9) {
            if (random.nextInt(25) == 0) {
                attemptSpread(world, pos, random);
            }
        }

        ci.cancel();
    }
    @Unique
    private void convertToDirt(BlockState state, ServerWorld world, BlockPos pos) {
        world.setBlockState(
                pos,
                Blocks.DIRT.getDefaultState(),
                Block.NOTIFY_ALL
        );
    }

    @Unique
    private void attemptSpread(ServerWorld world, BlockPos origin, Random random) {
        // Pick ONE random offset within the 3x5x3 area
        int dx = random.nextInt(3) - 1; // -1, 0, or 1
        int dy = random.nextInt(5) - 3; // -3, -2, -1, 0, or 1
        int dz = random.nextInt(3) - 1; // -1, 0, or 1

        BlockPos targetPos = origin.add(dx, dy, dz);
        BlockState targetState = world.getBlockState(targetPos);
        BlockPos abovePos = targetPos.up();
        BlockState aboveState = world.getBlockState(abovePos);

        // --- DEATH CHECK FOR TARGET ---
        // If the target is already grass but covered, kill it
        if (!aboveState.isOf(Blocks.SNOW)) {
            boolean hasFullDownFace = aboveState.isSideSolidFullSquare(world, abovePos, Direction.DOWN);
            boolean isBottomSlab = aboveState.contains(Properties.SLAB_TYPE) && aboveState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM;

            if (hasFullDownFace || isBottomSlab) {
                if (targetState.isOf(Blocks.GRASS_BLOCK) || targetState.isOf(ModBlocks.GRASS_SLAB)) {
                    convertToDirt(targetState, world, targetPos);
                }
                return; // End attempt
            }
        }

        // --- VALIDATION FOR SPREAD ---
        if (world.getLightLevel(abovePos) < 4 || aboveState.getOpacity() > 2) return;
        if (!world.getFluidState(abovePos).isEmpty()) return;

        // --- PERFORM SPREAD ---
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
}
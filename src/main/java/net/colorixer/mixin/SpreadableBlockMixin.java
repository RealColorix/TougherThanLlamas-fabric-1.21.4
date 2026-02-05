package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpreadableBlock.class)
public abstract class SpreadableBlockMixin {

    @Inject(method = "canSurvive", at = @At("RETURN"), cancellable = true)
    private static void injectSlabAndFullBlockCheck(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        BlockPos abovePos = pos.up();
        BlockState aboveState = world.getBlockState(abovePos);

        if (aboveState.isFullCube(world, abovePos)) {
            cir.setReturnValue(false);
            return;
        }

        if (aboveState.getBlock() instanceof SlabBlock && aboveState.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void checkCustomSlabSpread(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        // Vanilla grass only ticks if light is high enough
        if (world.getLightLevel(pos.up()) < 9) return;

        // Try spreading 4 times (matching vanilla behavior)
        for (int i = 0; i < 4; i++) {
            BlockPos targetPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            BlockState targetState = world.getBlockState(targetPos);

            if (targetState.isOf(ModBlocks.LOOSE_DIRT_SLAB)) {
                BlockPos targetAbovePos = targetPos.up();
                BlockState targetAboveState = world.getBlockState(targetAbovePos);

                // Check if spreading is blocked by the block ABOVE the target
                boolean blocked = targetAboveState.isFullCube(world, targetAbovePos) ||
                        targetAboveState.isOpaque() ||
                        !targetAboveState.getFluidState().isEmpty();

                if (!blocked && targetAboveState.getBlock() instanceof SlabBlock) {
                    if (targetAboveState.get(SlabBlock.TYPE) == SlabType.BOTTOM) {
                        blocked = true;
                    }
                }

                if (!blocked) {
                    // Final check: Reject waterlogged bottom slabs
                    boolean isWaterloggedBottom = targetState.get(SlabBlock.TYPE) == SlabType.BOTTOM && targetState.get(Properties.WATERLOGGED);

                    if (!isWaterloggedBottom) {
                        world.setBlockState(targetPos, ModBlocks.GRASS_SLAB.getDefaultState()
                                .with(SlabBlock.TYPE, targetState.get(SlabBlock.TYPE))
                                .with(Properties.WATERLOGGED, targetState.get(Properties.WATERLOGGED)));
                    }
                }
            }
        }
    }
}
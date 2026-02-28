package net.colorixer.mixin;

import net.colorixer.block.ModBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class CoarseDirtTicksMixin {

    // 1. Force Coarse Dirt to join the random tick loop
    @Inject(method = "hasRandomTicks", at = @At("HEAD"), cancellable = true)
    private void makeCoarseDirtTick(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.isOf(Blocks.COARSE_DIRT)) {
            cir.setReturnValue(true);
        }
    }

    // 2. Handle the transformation logic
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.isOf(Blocks.COARSE_DIRT)) {
            if (random.nextInt(25) == 0 && isGrassNearby(world, pos)) {
                world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            }
        }
    }

    @Unique
    private boolean isGrassNearby(ServerWorld world, BlockPos pos) {
        // Checks a 1-block radius (3x3x3 cube)
        for (BlockPos neighborPos : BlockPos.iterate(pos.add(-1, -2, -1), pos.add(1, 2, 1))) {
            if (world.getBlockState(neighborPos).isOf(Blocks.GRASS_BLOCK)||world.getBlockState(neighborPos).isOf(ModBlocks.GRASS_SLAB)) {
                return true;
            }
        }
        return false;
    }
}
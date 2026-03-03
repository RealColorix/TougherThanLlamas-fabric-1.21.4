package net.colorixer.mixin.blocks.seasonal;

import net.colorixer.util.SeasonTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.block.IceBlock.class)
public abstract class IceBlock {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void ttll$meltIceInSpring(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        long day = (SeasonTracker.activeSeasonDay - 12 + 96) % 96L;

        // If it is Spring, Summer, or early Fall
        if (day >= 72 || day < 36) {
            // Naturally snowy biomes (like Ice Spikes) never melt!
            if (world.getBiome(pos).value().getTemperature() < 0.15f) return;

            // Turn the ice block back into a water source block
            world.setBlockState(pos, Blocks.WATER.getDefaultState());
            ci.cancel();
        }
    }
}
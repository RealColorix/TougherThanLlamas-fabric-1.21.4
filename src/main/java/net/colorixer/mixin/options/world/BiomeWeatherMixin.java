package net.colorixer.mixin.options.world;

import net.colorixer.util.SeasonTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeWeatherMixin {

    // 1. VISUAL WEATHER: Changes falling Rain into falling Snow!
    // 1. VISUAL WEATHER: Changes falling Rain into falling Snow!
    @Inject(method = "isCold", at = @At("HEAD"), cancellable = true)
    private void ttll$forceVisualSnow(BlockPos pos, int seaLevel, CallbackInfoReturnable<Boolean> cir) {
        Biome biome = (Biome) (Object) this;
        if (biome.getTemperature() >= 0.9f) return; // Deserts stay hot

        long day = (SeasonTracker.activeSeasonDay - 12 + 96) % 96L;
        if (day >= 36 && day < 72) {
            cir.setReturnValue(true); // Yes, it is cold enough to snow!
        }
    }

    // 2. ICE FORMATION: Freezes water source blocks
    // FIXED: We explicitly tell Mixin to target the method that takes a (WorldView, BlockPos, boolean)
    @Inject(method = "canSetIce(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Z)Z", at = @At("HEAD"), cancellable = true)
    private void ttll$forceWinterIce(WorldView world, BlockPos pos, boolean doWaterCheck, CallbackInfoReturnable<Boolean> cir) {
        Biome biome = (Biome) (Object) this;
        if (biome.getTemperature() >= 0.9f) return;

        long day = (SeasonTracker.activeSeasonDay - 12 + 96) % 96L;
        if (day >= 36 && day < 72) {

            if (!SeasonTracker.hasSnowedThisWinter && !(world instanceof net.minecraft.server.world.ServerWorld)) {
                cir.setReturnValue(false);
                return;
            }

            if (doWaterCheck && world.getFluidState(pos).getFluid() == net.minecraft.fluid.Fluids.WATER) {
                cir.setReturnValue(true);
            }
        }
    }

    // 3. SNOW LAYERS: Piles snow on the ground during storms
    // 3. SNOW LAYERS: Piles snow on the ground during storms
    @Inject(method = "canSetSnow", at = @At("HEAD"), cancellable = true)
    private void ttll$forceWinterSnowLayers(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Biome biome = (Biome) (Object) this;
        if (biome.getTemperature() >= 0.9f) return;

        long day = (SeasonTracker.activeSeasonDay - 12 + 96) % 96L;
        if (day >= 36 && day < 72) {

            // --- THE WEATHER MEMORY CHECK ---
            // If it hasn't snowed yet, ONLY allow live weather (ServerWorld) to place snow.
            // If it HAS snowed, this check is bypassed and new chunks will generate with snow!
            if (!SeasonTracker.hasSnowedThisWinter && !(world instanceof net.minecraft.server.world.ServerWorld)) {
                cir.setReturnValue(false);
                return;
            }

            BlockState currentState = world.getBlockState(pos);
            BlockState blockBelow = world.getBlockState(pos.down());

            // 1. Prevent snow from spawning INSIDE leaves
            if (currentState.isIn(net.minecraft.registry.tag.BlockTags.LEAVES)) {
                cir.setReturnValue(false);
                return;
            }

            // 2. Prevent snow from spawning ON TOP of water or ice
            if (blockBelow.isOf(net.minecraft.block.Blocks.WATER) ||
                    blockBelow.isOf(net.minecraft.block.Blocks.ICE) ||
                    blockBelow.isOf(net.minecraft.block.Blocks.FROSTED_ICE)) {
                cir.setReturnValue(false);
                return;
            }

            // --- VANILLA SAFETY CHECKS ---
            if (world.getLightLevel(net.minecraft.world.LightType.BLOCK, pos) < 10) {
                if ((currentState.isAir() || currentState.isOf(net.minecraft.block.Blocks.SNOW)) &&
                        net.minecraft.block.Blocks.SNOW.getDefaultState().canPlaceAt(world, pos)) {
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}
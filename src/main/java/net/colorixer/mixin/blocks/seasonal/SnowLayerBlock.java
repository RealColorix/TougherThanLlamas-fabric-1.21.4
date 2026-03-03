package net.colorixer.mixin.blocks.seasonal;

import net.colorixer.util.SeasonTracker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowBlock.class)
public abstract class SnowLayerBlock extends Block {

    public SnowLayerBlock(Settings settings) {
        super(settings);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void ttll$startMelting(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        ttll$handleRapidThaw(state, world, pos);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        ttll$handleRapidThaw(state, world, pos);
    }

    private void ttll$handleRapidThaw(BlockState state, ServerWorld world, BlockPos pos) {
        long day = (SeasonTracker.activeSeasonDay - 12 + 96) % 96L;

        // If it is Spring, Summer, or early Fall
        if (day >= 72 || day < 36) {

            // Snowy biomes stay permanently frozen
            if (world.getBiome(pos).value().getTemperature() < 0.15f) return;

            // --- NEW: THE DEEP SUMMER INSTANT MELT ---
            // If it's deep Spring (80+) or Summer (< 36), skip the cascade entirely!
            if (day >= 80 || day < 36) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
                return;
            }

            // --- EARLY SPRING CASCADE ---
            // If it's days 72-79, slowly melt layer-by-layer
            int currentLayers = state.get(Properties.LAYERS);

            if (currentLayers > 1) {
                world.setBlockState(pos, state.with(Properties.LAYERS, currentLayers - 1), 2);
                world.scheduleBlockTick(pos, state.getBlock(), 40);
            } else {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            }
        }
    }
}
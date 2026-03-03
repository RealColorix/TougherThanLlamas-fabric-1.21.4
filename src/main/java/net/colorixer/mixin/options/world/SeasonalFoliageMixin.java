package net.colorixer.mixin.options.world;

import net.colorixer.util.SeasonTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public abstract class SeasonalFoliageMixin {

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private static void ttll$applySeasonalLeaves(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        ClientWorld clientWorld = MinecraftClient.getInstance().world;
        if (clientWorld == null) return;

        float temperature = clientWorld.getBiome(pos).value().getTemperature();

        // Skip seasons in hot biomes (Desert, Jungle stay green/vanilla)
        if (temperature >= 0.9f) return;

        // --- NEW: Snowy biomes stay permanently white! ---
        if (temperature < 0.15f) {
            cir.setReturnValue(0xFFFFFF);
            return;
        }

        int vanilla = cir.getReturnValue();

        long rawDay = SeasonTracker.activeSeasonDay;
        long day = (rawDay - 12 + 96) % 96L;
        float progress = (day % 24) / 24.0f;

        int colorAutumn = 0xCC7700;
        int colorWinter = 0xFFFFFF; // PURE WHITE SNOW
        int colorSpring = 0x55FF55;

        int finalColor;

        if (day < 24) {
            // Summer to Fall
            finalColor = SeasonTracker.blendColors(vanilla, colorAutumn, progress * 0.7f);
        } else if (day < 48) {
            // Fall to Winter: Blends up to 100% pure white!
            int startColor = SeasonTracker.blendColors(vanilla, colorAutumn, 0.7f);
            int endColor = SeasonTracker.blendColors(vanilla, colorWinter, 1.0f);
            finalColor = SeasonTracker.blendColors(startColor, endColor, progress);
        } else if (day < 72) {
            // Winter to Spring: Fades from 100% pure white back down to spring green
            int startColor = SeasonTracker.blendColors(vanilla, colorWinter, 1.0f);
            int endColor = SeasonTracker.blendColors(vanilla, colorSpring, 0.5f);
            finalColor = SeasonTracker.blendColors(startColor, endColor, progress);
        } else {
            // Spring to Summer
            int startColor = SeasonTracker.blendColors(vanilla, colorSpring, 0.5f);
            finalColor = SeasonTracker.blendColors(startColor, vanilla, progress);
        }

        cir.setReturnValue(finalColor);
    }

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private static void ttll$applySeasonalGrass(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        ClientWorld clientWorld = MinecraftClient.getInstance().world;
        if (clientWorld == null) return;

        float temperature = clientWorld.getBiome(pos).value().getTemperature();

        // Skip seasons in hot biomes
        if (temperature >= 0.9f) return;

        // --- NEW: Snowy biomes stay permanently white! ---
        if (temperature < 0.15f) {
            cir.setReturnValue(0xFFFFFF);
            return;
        }

        int vanilla = cir.getReturnValue();

        long rawDay = SeasonTracker.activeSeasonDay;
        long day = (rawDay - 12 + 96) % 96L;
        float progress = (day % 24) / 24.0f;

        int colorAutumn = 0xCC8833;
        int colorWinter = 0xFFFFFF; // PURE WHITE SNOW
        int colorSpring = 0x55FF55;

        int finalColor;

        if (day < 24) {
            // Summer to Fall
            finalColor = SeasonTracker.blendColors(vanilla, colorAutumn, progress * 0.35f);
        } else if (day < 48) {
            // Fall to Winter: Fades up to 90% pure white
            int startColor = SeasonTracker.blendColors(vanilla, colorAutumn, 0.35f);
            int endColor = SeasonTracker.blendColors(vanilla, colorWinter, 0.90f);
            finalColor = SeasonTracker.blendColors(startColor, endColor, progress);
        } else if (day < 72) {
            // Winter to Spring: Fades from 90% white down to spring green
            int startColor = SeasonTracker.blendColors(vanilla, colorWinter, 0.90f);
            int endColor = SeasonTracker.blendColors(vanilla, colorSpring, 0.15f);
            finalColor = SeasonTracker.blendColors(startColor, endColor, progress);
        } else {
            // Spring to Summer
            int startColor = SeasonTracker.blendColors(vanilla, colorSpring, 0.15f);
            finalColor = SeasonTracker.blendColors(startColor, vanilla, progress);
        }

        cir.setReturnValue(finalColor);
    }
}
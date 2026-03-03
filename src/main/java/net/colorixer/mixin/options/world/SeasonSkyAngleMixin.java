package net.colorixer.mixin.options.world;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;

@Mixin(DimensionType.class)
public abstract class SeasonSkyAngleMixin {

    // Vanilla uses this to check if time is frozen (like in the Nether/End)
    @Shadow @Final private OptionalLong fixedTime;

    @Inject(method = "getSkyAngle", at = @At("HEAD"), cancellable = true)
    private void ttll$applySeasonalSkyAngle(long time, CallbackInfoReturnable<Float> cir) {

        // If we are in the Nether or End, let vanilla handle it normally
        if (this.fixedTime.isPresent()) return;

        long currentDay = (time / 24000L) % 96L;
        long timeOfDay = time % 24000L;

        // Calculate today's daylight duration using our Cosine wave
        double dayLength = 12000.0 + 2400.0 * Math.cos((currentDay / 96.0) * 2.0 * Math.PI);
        double nightLength = 24000.0 - dayLength;

        double warpedTime;

        // TIME WARPING LOGIC
        if (timeOfDay < dayLength) {
            // We are in daytime. Squish or stretch the current time into vanilla's 0 -> 12000 scale.
            warpedTime = (timeOfDay / dayLength) * 12000.0;
        } else {
            // We are in nighttime. Map it to vanilla's 12000 -> 24000 scale.
            warpedTime = 12000.0 + ((timeOfDay - dayLength) / nightLength) * 12000.0;
        }

        // Now we feed our fake 'warpedTime' directly into the vanilla math you pasted!
        double d = MathHelper.fractionalPart(warpedTime / 24000.0 - 0.25);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        float skyAngle = (float)(d * 2.0 + e) / 3.0F;

        cir.setReturnValue(skyAngle);
    }
}
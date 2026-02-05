package net.colorixer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightmapTextureManager.class)
public abstract class MoonPhaseLightMapper {

    @ModifyVariable(method = "update", at = @At("STORE"), ordinal = 1)
    private float adjustSkyLightByMoonPhase(float g) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return g;

        float skyAngle = client.world.getSkyAngle(1.0F);

        // Check if it's night (Standard vanilla night range)
        if (skyAngle > 0.25f && skyAngle < 0.75f) {
            int phase = client.world.getMoonPhase();
            float multiplier;

            // Scale: 1.0 is full vanilla brightness, 0.0 is pitch black
            switch (phase) {
                case 0: multiplier = 1.0f; break;  // Full Moon (7/7 of vanilla night)
                case 1:
                case 7: multiplier = 0.9f; break;  // ~5/7
                case 2:
                case 6: multiplier = 0.75f; break; // ~4/7
                case 3:
                case 5: multiplier = 0.5f; break;  // ~3/7
                case 4: multiplier = 0.0f; break;  // New Moon (0/7)
                default: multiplier = 1.0f;
            }

            return g * multiplier;
        }

        return g;
    }
}
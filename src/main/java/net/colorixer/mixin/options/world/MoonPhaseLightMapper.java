package net.colorixer.mixin.options.world;

import com.mojang.blaze3d.systems.RenderSystem;
import net.colorixer.util.TtllLightingState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public abstract class MoonPhaseLightMapper {

    @ModifyVariable(method = "update", at = @At("STORE"), ordinal = 1)
    private float adjustSkyLightByMoonPhase(float g) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return g;

        float skyAngle = client.world.getSkyAngle(1.0F);

        // Logic for Moon Multiplier calculation
        if (skyAngle > 0.25f && skyAngle < 0.75f) {
            int phase = client.world.getMoonPhase();
            float targetMultiplier;

            switch (phase) {
                case 0: targetMultiplier = 0.36f; break; // Full Moon
                case 1, 7:targetMultiplier = 0.33f; break;
                case 2, 6:targetMultiplier = 0.30f; break;
                case 3, 5:targetMultiplier = 0.27f; break;
                case 4: targetMultiplier = 0.01f; break; // New Moon
                default: targetMultiplier = 0.36f;
            }

            float transitionAlpha = 1.0f;
            if (skyAngle >= 0.25f && skyAngle <= 0.30f) {
                transitionAlpha = (skyAngle - 0.25f) / 0.05f;
            } else if (skyAngle >= 0.70f && skyAngle <= 0.75f) {
                transitionAlpha = (0.75f - skyAngle) / 0.05f;
            }

            TtllLightingState.currentMoonMultiplier = MathHelper.lerp(transitionAlpha, 1.0f, targetMultiplier);
            return g * TtllLightingState.currentMoonMultiplier;
        }

        TtllLightingState.currentMoonMultiplier = 1.0f;
        return g;
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/SimpleFramebuffer;beginWrite(Z)V"))
    private void injectMoonToLightmapShader(float delta, CallbackInfo ci) {
        ShaderProgram shader = RenderSystem.getShader();
        if (shader != null) {
            // In 1.21.4, we access uniforms directly and use .set()
            // We've already modified 'g' (SkyFactor) via @ModifyVariable,
            // but for Sodium 0.6 compatibility, we ensure the uniform matches our math.
            var skyFactor = shader.getUniform("SkyFactor");
            if (skyFactor != null) {
                // We use getFloatData().get(0) to read, but since we have the multiplier
                // in TtllLightingState, we can just apply it to the existing uniform value.
                float currentVal = skyFactor.getFloatData().get(0);
                skyFactor.set(currentVal * TtllLightingState.currentMoonMultiplier);
            }
        }
    }
}
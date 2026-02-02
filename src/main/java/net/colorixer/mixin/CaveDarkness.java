package net.colorixer.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class CaveDarkness {

    // We override getBrightness to ensure the lightmap texture itself
    // doesn't try to "rescue" the darkness with ambient values.
    @Inject(method = "getBrightness(FI)F", at = @At("HEAD"), cancellable = true)
    private static void forceTrueZero(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (lightLevel == 0) {
            cir.setReturnValue(0.0f);
        }
    }
}
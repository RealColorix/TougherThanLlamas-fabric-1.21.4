package net.colorixer.mixin;

import net.colorixer.util.GloomHelper; // Make sure this import matches your package
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class Gloom {

    @Inject(method = "getFovMultiplier", at = @At("RETURN"), cancellable = true)
    private void applyGloomFov(CallbackInfoReturnable<Float> info) {
        if (GloomHelper.gloomLevel > 0) {
            float currentFov = info.getReturnValue();

            // Lerp between 1.0 (no change) and 1.5 (max zoom) based on gloomLevel
            float multiplier = 1.0f + (GloomHelper.gloomLevel * 0.5f);

            info.setReturnValue(currentFov * multiplier);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) (Object) this;
        // Only update for the local player
        if (player.getWorld().isClient) {
            GloomHelper.updateGloom(player);
        }
    }
}
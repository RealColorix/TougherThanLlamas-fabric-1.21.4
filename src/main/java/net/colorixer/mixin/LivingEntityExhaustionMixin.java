package net.colorixer.mixin;

import net.colorixer.access.PlayerArmorWeightAccessor;
import net.colorixer.util.ExhaustionHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityExhaustionMixin {

    private final float drainAmount = 0.1f;

    /**
     * TARGET: JUMPING
     * Adds exhaustion based on armor weight and triggers visual shake.
     */
    @Inject(method = "jump()V", at = @At("HEAD"))
    private void ttll$exhaustPlayerJump(CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity player) {
            // Get the armor weight via your accessor
            int armorWeight = ((PlayerArmorWeightAccessor) player).ttll$getArmorWeight();

            // Match your 1.5% multiplier logic: 1.0 + (Weight * 0.015)
            float weightMultiplier = 1.0f + (armorWeight * 0.015f);

            // Apply the penalized drain
            player.getHungerManager().addExhaustion(drainAmount * weightMultiplier);

            if (player.getWorld().isClient) {
                // Heavier gear feels "chunkier" - we increase jitter based on weight

                ExhaustionHelper.triggerJitter(10);
            }
        }
    }
}
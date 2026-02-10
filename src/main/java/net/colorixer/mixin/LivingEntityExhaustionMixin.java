package net.colorixer.mixin;

import net.colorixer.util.ExhaustionHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityExhaustionMixin {


    /**
     * TARGET: JUMPING
     * Adds 1.0 exhaustion and triggers the visual shake timer.
     */
    @Inject(method = "jump()V", at = @At("HEAD"))
    private void ttll$exhaustPlayerJump(CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity player) {
            player.getHungerManager().addExhaustion(1.0F);
            if (player.getWorld().isClient) {
                ExhaustionHelper.triggerJitter(10);
            }
        }
    }

}
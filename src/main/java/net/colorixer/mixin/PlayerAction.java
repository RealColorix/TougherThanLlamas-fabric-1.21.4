package net.colorixer.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PlayerAction {

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void ttll$restrictSprinting(boolean sprinting, CallbackInfo ci) {
        if (sprinting && (Object) this instanceof PlayerEntity player) {
            if (player.isCreative() || player.isSpectator()) return;

            if (player.getHealth() <= 8.0f || player.getHungerManager().getFoodLevel() <= 8) {
                // Sprinting is usually safe to cancel, but we'll check just in case
                if (ci.isCancellable()) ci.cancel();
                player.setSprinting(false);
            }
        }
    }

    @Inject(method = "jump", at = @At("TAIL"))
    private void ttll$restrictAndScaleJump(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player) {
            if (player.isCreative() || player.isSpectator()) return;

            // 1. Hard Grounding (Health/Hunger <= 4)
            if (player.getHealth() <= 4.0f || player.getHungerManager().getFoodLevel() <= 4) {
                Vec3d currentVel = player.getVelocity();
                // We set Y to 0 (or even a tiny negative to stick them to the floor)
                player.setVelocity(currentVel.x, 0.0, currentVel.z);
                return;
            }

            // 2. Horizontal Speed Penalty (Healthy-ish players)
            float healthRatio = player.getHealth() / player.getMaxHealth();
            float hungerRatio = player.getHungerManager().getFoodLevel() / 20.0f;

            float hMult = healthRatio > 0.25f ? 0.8f + (healthRatio - 0.25f) * (0.2f / 0.75f) : 0.1f + 0.7f * healthRatio * healthRatio;
            float fMult = hungerRatio > 0.25f ? 0.8f + (hungerRatio - 0.25f) * (0.2f / 0.75f) : 0.1f + 0.7f * hungerRatio * hungerRatio;

            float totalMult = hMult * fMult;

            Vec3d vel = player.getVelocity();
            // We multiply X and Z, but keep the Y that Minecraft just calculated
            player.setVelocity(vel.x * totalMult, vel.y, vel.z * totalMult);
        }
    }
}
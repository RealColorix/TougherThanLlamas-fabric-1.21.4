package net.colorixer.mixin;

import net.colorixer.util.ExhaustionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerActionExhaustionMixin {

    @Shadow public abstract HungerManager getHungerManager();

    @Unique
    private Vec3d ttll$lastPos;

    private final float drainAmount = 1.0f;
    private final float sprintDrainPerBlock = 0.1f;

    @Inject(method = "attack", at = @At("HEAD"))
    private void ttll$exhaustOnAttack(Entity target, CallbackInfo ci) {
        this.getHungerManager().addExhaustion(drainAmount);
        if (target.getWorld().isClient) {
            ExhaustionHelper.triggerJitter(10);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$exhaustTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // --- Climbing Logic ---
        if (player.isClimbing() && player.getVelocity().y != 0) {
            if (!player.getWorld().isClient) {
                this.getHungerManager().addExhaustion(drainAmount / 10);
            } else {
                ExhaustionHelper.triggerJitter(5);
            }
        }

        // --- Sprinting Logic (Any Block) ---
        if (ttll$lastPos != null && player.isSprinting() && player.isOnGround()) {
            // Calculate horizontal distance moved
            double distance = Math.sqrt(
                    Math.pow(player.getX() - ttll$lastPos.x, 2) +
                            Math.pow(player.getZ() - ttll$lastPos.z, 2)
            );

            if (distance > 0) {
                float exhaustionToAdd = (float) (distance * sprintDrainPerBlock);
                this.getHungerManager().addExhaustion(exhaustionToAdd);
                ExhaustionHelper.triggerJitter(5);
            }
        }

        // Update position for the next tick
        ttll$lastPos = player.getPos();
    }
}
package net.colorixer.mixin;

import net.colorixer.access.PlayerArmorWeightAccessor;
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

    private final float drainAmount = 0.1f;
    private final float sprintDrainPerBlock = 0.05f;

    @Inject(method = "attack", at = @At("HEAD"))
    private void ttll$exhaustOnAttack(Entity target, CallbackInfo ci) {
        // Apply the weight penalty to combat
        this.getHungerManager().addExhaustion(drainAmount * ttll$getWeightMultiplier());
        if (target.getWorld().isClient) {
            ExhaustionHelper.triggerJitter(10);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ttll$exhaustTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        float weightMultiplier = ttll$getWeightMultiplier();

        // --- Climbing Logic ---
        if (player.isClimbing() && Math.abs(player.getVelocity().y) > 0.1 && !player.isOnGround()) {
            if (!player.getWorld().isClient) {
                this.getHungerManager().addExhaustion((drainAmount / 10.0f) * weightMultiplier);
            }
            ExhaustionHelper.triggerJitter(5);
        }

        // --- Sprinting Logic ---
        if (ttll$lastPos != null && player.isSprinting()) {
            double distance = Math.sqrt(
                    Math.pow(player.getX() - ttll$lastPos.x, 2) +
                            Math.pow(player.getZ() - ttll$lastPos.z, 2)
            );

            if (distance > 0) {
                // Uses the same 1.5% penalty scaling as your movement speed
                float exhaustionToAdd = (float) (distance * sprintDrainPerBlock * weightMultiplier);
                this.getHungerManager().addExhaustion(exhaustionToAdd);
                ExhaustionHelper.triggerJitter(5);
            }
        }
        ttll$lastPos = player.getPos();
    }

    /**
     * Matches your movement speed penalty:
     * Each weight point increases exhaustion by 1.5%.
     */
    @Unique
    private float ttll$getWeightMultiplier() {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Using your Accessor to get the pre-calculated weight
        int armorWeight = ((PlayerArmorWeightAccessor) player).ttll$getArmorWeight();

        // 1.0 + (Weight * 0.015)
        // Netherite/Gold (20 weight) = 1.3x Exhaustion (30% more hunger drain)
        // Iron (15 weight) = 1.225x Exhaustion
        return 1.0f + (armorWeight * 0.015f);
    }
}
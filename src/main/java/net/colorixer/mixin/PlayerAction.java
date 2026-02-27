package net.colorixer.mixin;

import net.colorixer.sounds.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "tick", at = @At("TAIL"))
    private void ttll$cancelSprintingOnLowStats(CallbackInfo ci) {
        if ((Object)this instanceof PlayerEntity player) {
            if (player.isCreative() || player.isSpectator()) return;

            if (player.getHealth() <= 8.0f || player.getHungerManager().getFoodLevel() <= 8) {
                if (player.isSprinting()) {
                    player.setSprinting(false);
                }
            }
        }
    }


    private int lastDamageSoundTick = 0;

    // Make sure to import your effect and StatusEffectInstance at the top!
// import net.colorixer.registry.ModEffects;
// import net.minecraft.entity.effect.StatusEffectInstance;

    @Inject(method = "damage", at = @At("TAIL"))
    private void playCustomDamageSoundAndBleed(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerEntity player) {

            // ==========================================
            // BLEEDING LOGIC
            // ==========================================

            if (amount >= 2.0f  && !source.isOf(DamageTypes.STARVE)
                                && !source.isOf(DamageTypes.DROWN)
                                && !source.isOf(DamageTypes.ON_FIRE)
                                && !source.isOf(DamageTypes.FREEZE)
                    && !source.isOf(DamageTypes.WITHER)
                    && !source.isOf(DamageTypes.CAMPFIRE)
                    && !source.isOf(DamageTypes.INDIRECT_MAGIC)
                    && !source.isOf(DamageTypes.LAVA)
                    && !source.isOf(DamageTypes.IN_FIRE)) {

                int ticksToAdd = (int) amount * 50;
                int totalDuration = ticksToAdd;

                if (player.hasStatusEffect(net.colorixer.effect.ModEffects.BLEEDING)) {
                    totalDuration += player.getStatusEffect(net.colorixer.effect.ModEffects.BLEEDING).getDuration();
                }

                // Apply the new stacked duration!
                player.addStatusEffect(new StatusEffectInstance(
                        net.colorixer.effect.ModEffects.BLEEDING,
                        totalDuration,
                        0,
                        false, // ambient
                        false,  // show particles
                        true   // show icon
                ));
            }

            // ==========================================
            // SOUND LOGIC
            // ==========================================

            if (amount <= 0 || player.isInvulnerableTo(world, source)) return;
            if (player.isUsingItem() && player.getActiveItem().isOf(net.minecraft.item.Items.SHIELD)) return;

            // Skip if it's been less than 10 ticks since last sound
            if (player.age - lastDamageSoundTick < 10) return;
            lastDamageSoundTick = player.age;

            float randomVolume = 0.8f + (player.getRandom().nextFloat() * 0.4f);
            float healthPercent = player.getHealth() / player.getMaxHealth();
            float basePitch = 0.8f + (healthPercent * 0.2f);
            float randomPitch = basePitch + (player.getRandom().nextFloat() * 0.2f - 0.1f);

            world.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.CLASSIC_HURT,
                    SoundCategory.PLAYERS,
                    randomVolume,
                    randomPitch
            );
        }
    }
}
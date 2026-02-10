package net.colorixer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.colorixer.access.PlayerArmorWeightAccessor;
import net.colorixer.item.ModItems;
import net.colorixer.sounds.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class PlayerCombatModifiers {

    // Target the simple version that exists in your LivingEntity snippet
    @Shadow public abstract void playSound(SoundEvent sound);

    @Unique
    private static final Map<Item, Double> KNOCKBACK_MULTIPLIERS = new HashMap<>();
    @Unique
    private static boolean initialized = false;

    @Unique
    private static void initializeMap() {
        if (initialized) return;
        KNOCKBACK_MULTIPLIERS.put(ModItems.WOODEN_CLUB, 0.5);
        KNOCKBACK_MULTIPLIERS.put(ModItems.BONE_CLUB, 0.6);
        KNOCKBACK_MULTIPLIERS.put(Items.WOODEN_SWORD, 0.5);
        KNOCKBACK_MULTIPLIERS.put(Items.STONE_SWORD, 0.5);
        KNOCKBACK_MULTIPLIERS.put(Items.IRON_SWORD, 0.8);
        KNOCKBACK_MULTIPLIERS.put(Items.GOLDEN_SWORD, 0.5);
        KNOCKBACK_MULTIPLIERS.put(Items.DIAMOND_SWORD, 1.0);
        KNOCKBACK_MULTIPLIERS.put(Items.NETHERITE_SWORD, 1.1);
        initialized = true;
    }

    @Inject(
            method = "computeFallDamage(FF)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ttll$applyWeightToFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        // Only run for players
        if ((Object)this instanceof PlayerEntity player) {

            // 1. Get the base damage multiplier attribute (the 1.5x you set in defaults)
            float baseMultiplier = (float) player.getAttributeValue(EntityAttributes.FALL_DAMAGE_MULTIPLIER);

            // 2. Get the weight from your accessor
            int armorWeight = ((PlayerArmorWeightAccessor) player).ttll$getArmorWeight();


            float finalWeightMultiplier = baseMultiplier + (armorWeight * 0.075f);

            // 4. Calculate exactly how Minecraft does it, but with our new multiplier
            float safeDistance = (float) player.getAttributeValue(EntityAttributes.SAFE_FALL_DISTANCE);
            float damageAmount = (fallDistance - safeDistance) * finalWeightMultiplier;

            // Return the final damage (rounded up like vanilla)
            cir.setReturnValue(Math.max(0, Math.round(damageAmount)));
        }
    }

    @Unique
    private double calculateStatsMultiplier(PlayerEntity player) {
        float healthRatio = player.getHealth() / player.getMaxHealth();
        float hungerRatio = player.getHungerManager().getFoodLevel() / 20.0f;
        float healthMultiplier = (healthRatio > 0.25f) ? 0.8f + (healthRatio - 0.25f) * (0.2f / 0.75f) : 0.1f + 0.7f * (healthRatio / 0.25f) * (healthRatio / 0.25f);
        float hungerMultiplier = (hungerRatio > 0.25f) ? 0.8f + (hungerRatio - 0.25f) * (0.2f / 0.75f) : 0.1f + 0.7f * (hungerRatio / 0.25f) * (hungerRatio / 0.25f);
        return (double) (healthMultiplier * hungerMultiplier);
    }

    @ModifyVariable(method = "takeKnockback", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double ttll$applyCustomKnockback(double strength) {
        initializeMap();
        LivingEntity target = (LivingEntity) (Object) this;
        Entity attacker = target.getAttacker();
        if (attacker instanceof PlayerEntity player) {
            double statsMultiplier = calculateStatsMultiplier(player);
            Item heldItem = player.getMainHandStack().getItem();
            double itemMultiplier = KNOCKBACK_MULTIPLIERS.getOrDefault(heldItem, 0.0);
            return strength * itemMultiplier * statsMultiplier;
        }
        return strength;
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float ttll$applyDamagePenalty(float amount, ServerWorld world, DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker instanceof PlayerEntity player) {
            ItemStack stack = player.getMainHandStack();
            Item item = stack.getItem();

            if (!(item instanceof SwordItem || item instanceof MiningToolItem || item instanceof TridentItem || item instanceof MaceItem)) {
                if (player.getWorld().random.nextFloat() < 0.5f) {
                    return 0.0f;
                } else {
                    amount -= 0.5f;
                }
            }
            if (amount < 0.0f) amount = 0.0f;
            return (float) (amount * calculateStatsMultiplier(player));
        }
        return amount;
    }

    @Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
    private void ttll$swapHandSound(DamageSource source, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity player && player.getMainHandStack().isEmpty()) {
            // Calling the shadowed method that is defined in LivingEntity.java
            this.playSound(ModSounds.CLASSIC_HURT);
            ci.cancel();
        }
    }
}
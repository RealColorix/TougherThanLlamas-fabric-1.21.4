package net.colorixer.mixin;

import net.colorixer.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class PlayerCombatModifiers {

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

    @Unique
    private double calculateStatsMultiplier(PlayerEntity player) {
        float healthRatio = player.getHealth() / player.getMaxHealth();
        float hungerRatio = player.getHungerManager().getFoodLevel() / 20.0f;

        float healthMultiplier = (healthRatio > 0.25f)
                ? 0.8f + (healthRatio - 0.25f) * (0.2f / 0.75f)
                : 0.1f + 0.7f * (healthRatio / 0.25f) * (healthRatio / 0.25f);

        float hungerMultiplier = (hungerRatio > 0.25f)
                ? 0.8f + (hungerRatio - 0.25f) * (0.2f / 0.75f)
                : 0.1f + 0.7f * (hungerRatio / 0.25f) * (hungerRatio / 0.25f);

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

    // Fixed the signature for 1.21.4 (Added ServerWorld world)
    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float ttll$applyDamagePenalty(float amount, ServerWorld world, DamageSource source) {
        Entity attacker = source.getAttacker();

        if (attacker instanceof PlayerEntity player) {
            ItemStack stack = player.getMainHandStack();
            Item item = stack.getItem();

            // --- 1. TOOL CHECK PENALTY ---
            if (!(item instanceof SwordItem || item instanceof MiningToolItem)) {
                amount -= 0.5f;
            }

            if (amount < 0.0f) amount = 0.0f;

            // --- 2. STATS PENALTY ---
            double statsMultiplier = calculateStatsMultiplier(player);

            return (float) (amount * statsMultiplier);
        }

        return amount;
    }
}
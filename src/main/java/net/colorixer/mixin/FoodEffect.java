package net.colorixer.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies food sickness effects when food is fully consumed.
 * Correct hook for Minecraft 1.21.4.
 */
@Mixin(ItemStack.class)
public abstract class FoodEffect {

    @Inject(
            method = "finishUsing",
            at = @At("TAIL")
    )
    private void ttll$afterEat(
            World world,
            LivingEntity user,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (world.isClient()) return;

        ItemStack stack = (ItemStack)(Object)this;
        Random random = user.getRandom();

        /* =========================
         * RAW BEEF — infection risk
         * ========================= */
        if (stack.isOf(Items.BEEF)) {
            if (random.nextFloat() < 0.25F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        1200, 1
                ));
            }
        }

        if (stack.isOf(Items.PORKCHOP)) {
            if (random.nextFloat() < 0.25F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        1200, 1
                ));
            } else if (random.nextFloat() < 0.2F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        1200, 2
                ));
            }
        }

        if (stack.isOf(Items.MUTTON)) {
            if (random.nextFloat() < 0.25F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        1200, 1
                ));
            }
        }

        /* =========================
         * SWEET BERRIES — mild poison
         * ========================= */
        if (stack.isOf(Items.SWEET_BERRIES)) {
            if (random.nextFloat() < 0.25F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.POISON,
                        300, // 15 seconds
                        0
                ));
            }
        }

        if (stack.isOf(Items.GLOW_BERRIES)) {
            if (random.nextFloat() < 0.25F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WITHER,
                        300, // 15 seconds
                        0
                ));
            }
        }
        /* =========================
         * ROTTEN FLESH — guaranteed sickness
         * ========================= */
        if (stack.isOf(Items.ROTTEN_FLESH)) {
            user.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.HUNGER,
                    600, // 30 seconds
                    1
            ));
        }
    }
}

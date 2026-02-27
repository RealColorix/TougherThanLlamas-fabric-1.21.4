package net.colorixer.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

    /* --- NEW LOGIC: BLOCK EATING WHILE HUNGER EFFECT IS ACTIVE --- */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void ttll$preventEatingWithHungerEffect(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        // In 1.21.4, check if the item has a "Consumable" or "Food" component
        // Most food now uses the CONSUMABLE component which points to food settings
        if (stack.contains(DataComponentTypes.FOOD)) {
            if (user.hasStatusEffect(StatusEffects.HUNGER)) {
                // In 1.21.4, we return ActionResult.FAIL or ActionResult.PASS
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }

    /* --- YOUR ORIGINAL CODE (UNTOUCHED) --- */
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


        if (stack.isOf(Items.BEEF)) {
            if (random.nextFloat() < 0.5F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        2400, 1
                ));
            }
        }

        else if (stack.isOf(Items.PORKCHOP)) {
            if (random.nextFloat() < 0.5F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        2400, 1
                ));
            }
        }

        else if (stack.isOf(Items.CHICKEN)) {
            if (random.nextFloat() < 0.5F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        2400, 1
                ));
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.POISON,
                        60, 0
                ));
            }
        }

        else if (stack.isOf(Items.COD)) {
            if (random.nextFloat() < 0.5F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        2400, 1
                ));
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.POISON,
                        60, 0
                ));
            }
        }

        else if (stack.isOf(Items.SALMON)) {
            if (random.nextFloat() < 0.5F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        2400, 1
                ));
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.POISON,
                        60, 0
                ));
            }
        }

        else if (stack.isOf(Items.MUTTON)) {
            if (random.nextFloat() < 0.5F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.HUNGER,
                        2400, 1
                ));
            }
        }

        /* =========================
         * SWEET BERRIES — mild poison
         * ========================= */
        else if (stack.isOf(Items.SWEET_BERRIES)) {
            if (random.nextFloat() < 0.222F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.POISON,
                        400, // 20 seconds
                        0
                ));
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.NAUSEA,
                        400, // 20 seconds
                        0
                ));
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.BLINDNESS,
                        400, // 5 seconds
                        0
                ));
            }
        }

        else if (stack.isOf(Items.GLOW_BERRIES)) {
            if (random.nextFloat() < 0.222F) {
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WITHER,
                        400, // 20 seconds
                        0
                ));
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.MINING_FATIGUE,
                        1200, // 1 minute
                        0
                ));
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS,
                        2400, // 2 minutes
                        0
                ));
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS,
                        2400, // 2 minutes
                        0
                ));
            }
        }

        else if (stack.isOf(Items.ROTTEN_FLESH)) {
            user.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.HUNGER,
                    1200, // 30 seconds
                    2
            ));
        }
    }
}
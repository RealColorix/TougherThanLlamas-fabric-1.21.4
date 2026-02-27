package net.colorixer.item.items;

import net.colorixer.effect.ModEffects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BandageItem extends Item {

    public BandageItem(Settings settings) {
        super(
                settings.component(
                        DataComponentTypes.CONSUMABLE,
                        ConsumableComponent.builder()
                                .consumeSeconds(5.0f)
                                .useAction(UseAction.BOW)
                                .build()
                )
        );
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // Now allows the player to use the bandage at any time
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            StatusEffectInstance activeBleed = user.getStatusEffect(ModEffects.BLEEDING);

            // Case 1: Heavy Bleeding (More than 15 seconds)
            if (activeBleed != null && activeBleed.getDuration() > 300) {
                user.removeStatusEffect(ModEffects.BLEEDING);
                user.addStatusEffect(new StatusEffectInstance(
                        ModEffects.BLEEDING,
                        300,
                        activeBleed.getAmplifier(),
                        activeBleed.isAmbient(),
                        activeBleed.shouldShowParticles(),
                        activeBleed.shouldShowIcon()
                ));
            }
            // Case 2: Light Bleeding (<= 15s) or No Bleeding at all
            else {
                // Clear any remaining minor bleeding
                if (activeBleed != null) {
                    user.removeStatusEffect(ModEffects.BLEEDING);
                }

                // Apply Regeneration for 8 seconds (160 ticks)
                user.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.REGENERATION,
                        100,
                        0
                ));
            }
        }

        return super.finishUsing(stack, world, user);
    }
}
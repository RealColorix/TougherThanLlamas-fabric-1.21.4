package net.colorixer.mixin.items.food;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApplyEffectsConsumeEffect.class)
public class RemoveVanillaChickenEffectMixin {

    @Inject(method = "onConsume", at = @At("HEAD"), cancellable = true)
    private void cancelVanillaEffects(World world, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir) {

        // If the item being eaten is Chicken, cancel the vanilla effect!
        if (stack.isOf(Items.CHICKEN)) {


            cir.setReturnValue(true);
        }
    }
}
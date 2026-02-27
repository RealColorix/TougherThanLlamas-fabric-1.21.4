package net.colorixer.mixin; // Make sure this matches your package!

import net.colorixer.item.items.KnittingSticksItem;
import net.colorixer.item.items.SugarcaneMashItem;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Redirect(
            method = "applyEatOrDrinkTransformation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getItemUseTimeLeft()I")
    )
    private int fakeUseTimeLeftForKnitting(PlayerEntity player) {
        ItemStack stack = player.getActiveItem();

        if (stack.getItem() instanceof KnittingSticksItem)  {
            int actualMax = stack.getMaxUseTime(player);
            int actualLeft = player.getItemUseTimeLeft();
            int ticksUsed = actualMax - actualLeft;

            if (ticksUsed < 30) {
                return 30 - ticksUsed; // Smooth raise
            } else {
                return actualLeft; // Let it drop naturally so the bobbing is smooth!
            }
        }
        return player.getItemUseTimeLeft();
    }

    @Redirect(
            method = "applyEatOrDrinkTransformation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxUseTime(Lnet/minecraft/entity/LivingEntity;)I")
    )
    private int fakeMaxUseTimeForKnitting(ItemStack stack, LivingEntity user) {
        if (stack.getItem() instanceof KnittingSticksItem) {

                int actualMax = stack.getMaxUseTime(user);
                int actualLeft = user.getItemUseTimeLeft();
                int ticksUsed = actualMax - actualLeft;

                if (ticksUsed < 30) {
                    return 30;
                } else {
                    return 10000000;
                }
        }
        return stack.getMaxUseTime(user);
    }
}
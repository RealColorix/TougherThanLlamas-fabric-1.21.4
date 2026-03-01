package net.colorixer.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Inject(method = "usableWhenFillingSlot", at = @At("HEAD"), cancellable = true)
    private static void ttll$allowDamagedToolsToAutoFill(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack != null && !stack.isEmpty() && stack.isDamageable() && stack.isDamaged()) {
            if (!stack.hasEnchantments() && !stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "populateRecipeFinder", at = @At("HEAD"), cancellable = true)
    private void ttll$forceInfiniteToolStack(RecipeFinder finder, CallbackInfo ci) {
        PlayerInventory inventory = (PlayerInventory) (Object) this;

        for (ItemStack stack : inventory.main) {
            if (stack.isEmpty()) continue;

            // Target tools that are not enchanted or named
            if (stack.isDamageable() && !stack.hasEnchantments() && !stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                ItemStack fakeStack = stack.copy();
                fakeStack.setDamage(0);   // Strip damage
                fakeStack.setCount(127);  // SET TO 127 AS REQUESTED
                finder.addInputIfUsable(fakeStack);
            } else {
                finder.addInputIfUsable(stack);
            }
        }
        ci.cancel(); // Kill vanilla double-counting
    }
}
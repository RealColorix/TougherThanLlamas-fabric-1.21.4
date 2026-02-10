package net.colorixer.mixin;

import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.recipe.display.RecipeDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeToast.class)
public class RecipeToastMixin {

    @Inject(
            method = "show",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void cancelRecipeToast(ToastManager toastManager, RecipeDisplay display, CallbackInfo ci) {
        // Since the method is static in RecipeToast, this must be static.
        // Since the method returns void, we use CallbackInfo and just cancel.
        ci.cancel();
    }
}
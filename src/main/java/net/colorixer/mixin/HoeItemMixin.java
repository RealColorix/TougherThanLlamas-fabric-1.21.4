package net.colorixer.mixin;

import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public class HoeItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void stopTilling(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        // This stops the default "Till Grass to Farmland" logic completely.
        // We return PASS so other systems (like your Chopable logic) can handle it,
        // or FAIL if you want to explicitly block all right-click actions.
        cir.setReturnValue(ActionResult.PASS);
    }
}
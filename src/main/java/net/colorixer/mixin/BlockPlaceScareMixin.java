package net.colorixer.mixin;

import net.colorixer.util.WorldUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockPlaceScareMixin {

    @Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemPlacementContext;getBlockPos()Lnet/minecraft/util/math/BlockPos;"))
    private void ttll$scareOnPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        // Only run on the server
        if (context.getWorld().isClient) return;

        // Use the WorldUtil you already have to flip the 'ttll$blockScared' boolean to true
        WorldUtil.scareNearbyCows(context.getWorld(), context.getBlockPos(), 5.0);
    }
}
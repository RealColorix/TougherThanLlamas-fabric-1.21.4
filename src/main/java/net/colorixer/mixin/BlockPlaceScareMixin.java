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
        // Safety check: only run on the server to avoid ghost movements/desync
        if (context.getWorld().isClient) return;

        // UPDATED: Now calls the generalized animal method
        // Radius is 5.0 blocks around the placement position
        WorldUtil.scareNearbyAnimals(context.getWorld(), context.getBlockPos(), 5.0);
    }
}
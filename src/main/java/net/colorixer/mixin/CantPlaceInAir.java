package net.colorixer.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class CantPlaceInAir {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void ttll$preventAirPlacement(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = context.getPlayer();

        if (player != null) {
            if (!player.getAbilities().creativeMode) {

                boolean onGround = player.isOnGround();
                boolean inLiquid = player.isTouchingWater() || player.isInLava();
                boolean climbing = player.isClimbing();

                if (!onGround && !inLiquid && !climbing) {
                    cir.setReturnValue(ActionResult.FAIL);
                }
            }

        }
    }
}
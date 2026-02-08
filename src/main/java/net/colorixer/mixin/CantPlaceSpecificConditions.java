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
public class CantPlaceSpecificConditions {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void ttll$preventAirPlacement(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = context.getPlayer();

        if (player != null) {
            // Vi kollar bara begränsningar om spelaren INTE är i creative mode
            if (!player.getAbilities().creativeMode) {

                // 1. Kontroll för placering i luften
                boolean onGround = player.isOnGround();
                boolean inLiquid = player.isTouchingWater() || player.isInLava();
                boolean climbing = player.isClimbing();

                if (!onGround && !inLiquid && !climbing) {
                    cir.setReturnValue(ActionResult.FAIL);
                    return; // Avbryt tidigt om man är i luften
                }

                // 2. Kontroll för hälsa och hunger (<= 2.1)
                // 2.1 i hälsa motsvarar drygt ett hjärta.
                if (player.getHealth() <= 2.01f || player.getHungerManager().getFoodLevel() <= 2.01f) {
                    cir.setReturnValue(ActionResult.FAIL);
                }
            }
        }
    }
}
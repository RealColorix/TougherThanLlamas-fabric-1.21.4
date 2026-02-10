package net.colorixer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireChargeItem.class)
public class FireChargeMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void ttll$restrictedFireCharge(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        // Check if the block is burnable OR obsidian
        boolean isBurnable = state.isBurnable();
        boolean isObsidian = state.isOf(Blocks.OBSIDIAN);

        // If it's neither, "PASS" so the item does nothing (no fire, no item consumption)
        if (!isBurnable && !isObsidian) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}